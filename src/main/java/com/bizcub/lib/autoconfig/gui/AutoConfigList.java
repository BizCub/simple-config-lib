package com.bizcub.lib.autoconfig.gui;

import com.bizcub.lib.autoconfig.annotation.Color;
import com.bizcub.lib.autoconfig.annotation.Slider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

//? >=1.21.9 {
/*import net.minecraft.client.input.MouseButtonEvent;*///?}

public class AutoConfigList extends ContainerObjectSelectionList<AutoConfigList.Row> {

    public static final int WIDGET_WIDTH = 150;
    private static final int ROW_HEIGHT = 25;
    private static final int LABEL_COLOR = -1;
    private static final int SELECTED_BG = 0x80FFFFFF;

    private static final int INDENT = 12;
    private static final int HEADER_BTN = 20;
    private static final int HEADER_BTN_GAP = 4;

    protected final Screen screen;
    private final Font font;

    private final List<Node> nodes = new ArrayList<>();

    public AutoConfigList(final Minecraft minecraft, final int width, final int height, final int y, final Screen screen) {
        super(minecraft, width, height, y, ROW_HEIGHT);
        this.screen = screen;
        this.font = minecraft.font;
    }

    @Override
    public void renderWidget(final GuiGraphics graphics,
                                         final int mouseX, final int mouseY, final float a) {
        boolean overPopup = isPopupOpenAt(mouseX, mouseY);
        int hoverX = overPopup ? Integer.MIN_VALUE : mouseX;
        int hoverY = overPopup ? Integer.MIN_VALUE : mouseY;

        super.renderWidget(graphics, hoverX, hoverY, a);

        for (Row row : this.children()) {
            ColorWidget picker = colorPickerOf(row);
            if (picker != null && picker.pickerOpen()) {
                picker.renderPicker(graphics);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (Row row : this.children()) {
            ColorWidget picker = colorPickerOf(row);
            //~ mb_event
            if (picker != null && picker.isOverPicker(mouseX, mouseY)) { //~ !mb_event
                return row.mouseClicked(mouseX, mouseY, button);
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public int getRowWidth() {
        return 310;
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
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        for (Row row : this.children()) {
            ColorWidget picker = colorPickerOf(row);
            if (picker != null && picker.isDraggingPicker()) {
                return picker.mouseDragged(mouseX, mouseY, button, dx, dy);
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean handled = false;
        for (Row row : this.children()) {
            ColorWidget picker = colorPickerOf(row);
            if (picker != null && picker.isDraggingPicker()) {
                handled |= picker.mouseReleased(mouseX, mouseY, button);
            }
        }
        return handled || super.mouseReleased(mouseX, mouseY, button);
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
        commitScalarElements();
        this.clearEntries();
        for (Node n : this.nodes) {
            addNode(n, 0);
        }
        this.setScrollAmount(scroll);
    }

    private void addNode(final Node node, final int depth) {
        if (node instanceof WidgetNode w) {
            this.addEntry(new WidgetRow(w, depth));
        } else if (node instanceof GroupNode g) {
            this.addEntry(new GroupHeaderRow(g, depth));
            if (g.expanded) {
                for (Node c : g.children) {
                    addNode(c, depth + 1);
                }
            }
        } else if (node instanceof ListNode l) {
            this.addEntry(new ListHeaderRow(l, depth));
            if (l.model.expanded) {
                for (int i = 0; i < l.model.elements.size(); i++) {
                    Element e = l.model.elements.get(i);
                    if (e instanceof ScalarElement) {
                        this.addEntry(new ScalarElementRow(l.model, i, depth + 1));
                    } else if (e instanceof ObjectElement oe) {
                        this.addEntry(new ObjectElementRow(l.model, i, depth + 1));
                        if (oe.group.expanded) {
                            for (Node c : oe.group.children) {
                                addNode(c, depth + 2);
                            }
                        }
                    }
                }
            }
        }
    }

    public void commitElements() {
        commitScalarElements();
    }

    private void commitScalarElements() {
        for (Row row : this.children()) {
            if (row instanceof ScalarElementRow el) {
                el.commit();
            }
        }
    }

    private void notifySelectionChanged() {
        for (Row row : this.children()) {
            if (row instanceof ListHeaderRow header) {
                header.refreshButtons();
            }
        }
    }

    public abstract static class Node {
        public Component label;
        public Component tooltip;
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
        public boolean editable;
        public boolean expanded;
        public final boolean addToFront;
        public Slider sliderCfg;
        public Color colorCfg;
        public int selectedIndex = -1;
        public Class<?> type;

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

    public interface Element {}

    public static final class ScalarElement implements Element {
        public String value;
        public Component component;

        public ScalarElement(final String value) {
            this.value = value;
        }

        public ScalarElement(final Component component) {
            this.component = component;
            this.value = component.getString();
        }
    }

    public static final class ObjectElement implements Element {
        public final GroupNode group;

        public ObjectElement(final GroupNode group) {
            this.group = group;
        }
    }

    public abstract static class Row extends ContainerObjectSelectionList.Entry<Row> {
        protected int lastTop;
    }

    private final class WidgetRow extends Row {
        private final WidgetNode node;
        private final int depth;

        WidgetRow(final WidgetNode node, final int depth) {
            this.node = node;
            this.depth = depth;
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            //? <=1.21.8
            this.lastTop = top;
            int rowLeft = AutoConfigList.this.getRowLeft();
            int rowWidth = AutoConfigList.this.getRowWidth();
            int y = this.lastTop;
            int labelX = rowLeft + this.depth * INDENT;

            graphics.drawString(AutoConfigList.this.font, this.node.label.getVisualOrderText(), labelX, y + 6, LABEL_COLOR);

            int widgetX = rowLeft + rowWidth - WIDGET_WIDTH;
            this.node.widget.setPosition(widgetX, y);
            this.node.widget.setWidth(WIDGET_WIDTH);
            this.node.widget.render(graphics, mouseX, mouseY, partialTick);

            int labelWidth = AutoConfigList.this.font.width(this.node.label);
            boolean overLabel = mouseX >= labelX && mouseX <= labelX + labelWidth
                    && mouseY >= y && mouseY <= y + 20;

            if (overLabel) {
                Style style = this.node.label.getStyle();
                //? >=1.21.5 {
                /*FormattedText tooltipText = style.getHoverEvent() instanceof HoverEvent.ShowText(Component value)
                        ? value
                        : this.node.tooltip;

                *///?} else {
                HoverEvent hover = style.getHoverEvent();
                Component value = hover != null ? hover.getValue(HoverEvent.Action.SHOW_TEXT) : null;
                FormattedText tooltipText = value != null ? value : this.node.tooltip;//?}

                if (tooltipText != null) {
                    graphics.renderTooltip(
                            AutoConfigList.this.font,
                            AutoConfigList.this.font.split(tooltipText, 200),
                            mouseX,
                            mouseY
                    );
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
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            //~ mb_event
            if (this.node.widget instanceof ColorWidget cw && cw.isOverPicker(mouseX, mouseY)) { //~ !mb_event
                return cw.mouseClicked(mouseX, mouseY, button);
            }

            int rowLeft = AutoConfigList.this.getRowLeft();
            int y = this.lastTop;
            int labelX = rowLeft + this.depth * INDENT;
            int labelWidth = AutoConfigList.this.font.width(this.node.label);

            //~ mb_event
            boolean overLabel = mouseX >= labelX && mouseX <= labelX + labelWidth
                    && mouseY >= y && mouseY <= y + 20; //~ !mb_event

            if (overLabel) {
                Style style = this.node.label.getStyle();
                if (style.getClickEvent() != null) {
                    screen.handleComponentClicked(style);
                    return true;
                }
            }

            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.of(this.node.widget);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of(this.node.widget);
        }
    }

    private final class GroupHeaderRow extends Row {
        private final GroupNode node;
        private final int depth;
        private final Button toggleButton;

        GroupHeaderRow(final GroupNode node, final int depth) {
            this.node = node;
            this.depth = depth;
            this.toggleButton = Button.builder(toggleLabel(node.expanded), b -> toggle())
                    .size(HEADER_BTN, HEADER_BTN).build();
        }

        private void toggle() {
            AutoConfigList.this.commitScalarElements();
            this.node.expanded = !this.node.expanded;
            this.toggleButton.setMessage(toggleLabel(this.node.expanded));
            AutoConfigList.this.rebuild();
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            //? <=1.21.8
            this.lastTop = top;
            int rowLeft = AutoConfigList.this.getRowLeft();
            int rowWidth = AutoConfigList.this.getRowWidth();
            int y = this.lastTop;
            int labelX = rowLeft + this.depth * INDENT;

            graphics.drawString(AutoConfigList.this.font, this.node.label.getVisualOrderText(), labelX, y + 6, LABEL_COLOR);

            int toggleX = rowLeft + rowWidth - HEADER_BTN;
            this.toggleButton.setPosition(toggleX, y);
            this.toggleButton.render(graphics, mouseX, mouseY, partialTick);

            if (this.node.tooltip != null) {
                int labelWidth = AutoConfigList.this.font.width(this.node.label);
                if (mouseX >= labelX && mouseX <= labelX + labelWidth && mouseY >= y && mouseY <= y + 20) {
                    graphics.renderTooltip(
                            AutoConfigList.this.font,
                            AutoConfigList.this.font.split(this.node.tooltip, 200),
                            mouseX, mouseY);
                }
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (super.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
            int rowLeft = AutoConfigList.this.getRowLeft();
            int rowWidth = AutoConfigList.this.getRowWidth();
            int y = this.lastTop;
            int buttonsLeft = rowLeft + rowWidth - HEADER_BTN;
            //~ mb_event
            if (mouseX >= rowLeft && mouseX < buttonsLeft && mouseY >= y && mouseY < y + 20) { //~ !mb_event
                toggle();
                return true;
            }
            return false;
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.of(this.toggleButton);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of(this.toggleButton);
        }
    }

    private final class ListHeaderRow extends Row {
        private final ListNode node;
        private final int depth;
        private final Button addButton;
        private final Button removeButton;
        private final Button toggleButton;

        ListHeaderRow(final ListNode node, final int depth) {
            this.node = node;
            this.depth = depth;
            final ListModel model = node.model;

            this.toggleButton = Button.builder(toggleLabel(model.expanded), b -> toggle())
                    .size(HEADER_BTN, HEADER_BTN).build();

            if (model.editable) {
                this.addButton = Button.builder(Component.literal("+"), b -> {
                    AutoConfigList.this.commitScalarElements();
                    Element created = model.factory.get();
                    if (model.addToFront) {
                        model.elements.add(0, created);
                        if (model.selectedIndex >= 0) model.selectedIndex++;
                    } else {
                        model.elements.add(created);
                    }
                    model.expanded = true;
                    refreshButtons();
                    AutoConfigList.this.markDirty();
                    AutoConfigList.this.rebuild();
                }).size(HEADER_BTN, HEADER_BTN).build();

                this.removeButton = Button.builder(Component.literal("−"), b -> {
                    AutoConfigList.this.commitScalarElements();
                    if (model.expanded && model.selectedIndex >= 0 && model.selectedIndex < model.elements.size()) {
                        model.elements.remove(model.selectedIndex);
                        model.selectedIndex = -1;
                        refreshButtons();
                        AutoConfigList.this.markDirty();
                        AutoConfigList.this.rebuild();
                    }
                }).size(HEADER_BTN, HEADER_BTN).build();
            } else {
                this.addButton = null;
                this.removeButton = null;
            }

            refreshButtons();
        }

        private void toggle() {
            AutoConfigList.this.commitScalarElements();
            this.node.model.expanded = !this.node.model.expanded;
            this.toggleButton.setMessage(toggleLabel(this.node.model.expanded));
            refreshButtons();
            AutoConfigList.this.rebuild();
        }

        void refreshButtons() {
            if (this.removeButton != null) {
                this.removeButton.active = this.node.model.expanded && this.node.model.selectedIndex >= 0;
            }
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            //? <=1.21.8
            this.lastTop = top;
            int rowLeft = AutoConfigList.this.getRowLeft();
            int rowWidth = AutoConfigList.this.getRowWidth();
            int y = this.lastTop;
            int labelX = rowLeft + this.depth * INDENT;

            graphics.drawString(AutoConfigList.this.font, this.node.label.getVisualOrderText(), labelX, y + 6, LABEL_COLOR);

            int right = rowLeft + rowWidth;
            int toggleX = right - HEADER_BTN;
            this.toggleButton.setPosition(toggleX, y);
            this.toggleButton.render(graphics, mouseX, mouseY, partialTick);

            if (this.node.model.editable) {
                int removeX = toggleX - HEADER_BTN - HEADER_BTN_GAP;
                int addX = removeX - HEADER_BTN - HEADER_BTN_GAP;
                this.addButton.setPosition(addX, y);
                this.removeButton.setPosition(removeX, y);
                this.addButton.render(graphics, mouseX, mouseY, partialTick);
                this.removeButton.render(graphics, mouseX, mouseY, partialTick);
            }

            if (this.node.tooltip != null) {
                int labelWidth = AutoConfigList.this.font.width(this.node.label);
                if (mouseX >= labelX && mouseX <= labelX + labelWidth && mouseY >= y && mouseY <= y + 20) {
                    graphics.renderTooltip(
                            AutoConfigList.this.font,
                            AutoConfigList.this.font.split(this.node.tooltip, 200),
                            mouseX, mouseY);
                }
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (super.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
            int rowLeft = AutoConfigList.this.getRowLeft();
            int rowWidth = AutoConfigList.this.getRowWidth();
            int y = this.lastTop;
            int buttonCount = this.node.model.editable ? 3 : 1;
            int buttonsLeft = rowLeft + rowWidth - (buttonCount * HEADER_BTN + (buttonCount - 1) * HEADER_BTN_GAP);
            //~ mb_event
            if (mouseX >= rowLeft && mouseX < buttonsLeft && mouseY >= y && mouseY < y + 20) { //~ !mb_event
                toggle();
                return true;
            }
            return false;
        }

        @Override
        public List<? extends GuiEventListener> children() {
            if (this.node.model.editable) {
                return List.of(this.addButton, this.removeButton, this.toggleButton);
            }
            return List.of(this.toggleButton);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            if (this.node.model.editable) {
                return List.of(this.addButton, this.removeButton, this.toggleButton);
            }
            return List.of(this.toggleButton);
        }
    }

    private final class ScalarElementRow extends Row {
        private final ListModel model;
        private final int index;
        private final int depth;
        private final AbstractWidget widget;
        private Component componentLabel;

        ScalarElementRow(final ListModel model, final int index, final int depth) {
            this.model = model;
            this.index = index;
            this.depth = depth;
            int pad = depth * INDENT;
            ScalarElement e = scalar();
            String value = e != null ? e.value : "";

            if (model.type == Component.class) {
                Component comp = (e != null && e.component != null) ? e.component : Component.literal(value);
                this.componentLabel = comp;
                this.widget = new StringWidget(0, 0, getRowWidth() - pad, 20,
                        Component.empty(), AutoConfigList.this.font);
            } else if (model.type == Boolean.class) {
                boolean initial = Boolean.parseBoolean(value);
                CycleButton<Boolean> cb = CycleButton.onOffBuilder(initial)
                        .displayOnlyValue()
                        .create(0, 0, getRowWidth() - pad, 20, Component.empty(),
                                (b, v) -> {
                                    ScalarElement s = scalar();
                                    if (s != null) {
                                        s.value = String.valueOf(v);
                                        markDirty();
                                    }
                                });
                this.widget = cb;
            } else if (model.sliderCfg != null) {
                Slider r = model.sliderCfg;
                int initial = value.isEmpty() ? r.min() : Integer.parseInt(value.trim());
                AutoConfigScreen.IntSlider slider = new AutoConfigScreen.IntSlider(r.min(), r.max(), r.step(), initial, v -> {
                    ScalarElement s = scalar();
                    if (s != null) {
                        s.value = String.valueOf(v);
                        markDirty();
                    }
                });
                this.widget = slider;
            } else if (model.colorCfg != null) {
                int initial = value.isEmpty() ? 0 : (int) Long.parseLong(value.trim());
                ScalarElement s0 = scalar();
                if (s0 != null) s0.value = String.valueOf(initial);
                ColorWidget cw = new ColorWidget(AutoConfigList.this.font, initial,
                        model.colorCfg.alpha(), v -> {
                    ScalarElement s = scalar();
                    if (s != null) {
                        s.value = String.valueOf(v);
                        markDirty();
                    }
                });
                this.widget = cw;
            } else {
                EditBox eb = new EditBox(AutoConfigList.this.font, 0, 0,
                        getRowWidth() - pad, 20, Component.empty());
                eb.setMaxLength(256);
                eb.setValue(value);
                eb.setResponder(v -> {
                    ScalarElement s = scalar();
                    if (s != null) {
                        s.value = v;
                        markDirty();
                    }
                });
                this.widget = eb;
            }
        }

        private ScalarElement scalar() {
            if (this.index < this.model.elements.size()
                    && this.model.elements.get(this.index) instanceof ScalarElement s) {
                return s;
            }
            return null;
        }

        void commit() {
            ScalarElement s = scalar();
            if (s != null && this.widget instanceof EditBox editBox) {
                s.value = editBox.getValue();
            }
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            //? <=1.21.8
            this.lastTop = top;
            int rowLeft = AutoConfigList.this.getRowLeft();
            int rowWidth = AutoConfigList.this.getRowWidth();
            int y = this.lastTop;

            if (this.model.selectedIndex == this.index) {
                graphics.fill(rowLeft, y - 2, rowLeft + rowWidth, y + 22, SELECTED_BG);
            }

            int boxX = rowLeft + this.depth * INDENT;
            int boxW = rowLeft + rowWidth - boxX;

            this.widget.setPosition(boxX, y);
            this.widget.setWidth(boxW);
            this.widget.render(graphics, mouseX, mouseY, partialTick);

            if (this.componentLabel != null) {
                graphics.drawString(AutoConfigList.this.font,
                        this.componentLabel.getVisualOrderText(), boxX, y + 6, LABEL_COLOR);

                int labelW = AutoConfigList.this.font.width(this.componentLabel);
                if (mouseX >= boxX && mouseX <= boxX + labelW && mouseY >= y && mouseY <= y + 20) {
                    Style style = this.componentLabel.getStyle();

                    //? >=1.21.5 {
                    /*Component tooltipValue = style.getHoverEvent() instanceof HoverEvent.ShowText(Component value)
                            ? value
                            : null;

                    *///?} else {
                    HoverEvent hover = style.getHoverEvent();
                    Component tooltipValue = hover != null ? hover.getValue(HoverEvent.Action.SHOW_TEXT) : null;//?}

                    if (tooltipValue != null) {
                        graphics.renderTooltip(AutoConfigList.this.font,
                                AutoConfigList.this.font.split(tooltipValue, 200), mouseX, mouseY);
                    }
                }
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            //~ mb_event
            if (this.widget instanceof ColorWidget cw && cw.isOverPicker(mouseX, mouseY)) { //~ !mb_event
                return cw.mouseClicked(mouseX, mouseY, button);
            }

            if (this.model.editable) {
                this.model.selectedIndex = this.index;
                AutoConfigList.this.notifySelectionChanged();
            }

            if (this.componentLabel != null) {
                int boxX = AutoConfigList.this.getRowLeft() + this.depth * INDENT;
                int y = this.lastTop;
                int labelW = AutoConfigList.this.font.width(this.componentLabel);
                //~ mb_event
                if (mouseX >= boxX && mouseX <= boxX + labelW && mouseY >= y && mouseY <= y + 20) { //~ !mb_event
                    Style style = this.componentLabel.getStyle();
                    if (style.getClickEvent() != null) {
                        screen.handleComponentClicked(style);
                        return true;
                    }
                }
            }

            return super.mouseClicked(mouseX, mouseY, button);
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
            return List.of(this.widget);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of(this.widget);
        }
    }

    private final class ObjectElementRow extends Row {
        private final ListModel model;
        private final int index;
        private final int depth;
        private final Button toggleButton;

        ObjectElementRow(final ListModel model, final int index, final int depth) {
            this.model = model;
            this.index = index;
            this.depth = depth;
            this.toggleButton = Button.builder(toggleLabel(group().expanded), b -> toggle())
                    .size(HEADER_BTN, HEADER_BTN).build();
        }

        private GroupNode group() {
            return ((ObjectElement) this.model.elements.get(this.index)).group;
        }

        private void toggle() {
            AutoConfigList.this.commitScalarElements();
            GroupNode g = group();
            g.expanded = !g.expanded;
            this.toggleButton.setMessage(toggleLabel(g.expanded));
            AutoConfigList.this.rebuild();
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            //? <=1.21.8
            this.lastTop = top;
            int rowLeft = AutoConfigList.this.getRowLeft();
            int rowWidth = AutoConfigList.this.getRowWidth();
            int y = this.lastTop;
            int labelX = rowLeft + this.depth * INDENT;

            if (this.model.selectedIndex == this.index) {
                graphics.fill(rowLeft, y - 2, rowLeft + rowWidth, y + 22, SELECTED_BG);
            }

            GroupNode g = group();
            graphics.drawString(AutoConfigList.this.font, g.label.getVisualOrderText(), labelX, y + 6, LABEL_COLOR);

            int toggleX = rowLeft + rowWidth - HEADER_BTN;
            this.toggleButton.setPosition(toggleX, y);
            this.toggleButton.render(graphics, mouseX, mouseY, partialTick);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            int rowLeft = AutoConfigList.this.getRowLeft();
            int rowWidth = AutoConfigList.this.getRowWidth();
            int y = this.lastTop;
            int toggleLeft = rowLeft + rowWidth - HEADER_BTN;

            if (super.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }

            //~ mb_event
            if (mouseX >= rowLeft && mouseX < toggleLeft && mouseY >= y && mouseY < y + 20 && this.model.editable) { //~ !mb_event
                this.model.selectedIndex = this.index;
                AutoConfigList.this.notifySelectionChanged();
                return true;
            }
            return false;
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.of(this.toggleButton);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of(this.toggleButton);
        }
    }

    private static Component toggleLabel(final boolean expanded) {
        return Component.literal(expanded ? "▼" : "▶");
    }
}
