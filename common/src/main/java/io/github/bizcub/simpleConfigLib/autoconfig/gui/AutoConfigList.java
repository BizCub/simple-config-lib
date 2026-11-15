package io.github.bizcub.simpleConfigLib.autoconfig.gui;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.bizcub.simpleConfigLib.autoconfig.annotation.Color;
import io.github.bizcub.simpleConfigLib.autoconfig.annotation.Slider;
import io.github.bizcub.simpleConfigLib.internal.GuiSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.state.gui.GuiTextRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix3x2f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class AutoConfigList extends ContainerObjectSelectionList<AutoConfigList.Row> {

    private static final int MAX_ROW_WIDTH = 520;
    private static final int SIDE_PADDING = 40;
    private static final int ROW_HEIGHT = 25;
    private static final int LABEL_COLOR = -1;
    private static final int DRAG_COLOR = 0xFF66BBFF;

    private static final int INDENT = 8;
    private static final int HEADER_BTN = 20;
    private static final int HANDLE_WIDTH = 12;
    private static final int HANDLE_GAP = 2;

    protected final Screen screen;
    private final Font font;

    private FormattedText pendingTooltip;
    private int pendingTooltipX;
    private int pendingTooltipY;

    private final List<Node> nodes = new ArrayList<>();

    private boolean readOnly;
    private String filter = "";
    private ListModel dragModel;
    private Element dragElement;
    private int dropIndex = -1;
    private double startX, startY, pointerX, pointerY;
    private boolean dragging;
    private Double pendingScroll;

    void focusWidget(AbstractWidget widget) {
        for (Row row : this.children()) {
            if (row.children().contains(widget)) {
                this.setFocused(row);
                row.setFocused(widget);
                widget.setFocused(true);
                if (this.screen != null) this.screen.setFocused(this);
                break;
            }
        }
    }

    public boolean containsWidget(AbstractWidget widget) {
        return containsWidget(this.nodes, widget);
    }

    private boolean containsWidget(List<Node> nodes, AbstractWidget widget) {
        for (Node node : nodes) {
            if (node instanceof WidgetNode field && field.widget == widget) return true;
            if (node instanceof GroupNode group && containsWidget(group.children, widget)) return true;
            if (node instanceof ListNode list) {
                for (Element element : list.model.elements) {
                    if (element instanceof ObjectElement object && containsWidget(object.group.children, widget)) return true;
                }
            }
        }
        return false;
    }

    public void setFilter(String value) {
        String next = value.strip().toLowerCase(Locale.ROOT);
        if (next.equals(this.filter)) return;
        cancelItemDrag();
        commitScalarElements();
        this.filter = next;
        rebuild();
        setScrollAmount(0);
    }

    record ViewState(double scroll, Map<String, Boolean> expanded) {}

    ViewState viewState() {
        Map<String, Boolean> expanded = new HashMap<>();
        visitNodes(this.nodes, "", (path, node) -> {
            if (node instanceof GroupNode group) expanded.put(path, group.expanded);
            if (node instanceof ListNode list) expanded.put(path, list.model.expanded);
        });
        return new ViewState(scrollAmount(), expanded);
    }

    void restoreViewState(ViewState state) {
        visitNodes(this.nodes, "", (path, node) -> {
            Boolean expanded = state.expanded().get(path);
            if (expanded == null) return;
            if (node instanceof GroupNode group) group.expanded = expanded;
            if (node instanceof ListNode list) list.model.expanded = expanded;
        });
        rebuild();
        this.pendingScroll = state.scroll();
    }

    void restoreScroll() {
        if (this.pendingScroll != null) {
            setScrollAmount(this.pendingScroll);
            this.pendingScroll = null;
        }
    }

    private void visitNodes(List<Node> nodes, String prefix, BiConsumer<String, Node> visitor) {
        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            String path = prefix + "/" + i;
            visitor.accept(path, node);
            if (node instanceof GroupNode group) visitNodes(group.children, path + "/g", visitor);
            if (node instanceof ListNode list) {
                for (int j = 0; j < list.model.elements.size(); j++) {
                    if (list.model.elements.get(j) instanceof ObjectElement object) {
                        visitNodes(List.of(object.group), path + "/e" + j, visitor);
                    }
                }
            }
        }
    }

    public AutoConfigList(final Minecraft minecraft, final int width, final int height, final int y, final Screen screen) {
        super(minecraft, width, height, y, ROW_HEIGHT);
        this.screen = screen;
        this.font = minecraft.font;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        for (Node n : this.nodes) {
            applyReadOnly(n);
        }
        rebuild();
    }

    private void applyReadOnly(Node node) {
        if (node instanceof WidgetNode w) {
            w.widget.active = false;
        } else if (node instanceof GroupNode g) {
            for (Node c : g.children) applyReadOnly(c);
        } else if (node instanceof ListNode l) {
            l.model.editable = false;
            for (Element e : l.model.elements) {
                if (e instanceof ObjectElement oe) {
                    for (Node c : oe.group.children) applyReadOnly(c);
                }
            }
        }
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        boolean overPopup = isPopupOpenAt(mouseX, mouseY)
                || this.screen instanceof AutoConfigScreen configScreen && configScreen.hasContextMenu();
        int hoverX = overPopup ? Integer.MIN_VALUE : mouseX;
        int hoverY = overPopup ? Integer.MIN_VALUE : mouseY;

        this.pendingTooltip = null;

        super.extractWidgetRenderState(graphics, hoverX, hoverY, a);
        for (Row row : this.children()) {
            row.renderHandle(graphics, hoverX, hoverY, a);
        }

        for (Row row : this.children()) {
            ColorWidget picker = colorPickerOf(row);
            if (picker != null && picker.pickerOpen()) {
                picker.renderPicker(graphics);
            }
        }

        renderDropTarget(graphics);
        if (this.pendingTooltip != null && !this.dragging) {
            graphics.setTooltipForNextFrame(this.font,
                    this.font.split(this.pendingTooltip, 200),
                    this.pendingTooltipX, this.pendingTooltipY);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        for (Row row : this.children()) {
            ColorWidget picker = colorPickerOf(row);
            if (picker != null && picker.isOverPicker(event.x(), event.y())) return row.mouseClicked(event, doubleClick);
        }
        if (inside(event.x(), event.y())) {
            for (Row row : this.children()) {
                if (event.y() < row.getContentY() || event.y() >= row.getContentY() + ROW_HEIGHT) continue;
                if (event.button() == 3) {
                    row.openContext((int) event.x(), (int) event.y());
                    return true;
                }
                if (event.button() == 1 && row.canDrag() && row.overHandle(event.x())) {
                    commitScalarElements();
                    this.dragModel = row.model();
                    this.dragElement = this.dragModel.elements.get(row.index());
                    this.startX = this.pointerX = event.x();
                    this.startY = this.pointerY = event.y();
                    this.setFocused(row);
                    row.setFocused(row.handle());
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    private boolean inside(double x, double y) {
        return x >= getRowLeft() && x < getRowLeft() + getRowWidth() && y >= getY() && y < getBottom();
    }

    public boolean hasItemDrag() {
        return this.dragModel != null;
    }

    public void cancelItemDrag() {
        this.dragModel = null;
        this.dragElement = null;
        this.dragging = false;
        this.dropIndex = -1;
    }

    public void tickDrag() {
        if (!this.dragging || !inside(this.pointerX, this.pointerY)) return;
        if (this.pointerY < getY() + 20) setScrollAmount(scrollAmount() - 10);
        else if (this.pointerY > getBottom() - 20) setScrollAmount(scrollAmount() + 10);
        updateDropTarget();
    }

    private void updateDropTarget() {
        this.dropIndex = -1;
        if (!inside(this.pointerX, this.pointerY)) return;
        Row first = null, last = null;
        for (Row row : this.children()) {
            if (row.model() == this.dragModel) {
                if (first == null) first = row;
                last = row;
            }
        }
        if (first == null || this.pointerY < first.getContentY() - ROW_HEIGHT || this.pointerY >= blockBottom(last)) return;
        this.dropIndex = this.dragModel.elements.size();
        for (Row row : this.children()) {
            if (row.model() == this.dragModel && this.pointerY < row.getContentY() + HEADER_BTN / 2.0) {
                this.dropIndex = row.index();
                break;
            }
        }
    }

    private int blockBottom(Row header) {
        int end = header.getContentY() + ROW_HEIGHT;
        int start = this.children().indexOf(header);
        for (int i = start + 1; i < this.children().size(); i++) {
            Row row = this.children().get(i);
            if (row.depth() <= header.depth()) break;
            end = row.getContentY() + ROW_HEIGHT;
        }
        return end;
    }

    private void renderDropTarget(GuiGraphicsExtractor graphics) {
        if (!this.dragging) return;
        graphics.enableScissor(getRowLeft(), getY(), getRowLeft() + getRowWidth(), getBottom());
        int line = -1;
        for (Row row : this.children()) {
            if (row.model() != this.dragModel) continue;
            if (row.model().elements.get(row.index()) == this.dragElement) {
                graphics.fill(getRowLeft(), row.getContentY() - 2, getRowLeft() + getRowWidth(), row.getContentY() + 20, 0x4066BBFF);
            }
            if (row.index() == this.dropIndex) line = row.getContentY() - 2;
            else if (this.dropIndex == this.dragModel.elements.size()) line = blockBottom(row) - 2;
        }
        if (this.dropIndex >= 0 && line >= getY() && line <= getBottom()) {
            graphics.fill(getRowLeft(), line, getRowLeft() + getRowWidth(), line + 2, DRAG_COLOR);
        }
        graphics.disableScissor();
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.isEscape() && hasItemDrag()) {
            cancelItemDrag();
            return true;
        }
        Row row = this.getFocused();
        if (row != null && event.key() == InputConstants.KEY_F10 && event.hasShiftDown()) {
            row.openContext(getRowLeft(), row.getContentY() + HEADER_BTN);
            return true;
        }
        if (row != null && row.canDrag() && row.getFocused() == row.handle() && event.hasAltDown()
                && (event.isUp() || event.isDown())) {
            int next = row.index() + (event.isUp() ? -1 : 1);
            if (next >= 0 && next < row.model().elements.size()) {
                move(row.model(), row.index(), next);
                for (Row moved : this.children()) {
                    if (moved.model() == row.model() && moved.index() == next) {
                        this.setFocused(moved);
                        moved.setFocused(moved.handle());
                        scrollToEntry(moved);
                        break;
                    }
                }
            }
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    protected int scrollBarX() {
        return this.getRowLeft() + this.getRowWidth() + 8;
    }

    @Override
    public int getRowWidth() {
        return Math.min(MAX_ROW_WIDTH, this.width - SIDE_PADDING);
    }

    private int widgetWidth() {
        return Math.max(80, getRowWidth() * 3 / 7);
    }

    private void markDirty() {
        if (this.screen instanceof AutoConfigScreen s) {
            s.markDirty();
        }
    }

    public boolean isPickerDragging() {
        for (Row row : this.children()) {
            ColorWidget picker = colorPickerOf(row);
            if (picker != null && picker.isDraggingPicker()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent mouseButtonEvent, double dx, double dy) {
        if (hasItemDrag()) {
            this.pointerX = mouseButtonEvent.x();
            this.pointerY = mouseButtonEvent.y();
            if (Math.hypot(this.pointerX - this.startX, this.pointerY - this.startY) >= 4) this.dragging = true;
            if (this.dragging) updateDropTarget();
            return true;
        }
        for (Row row : this.children()) {
            ColorWidget picker = colorPickerOf(row);
            if (picker != null && picker.isDraggingPicker()) {
                return picker.mouseDragged(mouseButtonEvent, dx, dy);
            }
        }
        return super.mouseDragged(mouseButtonEvent, dx, dy);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double horizontal, double vertical) {
        if (!hasItemDrag()) return super.mouseScrolled(x, y, horizontal, vertical);
        this.pointerX = x;
        this.pointerY = y;
        super.mouseScrolled(x, y, horizontal, vertical);
        if (this.dragging) updateDropTarget();
        return true;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent mouseButtonEvent) {
        if (hasItemDrag()) {
            this.pointerX = mouseButtonEvent.x();
            this.pointerY = mouseButtonEvent.y();
            updateDropTarget();
            ListModel model = this.dragModel;
            int from = model.elements.indexOf(this.dragElement);
            int to = this.dropIndex > from ? this.dropIndex - 1 : this.dropIndex;
            boolean apply = this.dragging && from >= 0 && this.dropIndex >= 0;
            cancelItemDrag();
            if (apply) move(model, from, to);
            return true;
        }
        boolean handled = false;
        for (Row row : this.children()) {
            ColorWidget picker = colorPickerOf(row);
            if (picker != null && picker.isDraggingPicker()) {
                handled |= picker.mouseReleased(mouseButtonEvent);
            }
        }
        return handled || super.mouseReleased(mouseButtonEvent);
    }

    public boolean isPopupOpenAt(final double x, final double y) {
        for (Row row : this.children()) {
            ColorWidget picker = colorPickerOf(row);
            if (picker != null && picker.isOverPicker(x, y)) {
                return true;
            }
        }
        return false;
    }

    public void closePickersExceptSwatchAt(final double x, final double y) {
        for (Row row : this.children()) {
            ColorWidget picker = colorPickerOf(row);
            if (picker != null && picker.pickerOpen() && !picker.isOverSwatch(x, y)) {
                picker.closePicker();
            }
        }
    }

    private static ColorWidget colorPickerOf(final Row row) {
        if (row instanceof WidgetRow wr && wr.node.widget instanceof ColorWidget cw) {
            return cw;
        }
        if (row instanceof ScalarElementRow sr && sr.widget instanceof ColorWidget cw) {
            return cw;
        }
        return null;
    }

    public void addNode(final Node node) {
        this.nodes.add(node);
        rebuild();
    }

    void rebuild() {
        final double scroll = this.scrollAmount();
        this.clearEntries();
        for (Node n : this.nodes) {
            addNode(n, 0, false, 0);
        }
        this.setScrollAmount(scroll);
    }

    private boolean matches(Component text) {
        return text != null && text.getString().toLowerCase(Locale.ROOT).contains(this.filter);
    }

    private boolean matches(Node node) {
        if (this.filter.isEmpty() || matches(node.label) || matches(node.tooltip)) return true;
        if (node instanceof GroupNode group) return group.children.stream().anyMatch(this::matches);
        if (node instanceof ListNode list) return list.model.elements.stream().anyMatch(this::matches);
        return false;
    }

    private boolean matches(Element element) {
        if (element instanceof ObjectElement object) return matches(object.group);
        return element instanceof ScalarElement scalar
                && scalar.value.toLowerCase(Locale.ROOT).contains(this.filter);
    }

    private void addNode(final Node node, final int depth, final boolean showAll, final int inset) {
        if (!showAll && !matches(node)) return;
        boolean showChildren = showAll || matches(node.label);
        if (node instanceof WidgetNode widget) {
            addRow(new WidgetRow(widget, depth), inset);
        } else if (node instanceof GroupNode group) {
            addRow(new HeaderRow(group, depth), inset);
            if (group.expanded || !this.filter.isEmpty()) {
                for (Node child : group.children) addNode(child, depth + 1, showChildren, inset);
            }
        } else if (node instanceof ListNode list) {
            addRow(new ListHeaderRow(list, depth), inset);
            if (list.model.expanded || !this.filter.isEmpty()) {
                for (int i = 0; i < list.model.elements.size(); i++) {
                    Element element = list.model.elements.get(i);
                    if (!showChildren && !matches(element)) continue;
                    if (element instanceof ScalarElement) {
                        addRow(new ScalarElementRow(list.model, i, depth + 1), inset);
                    } else if (element instanceof ObjectElement object) {
                        addRow(new ObjectElementRow(list.model, i, depth + 1), inset);
                        if (object.group.expanded || !this.filter.isEmpty()) {
                            for (Node child : object.group.children) {
                                addNode(child, depth + 2, showChildren, inset + HANDLE_WIDTH + HANDLE_GAP);
                            }
                        }
                    }
                }
            }
        }
    }

    private void addRow(Row row, int inset) {
        row.inset = inset;
        this.addEntry(row);
    }

    private List<AutoConfigScreen.RowAction> nodeActions(Node node) {
        List<AutoConfigScreen.RowAction> actions = new ArrayList<>();
        if (this.readOnly) return actions;
        if (node.resetAction != null) actions.add(new AutoConfigScreen.RowAction(
                "text.simple_config_lib.reset_value", node.resetAction, true));
        if (node.manualAction != null) {
            EditBox input = node instanceof WidgetNode widget && widget.widget instanceof EditBox box ? box : null;
            boolean manual = input != null;
            boolean valid = !manual || AutoConfigScreen.parseElement(Integer.class, input.getValue()) != null;
            actions.add(new AutoConfigScreen.RowAction(manual ? "text.simple_config_lib.restore_slider"
                    : "text.simple_config_lib.enter_value", node.manualAction, valid));
        }
        return actions;
    }

    private List<AutoConfigScreen.RowAction> elementActions(ListModel model, int index) {
        List<AutoConfigScreen.RowAction> actions = new ArrayList<>();
        if (this.readOnly) return actions;
        if (model.sliderCfg != null && model.elements.get(index) instanceof ScalarElement scalar) {
            boolean manual = scalar.manualInput;
            boolean valid = !manual || AutoConfigScreen.parseElement(Integer.class, scalar.value) != null;
            actions.add(new AutoConfigScreen.RowAction(manual ? "text.simple_config_lib.restore_slider"
                    : "text.simple_config_lib.enter_value", () -> {
                commitScalarElements();
                if (scalar.manualInput && AutoConfigScreen.parseElement(Integer.class, scalar.value) == null) return;
                scalar.manualInput = !scalar.manualInput;
                rebuild();
                for (Row row : this.children()) {
                    if (row.model() == model && row.index() == index && row instanceof ScalarElementRow number) {
                        focusWidget(number.widget);
                        if (number.widget instanceof EditBox box) {
                            box.setCursorPosition(box.getValue().length());
                            box.setHighlightPos(0);
                        }
                    }
                }
            }, valid));
        }
        actions.add(new AutoConfigScreen.RowAction("text.simple_config_lib.reset_value", () -> resetElement(model, index), true));
        if (!model.editable) return actions;
        actions.add(new AutoConfigScreen.RowAction("text.simple_config_lib.add_before", () -> addElement(model, index), true));
        actions.add(new AutoConfigScreen.RowAction("text.simple_config_lib.add_after", () -> addElement(model, index + 1), true));
        actions.add(new AutoConfigScreen.RowAction("text.simple_config_lib.remove", () -> {
            commitScalarElements();
            model.elements.remove(index);
            rebuild();
            markDirty();
        }, true));
        return actions;
    }

    private void resetElement(ListModel model, int index) {
        commitScalarElements();
        ViewState state = viewState();
        Element current = model.elements.get(index);
        Supplier<Element> factory = current.resetFactory != null ? current.resetFactory : model.factory;
        Element replacement = factory.get();
        replacement.resetFactory = factory;
        model.elements.set(index, replacement);
        restoreViewState(state);
        restoreScroll();
        markDirty();
    }
    private void addElement(ListModel model, int index) {
        commitScalarElements();
        model.elements.add(index, model.factory.get());
        model.expanded = true;
        rebuild();
        markDirty();
    }

    private void move(ListModel model, int from, int to) {
        if (this.readOnly || !this.filter.isEmpty() || !model.editable || from == to
                || from < 0 || to < 0 || from >= model.elements.size() || to >= model.elements.size()) return;
        commitScalarElements();
        model.elements.add(to, model.elements.remove(from));
        rebuild();
        markDirty();
    }

    public void commitElements() {
        commitScalarElements();
        visitNodes(this.nodes, "", (path, node) -> {
            if (node instanceof ListNode list && list.model.applyAction != null) list.model.applyAction.run();
        });
    }

    private void commitScalarElements() {
        for (Row row : this.children()) {
            if (row instanceof ScalarElementRow el) {
                el.commit();
            }
        }
    }

    private static Style styleUnderCursor(Font font, FormattedCharSequence text, int leftX, int topY, int mouseX, int mouseY) {
        GuiTextRenderState state = new GuiTextRenderState(font, text, new Matrix3x2f(), leftX, topY, ARGB.white(1.0F), 0, true, true, null);
        Style[] found = new Style[1];
        ActiveTextCollector.findElementUnderCursor(state, mouseX, mouseY, s -> found[0] = s);
        return found[0];
    }

    public abstract static class Node {
        public Component label;
        public Component tooltip;
        public Runnable resetAction;
        public Runnable manualAction;
    }

    public static final class WidgetNode extends Node {
        public AbstractWidget widget;
    }

    public static final class GroupNode extends Node {
        public final List<Node> children = new ArrayList<>();
        public boolean expanded;
    }

    public static final class ListNode extends Node {
        public ListModel model;
    }

    public static final class ListModel {
        Runnable applyAction;
        public boolean editable;
        public boolean expanded;
        public final boolean addToFront;
        public Slider sliderCfg;
        public Color colorCfg;
        public final Class<?> type;
        public int textMaxLength = 256;

        public final List<Element> elements = new ArrayList<>();
        public final Supplier<Element> factory;

        public ListModel(final List<Element> initial, final boolean editable, final boolean expanded,
                         final boolean addToFront, final Class<?> type, final Supplier<Element> factory) {
            this.elements.addAll(initial);
            this.editable = editable;
            this.expanded = expanded;
            this.addToFront = addToFront;
            this.factory = factory;
            this.type = type;
        }
    }

    public abstract static class Element {
        public Supplier<Element> resetFactory;
    }

    public static final class ScalarElement extends Element {
        public String value;
        public boolean manualInput;
        public Component component;

        public ScalarElement(final String value) {
            this.value = value;
        }

        public ScalarElement(final Component component) {
            this.component = component;
            this.value = component.getString();
        }
    }

    public static final class ObjectElement extends Element {
        public final GroupNode group;
        public final Object value;

        public ObjectElement(final GroupNode group) {
            this(group, null);
        }

        ObjectElement(final GroupNode group, final Object value) {
            this.group = group;
            this.value = value;
        }
    }

    public abstract class Row extends ContainerObjectSelectionList.Entry<Row> {
        private DragHandle dragHandle;
        private int inset;

        int indent() {
            return depth() * INDENT + this.inset;
        }

        protected abstract List<AutoConfigScreen.RowAction> actions();
        protected abstract int depth();
        protected ListModel model() { return null; }
        protected int index() { return -1; }

        boolean canDrag() {
            return !AutoConfigList.this.readOnly && AutoConfigList.this.filter.isEmpty()
                    && model() != null && model().editable;
        }

        DragHandle handle() {
            if (this.dragHandle == null) this.dragHandle = new DragHandle();
            return this.dragHandle;
        }

        boolean overHandle(double x) {
            int left = AutoConfigList.this.getRowLeft() + indent();
            return x >= left && x < left + HANDLE_WIDTH;
        }

        void openContext(int x, int y) {
            if (AutoConfigList.this.screen instanceof AutoConfigScreen configScreen) {
                configScreen.openActions(actions(), x, y);
            }
        }

        protected List<GuiEventListener> rowWidgets(GuiEventListener... widgets) {
            List<GuiEventListener> result = new ArrayList<>(List.of(widgets));
            if (canDrag()) result.add(handle());
            return result;
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return children().stream().filter(NarratableEntry.class::isInstance).map(NarratableEntry.class::cast).toList();
        }

        void renderHandle(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            if (!canDrag()) return;
            handle().setPosition(AutoConfigList.this.getRowLeft() + indent(), getContentY());
            if (getContentY() >= AutoConfigList.this.getY() && getContentY() + HEADER_BTN <= AutoConfigList.this.getBottom()) {
                handle().extractRenderState(graphics, mouseX, mouseY, partialTick);
            }
        }

        private final class DragHandle extends AbstractWidget {
            DragHandle() {
                super(0, 0, HANDLE_WIDTH, HEADER_BTN, Component.translatable("text.simple_config_lib.drag"));
            }

            @Override
            protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
                graphics.text(AutoConfigList.this.font, Component.literal("⋮"), getX() + 4, getY() + 6,
                        isFocused() || isMouseOver(mouseX, mouseY) ? DRAG_COLOR : 0xFF999999);
            }

            @Override
            protected void updateWidgetNarration(NarrationElementOutput output) {
                output.add(NarratedElementType.TITLE, getMessage());
            }
        }
    }

    private final class WidgetRow extends Row {
        @Override
        protected int depth() { return this.depth; }

        @Override
        protected List<AutoConfigScreen.RowAction> actions() {
            return nodeActions(this.node);
        }

        private final WidgetNode node;
        private final int depth;

        WidgetRow(final WidgetNode node, final int depth) {
            this.node = node;
            this.depth = depth;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
            int rowLeft = AutoConfigList.this.getRowLeft();
            int rowWidth = AutoConfigList.this.getRowWidth();
            int y = this.getContentY();
            int labelX = rowLeft + indent();

            int widgetW = AutoConfigList.this.widgetWidth();
            int widgetX = rowLeft + rowWidth - widgetW;

            int LABEL_GAP = 4;
            int labelRight = widgetX - LABEL_GAP;
            graphics.enableScissor(labelX, y, labelRight, y + 20);
            graphics.text(AutoConfigList.this.font, this.node.label.getVisualOrderText(), labelX, y + 6, LABEL_COLOR);
            graphics.disableScissor();

            this.node.widget.setPosition(widgetX, y);
            this.node.widget.setWidth(widgetW);
            if (AutoConfigList.this.readOnly) {
                this.node.widget.active = false;
            }
            this.node.widget.extractRenderState(graphics, mouseX, mouseY, partialTick);

            int labelWidth = AutoConfigList.this.font.width(this.node.label);
            boolean overLabel = mouseX >= labelX && mouseX <= labelX + labelWidth
                    && mouseY >= y && mouseY <= y + 20;

            if (overLabel) {
                Style style = styleUnderCursor(AutoConfigList.this.font,
                        this.node.label.getVisualOrderText(), labelX, y + 6, mouseX, mouseY);

                FormattedText tooltipText = style != null && style.getHoverEvent() instanceof HoverEvent.ShowText(Component value)
                        ? value
                        : this.node.tooltip;

                if (tooltipText != null) {
                    AutoConfigList.this.pendingTooltip = tooltipText;
                    AutoConfigList.this.pendingTooltipX = mouseX;
                    AutoConfigList.this.pendingTooltipY = mouseY;
                }
            }
        }

        @Override
        public boolean isMouseOver(final double mx, final double my) {
            if (this.node.widget instanceof ColorWidget cw && cw.isOverPicker(mx, my)) {
                return true;
            }
            return super.isMouseOver(mx, my);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean doubleClick) {
            if (this.node.widget instanceof ColorWidget cw && cw.isOverPicker(mouseButtonEvent.x(), mouseButtonEvent.y())) {
                return cw.mouseClicked(mouseButtonEvent, doubleClick);
            }

            int rowLeft = AutoConfigList.this.getRowLeft();
            int y = this.getContentY();
            int labelX = rowLeft + indent();
            int labelWidth = AutoConfigList.this.font.width(this.node.label);

            boolean overLabel = mouseButtonEvent.x() >= labelX && mouseButtonEvent.x() <= labelX + labelWidth
                    && mouseButtonEvent.y() >= y && mouseButtonEvent.y() <= y + 20;

            if (mouseButtonEvent.button() == 1 && overLabel) {
                Style style = styleUnderCursor(AutoConfigList.this.font, this.node.label.getVisualOrderText(),
                        labelX, y + 6, (int) mouseButtonEvent.x(), (int) mouseButtonEvent.y());

                if (style != null && style.getClickEvent() != null) {
                    GuiSounds.playClick();
                    Screen.defaultHandleClickEvent(style.getClickEvent(), Minecraft.getInstance(), screen);
                    return true;
                }
            }

            return super.mouseClicked(mouseButtonEvent, doubleClick);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return rowWidgets(this.node.widget);
        }
    }

    private class HeaderRow extends Row {
        protected final Node node;
        private final int depth;
        private final Button toggleButton;

        HeaderRow(final Node node, final int depth) {
            this.node = node;
            this.depth = depth;
            this.toggleButton = Button.builder(toggleLabel(expanded()), button -> toggle())
                    .size(HEADER_BTN, HEADER_BTN).build();
        }

        @Override
        protected int depth() { return this.depth; }

        @Override
        protected List<AutoConfigScreen.RowAction> actions() {
            return nodeActions(this.node);
        }

        private boolean expanded() {
            return this.node instanceof GroupNode group ? group.expanded : ((ListNode) this.node).model.expanded;
        }

        private void toggle() {
            commitScalarElements();
            if (this.node instanceof GroupNode group) group.expanded = !group.expanded;
            else if (this.node instanceof ListNode list) list.model.expanded = !list.model.expanded;
            rebuild();
        }

        private int labelX() {
            return getRowLeft() + indent() + (model() != null ? HANDLE_WIDTH + HANDLE_GAP : 0);
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
            int y = getContentY();
            int labelX = labelX();
            graphics.text(AutoConfigList.this.font, this.node.label.getVisualOrderText(), labelX, y + 6, LABEL_COLOR);
            this.toggleButton.setPosition(getRowLeft() + getRowWidth() - HEADER_BTN, y);
            this.toggleButton.extractRenderState(graphics, mouseX, mouseY, partialTick);

            if (this.node.tooltip != null && mouseX >= labelX
                    && mouseX <= labelX + AutoConfigList.this.font.width(this.node.label)
                    && mouseY >= y && mouseY <= y + HEADER_BTN) {
                graphics.setTooltipForNextFrame(AutoConfigList.this.font,
                        AutoConfigList.this.font.split(this.node.tooltip, 200), mouseX, mouseY);
            }
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            if (super.mouseClicked(event, doubleClick)) return true;
            int left = model() != null ? labelX() : getRowLeft();
            if (event.button() == InputConstants.MOUSE_BUTTON_LEFT
                    && event.x() >= left && event.x() < getRowLeft() + getRowWidth() - HEADER_BTN
                    && event.y() >= getContentY() && event.y() < getContentY() + HEADER_BTN) {
                GuiSounds.playClick();
                toggle();
                return true;
            }
            return false;
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return rowWidgets(this.toggleButton);
        }
    }

    private final class ListHeaderRow extends HeaderRow {
        ListHeaderRow(final ListNode node, final int depth) {
            super(node, depth);
        }

        @Override
        protected List<AutoConfigScreen.RowAction> actions() {
            ListNode list = (ListNode) this.node;
            List<AutoConfigScreen.RowAction> result = nodeActions(list);
            if (!AutoConfigList.this.readOnly && list.model.editable) result.add(
                    new AutoConfigScreen.RowAction("text.simple_config_lib.add", () -> addElement(list.model,
                            list.model.addToFront ? 0 : list.model.elements.size()), true));
            return result;
        }
    }

    private final class ScalarElementRow extends Row {
        @Override
        protected int depth() { return this.depth; }

        @Override
        protected ListModel model() { return this.model; }

        @Override
        protected int index() { return this.index; }

        @Override
        protected List<AutoConfigScreen.RowAction> actions() {
            return elementActions(this.model, this.index);
        }

        private final ListModel model;
        private final int index;
        private final int depth;
        private final ScalarElement element;
        private final AbstractWidget widget;
        private Component componentLabel;

        ScalarElementRow(final ListModel model, final int index, final int depth) {
            this.model = model;
            this.index = index;
            this.depth = depth;
            this.element = (ScalarElement) model.elements.get(index);
            int pad = depth * INDENT + HANDLE_WIDTH + HANDLE_GAP;
            String value = this.element.value;

            if (model.type == Component.class) {
                this.componentLabel = this.element.component != null ? this.element.component : Component.literal(value);
                this.widget = new StringWidget(0, 0, getRowWidth() - pad, 20,
                        Component.empty(), AutoConfigList.this.font);
            } else if (model.type == Boolean.class) {
                this.widget = CycleButton.onOffBuilder(Boolean.parseBoolean(value))
                        .displayOnlyValue()
                        .create(0, 0, getRowWidth() - pad, 20, Component.empty(),
                                (button, next) -> setValue(String.valueOf(next)));
            } else if (model.sliderCfg != null && !this.element.manualInput) {
                Slider slider = model.sliderCfg;
                int initial = value.isEmpty() ? slider.min() : Integer.parseInt(value.trim());
                this.widget = new AutoConfigScreen.IntSlider(slider.min(), slider.max(), slider.step(), initial,
                        next -> setValue(String.valueOf(next)));
            } else if (model.colorCfg != null) {
                int initial = value.isEmpty() ? 0 : (int) Long.parseLong(value.trim());
                this.element.value = String.valueOf(initial);
                this.widget = new ColorWidget(AutoConfigList.this.font, initial, model.colorCfg.alpha(),
                        next -> setValue(String.valueOf(next)));
            } else {
                EditBox box = new EditBox(AutoConfigList.this.font, 0, 0,
                        getRowWidth() - pad, 20, Component.empty());
                box.setMaxLength(Math.max(model.textMaxLength, value.length()));
                box.setValue(value);
                box.setResponder(next -> {
                    if (model.type != String.class) {
                        box.setTextColor(AutoConfigScreen.parseElement(model.type, next) == null ? 0xFFFF5555 : 0xFFE0E0E0);
                    }
                    setValue(next);
                });
                this.widget = box;
            }
        }

        private void setValue(String value) {
            this.element.value = value;
            markDirty();
        }

        void commit() {
            if (this.widget instanceof EditBox editBox) this.element.value = editBox.getValue();
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
            int rowLeft = AutoConfigList.this.getRowLeft();
            int rowWidth = AutoConfigList.this.getRowWidth();
            int y = this.getContentY();
            int boxX = rowLeft + indent() + HANDLE_WIDTH + HANDLE_GAP;
            int boxW = rowLeft + rowWidth - boxX;

            this.widget.setPosition(boxX, y);
            this.widget.setWidth(boxW);
            if (AutoConfigList.this.readOnly) {
                this.widget.active = false;
            }
            this.widget.extractRenderState(graphics, mouseX, mouseY, partialTick);

            if (this.componentLabel != null) {
                graphics.text(AutoConfigList.this.font,
                        this.componentLabel.getVisualOrderText(), boxX, y + 6, LABEL_COLOR);

                int labelW = AutoConfigList.this.font.width(this.componentLabel);
                if (mouseX >= boxX && mouseX <= boxX + labelW && mouseY >= y && mouseY <= y + 20) {
                    Style style = styleUnderCursor(AutoConfigList.this.font,
                            this.componentLabel.getVisualOrderText(), boxX, y + 6, mouseX, mouseY);

                    Component tooltipValue = style != null && style.getHoverEvent() instanceof HoverEvent.ShowText(Component value)
                            ? value
                            : null;

                    if (tooltipValue != null) {
                        AutoConfigList.this.pendingTooltip = tooltipValue;
                        AutoConfigList.this.pendingTooltipX = mouseX;
                        AutoConfigList.this.pendingTooltipY = mouseY;
                    }
                }
            }
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean doubleClick) {
            if (this.widget instanceof ColorWidget cw && cw.isOverPicker(mouseButtonEvent.x(), mouseButtonEvent.y())) {
                return cw.mouseClicked(mouseButtonEvent, doubleClick);
            }
            if (this.componentLabel != null) {
                int boxX = AutoConfigList.this.getRowLeft() + indent() + HANDLE_WIDTH + HANDLE_GAP;
                int y = this.getContentY();
                int labelW = AutoConfigList.this.font.width(this.componentLabel);
                if (mouseButtonEvent.x() >= boxX && mouseButtonEvent.x() <= boxX + labelW && mouseButtonEvent.y() >= y && mouseButtonEvent.y() <= y + 20) {
                    Style style = styleUnderCursor(AutoConfigList.this.font, this.componentLabel.getVisualOrderText(),
                            boxX, y + 6, (int) mouseButtonEvent.x(), (int) mouseButtonEvent.y());

                    if (style != null && style.getClickEvent() != null) {
                        GuiSounds.playClick();
                        Screen.defaultHandleClickEvent(style.getClickEvent(), Minecraft.getInstance(), screen);
                        return true;
                    }
                }
            }

            return super.mouseClicked(mouseButtonEvent, doubleClick);
        }

        @Override
        public boolean isMouseOver(final double mx, final double my) {
            if (this.widget instanceof ColorWidget cw && cw.isOverPicker(mx, my)) {
                return true;
            }
            return super.isMouseOver(mx, my);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return rowWidgets(this.widget);
        }
    }

    private final class ObjectElementRow extends HeaderRow {
        private final ListModel model;
        private final int index;

        ObjectElementRow(final ListModel model, final int index, final int depth) {
            super(((ObjectElement) model.elements.get(index)).group, depth);
            this.model = model;
            this.index = index;
        }

        @Override
        protected ListModel model() { return this.model; }

        @Override
        protected int index() { return this.index; }

        @Override
        protected List<AutoConfigScreen.RowAction> actions() {
            return elementActions(this.model, this.index);
        }
    }

    private static Component toggleLabel(final boolean expanded) {
        return Component.literal(expanded ? "▼" : "▶");
    }
}
