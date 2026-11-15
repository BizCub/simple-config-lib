package io.github.bizcub.simpleConfigLib.autoconfig.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import io.github.bizcub.simpleConfigLib.autoconfig.ConfigHolder;
import io.github.bizcub.simpleConfigLib.autoconfig.ConfigSide;
import io.github.bizcub.simpleConfigLib.autoconfig.annotation.*;
import io.github.bizcub.simpleConfigLib.autoconfig.annotation.Tooltip;
import io.github.bizcub.simpleConfigLib.autoconfig.gui.AutoConfigList.Element;
import io.github.bizcub.simpleConfigLib.autoconfig.gui.AutoConfigList.GroupNode;
import io.github.bizcub.simpleConfigLib.autoconfig.gui.AutoConfigList.ListModel;
import io.github.bizcub.simpleConfigLib.autoconfig.gui.AutoConfigList.ListNode;
import io.github.bizcub.simpleConfigLib.autoconfig.gui.AutoConfigList.Node;
import io.github.bizcub.simpleConfigLib.autoconfig.gui.AutoConfigList.ObjectElement;
import io.github.bizcub.simpleConfigLib.autoconfig.gui.AutoConfigList.ScalarElement;
import io.github.bizcub.simpleConfigLib.autoconfig.gui.AutoConfigList.WidgetNode;
import io.github.bizcub.simpleConfigLib.autoconfig.network.ConfigApplyPayload;
import io.github.bizcub.simpleConfigLib.autoconfig.network.ConfigResultPayload;
import io.github.bizcub.simpleConfigLib.client.SimpleConfigClient;
import io.github.bizcub.simpleConfigLib.internal.KeyFormatter;
import io.github.bizcub.simpleConfigLib.internal.network.NetworkClient;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.tabs.*;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.slf4j.Logger;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

public class AutoConfigScreen extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String DEFAULT_GROUP = "general";
    private static final int DEFAULT_WIDGET_WIDTH = 150;

    private final Screen lastScreen;
    private final ConfigHolder<?> holder;
    private final ConfigHolder<?> editing;
    private final Map<Object, Supplier<Object>> defaultFactories = new IdentityHashMap<>();
    private final Set<EditBox> invalidInputs = Collections.newSetFromMap(new IdentityHashMap<>());
    private ContextMenu contextMenu;
    private ItemPicker itemPicker;
    private EditBox searchBox;
    private Button searchButton;
    private boolean searchOpen;
    private String search = "";
    private boolean invalidListValues;
    private static int nextRequestId;
    private int pendingRequest;
    private long saveDeadline;
    private long errorUntil;
    private HeaderAndFooterLayout layout;
    private final List<AutoConfigList> lists = new ArrayList<>();
    private final TabManager tabManager = new TabManager(this::addRenderableWidget, this::removeWidget);
    private MenuTabBar tabNavigationBar;

    private Button resetButton;
    private Button doneButton;
    private boolean dirty;
    private final String initialSnapshot;

    private final ConfigSide viewEnv;
    private final boolean readOnly;

    public AutoConfigScreen(Screen lastScreen, ConfigHolder<?> holder) {
        this(lastScreen, holder, holder.getMeta().side(), false);
    }

    public AutoConfigScreen(Screen lastScreen, ConfigHolder<?> holder, ConfigSide viewEnv, boolean readOnly) {
        super(Component.translatable("text." + holder.getMeta().name() + ".title"));
        this.lastScreen = lastScreen;
        this.holder = holder;
        this.viewEnv = viewEnv;
        this.readOnly = readOnly;
        this.editing = viewEnv == ConfigSide.CLIENT ? holder : holder.draft();

        if (viewEnv == ConfigSide.CLIENT) this.holder.load();
        this.initialSnapshot = this.editing.snapshot();
    }

    public static <T> Screen create(ConfigHolder<T> holder, Screen parent) {
        return new AutoConfigScreen(parent, holder);
    }

    public static <T> Screen create(ConfigHolder<T> holder, Screen parent, ConfigSide viewEnv, boolean readOnly) {
        return new AutoConfigScreen(parent, holder, viewEnv, readOnly);
    }

    @Override
    protected void init() {
        int previousTab = Math.max(0, this.lists.indexOf(currentList()));
        List<AutoConfigList.ViewState> views = this.lists.stream().map(AutoConfigList::viewState).toList();
        this.layout = new HeaderAndFooterLayout(this);
        this.lists.clear();
        this.defaultFactories.clear();
        this.defaultFactories.put(this.editing.get(), this.editing::defaults);
        this.invalidInputs.clear();
        this.contextMenu = null;
        this.itemPicker = null;

        List<ConfigTab> tabs = new ArrayList<>();
        groupFields().forEach((key, value) ->
                tabs.add(new ConfigTab(key, value))
        );

        this.tabNavigationBar = this.addRenderableWidget(MenuTabBar.builder(this.tabManager, this.width)
                .addTabs(tabs.toArray(new Tab[0]))
                .build()
        );

        LinearLayout footer = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));

        Button cancelButton = Button.builder(CommonComponents.GUI_CANCEL, b -> onClose()).build();
        this.doneButton = Button.builder(CommonComponents.GUI_DONE, b -> save()).build();
        this.doneButton.active = false;

        for (Button button : List.of(cancelButton, this.doneButton)) {
            button.setWidth(Math.max(40, Math.min(DEFAULT_WIDGET_WIDTH, (this.width - 128) / 2)));
            footer.addChild(button);
        }

        this.layout.visitWidgets(w -> {
            w.setTabOrderGroup(1);
            this.addRenderableWidget(w);
        });
        this.tabNavigationBar.selectTab(Math.min(previousTab, Math.max(0, this.lists.size() - 1)), false);
        repositionElements();

        this.resetButton = this.addRenderableWidget(
                Button.builder(Component.literal("\uD83D\uDDD8"), b -> {
                            onReset();
                            markDirty();
                        })
                        .size(20, 20)
                        .pos(this.layout.getFooterHeight() / 2 - 10, this.height - this.layout.getFooterHeight() / 2 - 10)
                        .build());
        this.resetButton.active = !this.readOnly && Minecraft.getInstance().hasShiftDown();
        this.searchButton = this.addRenderableWidget(Button.builder(Component.literal("🔎"), b -> toggleSearch())
                .size(20, 20).build());
        this.searchButton.setTooltip(net.minecraft.client.gui.components.Tooltip.create(
                Component.translatable("text.simple_config_lib.search")));
        this.searchBox = this.addRenderableWidget(new EditBox(this.font, 8, 0, Math.max(40, this.width - 16), 20,
                Component.translatable("text.simple_config_lib.search")));
        this.searchBox.setHint(Component.translatable("gui.recipebook.search_hint"));
        this.searchBox.setValue(this.search);
        this.searchBox.setResponder(this::filter);
        this.searchBox.visible = searchVisible();
        for (AutoConfigList list : this.lists) list.setFilter(this.search);
        repositionElements();
        updateDoneButton();

        if (this.readOnly) {
            this.doneButton.active = false;
            this.doneButton.visible = false;
            this.resetButton.active = false;
            this.resetButton.visible = false;
            for (AutoConfigList l : this.lists) {
                l.setReadOnly(true);
            }
        }
        for (int i = 0; i < Math.min(views.size(), this.lists.size()); i++) this.lists.get(i).restoreViewState(views.get(i));
        repositionElements();
    }

    public void markDirty() {
        this.invalidInputs.removeIf(box -> this.lists.stream().noneMatch(list -> list.containsWidget(box)));
        this.invalidListValues = false;
        for (AutoConfigList list : this.lists) list.commitElements();
        this.dirty = !this.initialSnapshot.equals(this.editing.snapshot())
                || !this.invalidInputs.isEmpty() || this.invalidListValues;
        if (this.viewEnv == ConfigSide.CLIENT) this.holder.notifyChanges();
        updateDoneButton();
    }

    private void updateDoneButton() {
        if (this.doneButton == null) return;
        boolean error = System.currentTimeMillis() < this.errorUntil;
        this.doneButton.setMessage(this.pendingRequest != 0
                ? Component.translatable("text.simple_config_lib.saving")
                : error ? Component.translatable("text.simple_config_lib.save_error") : CommonComponents.GUI_DONE);
        this.doneButton.active = !this.readOnly && this.dirty && this.pendingRequest == 0 && !error
                && this.invalidInputs.isEmpty() && !this.invalidListValues;
    }

    private void save() {
        markDirty();
        if (!this.doneButton.active) return;
        if (this.minecraft.player != null && this.viewEnv != ConfigSide.CLIENT) {
            this.pendingRequest = ++nextRequestId;
            this.saveDeadline = System.currentTimeMillis() + 10000;
            updateDoneButton();
            try {
                NetworkClient.sendToServer(new ConfigApplyPayload(this.holder.id(), this.editing.snapshot(), this.pendingRequest));
            } catch (Exception e) {
                LOGGER.error("Failed to send config {}", this.holder.id(), e);
                saveFailed();
            }
        } else if (this.holder.saveSnapshot(this.editing.snapshot())) {
            saved();
        } else {
            saveFailed();
        }
    }

    public void handleResult(ConfigResultPayload result) {
        if (this.pendingRequest == 0 || result.requestId() != this.pendingRequest || !result.name().equals(this.holder.id())) return;
        if (result.success()) saved();
        else saveFailed();
    }

    private void saved() {
        this.pendingRequest = 0;
        this.dirty = false;
        this.minecraft.gui.setScreen(this.lastScreen);
    }

    private void saveFailed() {
        this.pendingRequest = 0;
        this.errorUntil = System.currentTimeMillis() + 2000;
        updateDoneButton();
    }

    @Override
    public void tick() {
        if (this.pendingRequest != 0 && System.currentTimeMillis() >= this.saveDeadline) saveFailed();
        updateDoneButton();
        if (this.searchBox != null && this.searchBox.visible != searchVisible()) {
            this.searchBox.visible = searchVisible();
            if (!this.searchBox.visible) {
                this.searchBox.setValue("");
                if (this.getFocused() == this.searchBox) this.setFocused(currentList());
            }
            repositionElements();
        }
        AutoConfigList list = currentList();
        if (list != null) list.tickDrag();
    }

    private void onReset() {
        this.editing.reset();
        this.rebuildWidgets();
    }

    private boolean searchVisible() {
        return this.searchOpen || SimpleConfigClient.getInstance().get().alwaysShowSearch;
    }

    private void toggleSearch() {
        closeActions();
        if (!SimpleConfigClient.getInstance().get().alwaysShowSearch) this.searchOpen = !this.searchOpen;
        this.searchBox.visible = searchVisible();
        if (!this.searchBox.visible) this.searchBox.setValue("");
        repositionElements();
        this.setFocused(this.searchBox.visible ? this.searchBox : currentList());
    }

    private void filter(String value) {
        this.search = value;
        for (AutoConfigList list : this.lists) list.setFilter(value);
        AutoConfigList current = currentList();
        if (!value.isBlank() && (current == null || current.children().isEmpty())) {
            for (int i = 0; i < this.lists.size(); i++) {
                if (!this.lists.get(i).children().isEmpty()) {
                    this.tabNavigationBar.selectTab(i, false);
                    break;
                }
            }
        }
    }

    record RowAction(String key, Runnable action, boolean enabled) {}

    void openActions(List<RowAction> actions, int x, int y) {
        closeItemPicker();
        closeActions();
        if (this.readOnly || this.pendingRequest != 0 || actions.isEmpty()) return;
        AutoConfigList list = currentList();
        if (list != null) {
            list.cancelItemDrag();
            list.closePickersExceptSwatchAt(Integer.MIN_VALUE, Integer.MIN_VALUE);
        }
        this.contextMenu = this.addRenderableWidget(new ContextMenu(this.font, actions, this::closeActions,
                x, y, this.width, this.height));
        this.setFocused(this.contextMenu);
    }

    boolean hasContextMenu() {
        return this.contextMenu != null || this.itemPicker != null;
    }

    void openItemPicker(ItemWidget owner) {
        if (this.readOnly || this.pendingRequest != 0) return;
        closeActions();
        closeItemPicker();
        AutoConfigList list = currentList();
        if (list != null) {
            list.cancelItemDrag();
            list.closePickersExceptSwatchAt(Integer.MIN_VALUE, Integer.MIN_VALUE);
        }
        this.itemPicker = this.addRenderableWidget(new ItemPicker(this.font, owner, this::closeItemPicker, this.width, this.height));
        this.setFocused(this.itemPicker);
    }

    private void closeItemPicker() {
        if (this.itemPicker == null) return;
        this.removeWidget(this.itemPicker);
        this.itemPicker = null;
        this.setFocused(currentList());
    }

    private void closeActions() {
        if (this.contextMenu == null) return;
        if (this.getFocused() == this.contextMenu) this.setFocused(currentList());
        this.removeWidget(this.contextMenu);
        this.contextMenu = null;
    }

    private String key(final String raw) {
        return this.holder.getMeta().snakeCaseKeys() ? KeyFormatter.toSnakeCase(raw) : raw;
    }

    private Map<String, List<Field>> groupFields() {
        Map<String, List<Field>> groups = new LinkedHashMap<>();
        groups.put(DEFAULT_GROUP, new ArrayList<>());
        for (Field field : instanceFields(this.holder.type())) {
            ConfigGroup group = field.getAnnotation(ConfigGroup.class);
            String key = group != null ? group.value() : DEFAULT_GROUP;
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(field);
        }

        if (groups.get(DEFAULT_GROUP).isEmpty()) {
            groups.remove(DEFAULT_GROUP);
        }
        return groups;
    }

    private AutoConfigList currentList() {
        Tab tab = this.tabManager.getCurrentTab();
        return tab instanceof ConfigTab ct ? ct.list() : null;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        if (this.resetButton != null) {
            boolean shift = Minecraft.getInstance().hasShiftDown();
            this.resetButton.active = !this.readOnly && this.pendingRequest == 0 && shift;
            Component resetComponent = Component.translatable("text.simple_config_lib.reset");
            this.resetButton.setTooltip(net.minecraft.client.gui.components.Tooltip.create(
                    shift
                            ? resetComponent
                            : resetComponent.copy().append("\n").append(Component.translatable("text.simple_config_lib.reset.shift"))));
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean doubleClick) {
        if (this.pendingRequest != 0) return true;
        if (this.itemPicker != null) return this.itemPicker.mouseClicked(mouseButtonEvent, doubleClick);
        if (this.contextMenu != null) {
            if (this.contextMenu.isMouseOver(mouseButtonEvent.x(), mouseButtonEvent.y())) {
                return this.contextMenu.mouseClicked(mouseButtonEvent, doubleClick);
            }
            closeActions();
            if (mouseButtonEvent.button() != 3) return true;
        }
        AutoConfigList list = currentList();

        if (list != null && list.isPopupOpenAt(mouseButtonEvent.x(), mouseButtonEvent.y())) {
            list.mouseClicked(mouseButtonEvent, doubleClick);
            return true;
        }

        if (list != null) {
            list.closePickersExceptSwatchAt(mouseButtonEvent.x(), mouseButtonEvent.y());
        }
        boolean handled = super.mouseClicked(mouseButtonEvent, doubleClick);
        if (this.itemPicker != null) this.setFocused(this.itemPicker);
        return handled;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent mouseButtonEvent, double dx, double dy) {
        if (this.itemPicker != null) return this.itemPicker.mouseDragged(mouseButtonEvent, dx, dy);
        AutoConfigList list = currentList();
        if (list != null && (list.isPickerDragging() || list.hasItemDrag())) {
            return list.mouseDragged(mouseButtonEvent, dx, dy);
        }
        return super.mouseDragged(mouseButtonEvent, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent mouseButtonEvent) {
        if (this.itemPicker != null) return this.itemPicker.mouseReleased(mouseButtonEvent);
        AutoConfigList list = currentList();
        if (list != null && (list.isPickerDragging() || list.hasItemDrag())) {
            list.mouseReleased(mouseButtonEvent);
            return true;
        }
        return super.mouseReleased(mouseButtonEvent);
    }

    @Override
    protected void repositionElements() {
        if (this.tabNavigationBar != null) {
            this.tabNavigationBar.arrangeElements(this.width);
            int tabAreaTop = this.tabNavigationBar.getRectangle().bottom();
            if (this.searchBox != null) {
                this.searchBox.setPosition(8, tabAreaTop + 2);
                this.searchBox.setWidth(Math.max(40, this.width - 16));
            }
            if (searchVisible()) tabAreaTop += 26;
            ScreenRectangle tabArea = new ScreenRectangle(0, tabAreaTop, this.width,
                    this.height - this.layout.getFooterHeight() - tabAreaTop);
            this.tabManager.setTabArea(tabArea);
            this.layout.setHeaderHeight(tabAreaTop);
            this.layout.arrangeElements();
        }
        int y = this.height - this.layout.getFooterHeight() / 2 - 10;
        if (this.resetButton != null) this.resetButton.setPosition(8, y);
        if (this.searchButton != null) {
            this.searchButton.setPosition(this.resetButton != null && this.resetButton.visible ? 32 : 8, y);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        if (this.pendingRequest != 0) return true;
        if (this.itemPicker != null) return this.itemPicker.keyPressed(keyEvent);
        if (this.contextMenu != null) return this.contextMenu.keyPressed(keyEvent);
        AutoConfigList list = currentList();
        if (list != null && list.hasItemDrag()) {
            if (keyEvent.isEscape()) list.cancelItemDrag();
            return true;
        }
        if (list != null && ((keyEvent.key() == InputConstants.KEY_F10 && keyEvent.hasShiftDown()) || keyEvent.hasAltDown())
                && list.keyPressed(keyEvent)) return true;
        if (keyEvent.key() == InputConstants.KEY_F && keyEvent.hasControlDown()) {
            if (!searchVisible()) toggleSearch();
            this.setFocused(this.searchBox);
            return true;
        }
        if (keyEvent.isEscape() && searchVisible()) {
            if (!SimpleConfigClient.getInstance().get().alwaysShowSearch) {
                toggleSearch();
                return true;
            }
            if (!this.searchBox.getValue().isEmpty()) {
                this.searchBox.setValue("");
                return true;
            }
        }
        return (this.tabNavigationBar != null && this.tabNavigationBar.keyPressed(keyEvent)) || super.keyPressed(keyEvent);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (this.itemPicker != null) return this.itemPicker.charTyped(event);
        return this.pendingRequest != 0 || this.contextMenu != null || super.charTyped(event);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double horizontal, double vertical) {
        if (this.pendingRequest != 0) return true;
        if (this.itemPicker != null) return this.itemPicker.mouseScrolled(x, y, horizontal, vertical);
        if (this.contextMenu != null) {
            closeActions();
            return true;
        }
        AutoConfigList list = currentList();
        if (list != null && list.hasItemDrag()) return list.mouseScrolled(x, y, horizontal, vertical);
        return super.mouseScrolled(x, y, horizontal, vertical);
    }

    @Override
    public void onClose() {
        if (this.pendingRequest != 0) return;
        closeActions();
        if (!this.dirty) {
            this.minecraft.gui.setScreen(this.lastScreen);
            return;
        }

        this.minecraft.gui.setScreen(new ConfirmScreen(
                yes -> {
                    if (yes) {
                        if (this.viewEnv == ConfigSide.CLIENT) this.holder.applySnapshot(this.initialSnapshot);
                        this.minecraft.gui.setScreen(this.lastScreen);
                    } else {
                        this.minecraft.gui.setScreen(this);
                    }
                },
                Component.translatable("text.simple_config_lib.confirm.title"),
                Component.translatable("text.simple_config_lib.confirm.message"),
                Component.translatable("text.simple_config_lib.confirm.discard"),
                Component.translatable("text.simple_config_lib.confirm.keep")
        ));
    }

    private final class ConfigTab extends GridLayoutTab {
        private final AutoConfigList list;

        ConfigTab(final String groupId, final List<Field> fields) {
            super(AutoConfigScreen.this.groupLabel(groupId));
            this.list = new AutoConfigList(AutoConfigScreen.this.minecraft, AutoConfigScreen.this.width,
                    AutoConfigScreen.this.height, 0, AutoConfigScreen.this);
            AutoConfigScreen.this.lists.add(this.list);
            for (Field field : fields) {
                Node node = AutoConfigScreen.this.buildNode(AutoConfigScreen.this.editing.get(), field);
                if (node != null) {
                    this.list.addNode(node);
                }
            }
        }

        AutoConfigList list() {
            return this.list;
        }

        @Override
        public void visitChildren(final Consumer<AbstractWidget> consumer) {
            consumer.accept(this.list);
        }

        @Override
        public void doLayout(final ScreenRectangle rectangle) {
            this.list.updateSizeAndPosition(rectangle.width(), rectangle.height(), rectangle.top());
            this.list.restoreScroll();
        }
    }

    private Node buildNode(final Object config, final Field field) {
        return buildNode(config, field, null);
    }

    private Node buildNode(final Object config, final Field field, final String fieldPrefix) {
        Node node = buildFieldNode(config, field, fieldPrefix);
        if (node != null && field.getType() != Component.class) {
            node.resetAction = () -> {
                try {
                    field.set(config, defaultValue(config, field));
                    if (node instanceof WidgetNode widgetNode) {
                        widgetNode.widget = buildWidget(config, field);
                        AutoConfigList list = currentList();
                        if (list != null) list.rebuild();
                    } else {
                        this.rebuildWidgets();
                    }
                    markDirty();
                } catch (IllegalAccessException e) {
                    LOGGER.error("Failed to reset config field {}", field.getName(), e);
                }
            };
            if (node instanceof WidgetNode widgetNode && field.isAnnotationPresent(Slider.class)
                    && (field.getType() == int.class || field.getType() == Integer.class)) {
                node.manualAction = () -> {
                    AutoConfigList list = currentList();
                    if (list == null) return;
                    try {
                        if (widgetNode.widget instanceof EditBox box) {
                            if (parseElement(Integer.class, box.getValue()) == null) return;
                            this.invalidInputs.remove(box);
                            widgetNode.widget = sliderWidget(config, field);
                        } else {
                            widgetNode.widget = numberBox(config, field);
                        }
                        list.rebuild();
                        list.focusWidget(widgetNode.widget);
                        if (widgetNode.widget instanceof EditBox box) {
                            box.setCursorPosition(box.getValue().length());
                            box.setHighlightPos(0);
                        }
                    } catch (IllegalAccessException e) {
                        LOGGER.error("Failed to edit config field {}", field.getName(), e);
                    }
                };
            }
        }
        return node;
    }

    private Node buildFieldNode(final Object config, final Field field, final String fieldPrefix) {
        field.setAccessible(true);
        try {
            Class<?> t = field.getType();

            if (List.class.isAssignableFrom(t)) {
                return buildListNode(config, field, fieldPrefix);
            }

            AbstractWidget widget = buildWidget(config, field);
            if (widget != null) {
                WidgetNode n = new WidgetNode();
                n.label = field.getType() == Component.class
                        ? (Component) field.get(config)
                        : labelFor(field, fieldPrefix);
                n.tooltip = tooltipFor(field, fieldPrefix);
                n.widget = widget;
                return n;
            }

            if (isNestedObject(t)) {
                Object value = field.get(config);
                if (value == null) {
                    value = instantiate(t);
                    setQuietly(config, field, value);
                }
                this.defaultFactories.put(value, () -> {
                    Object defaults = defaultValue(config, field);
                    return defaults != null ? defaults : instantiate(t);
                });
                GroupNode g = new GroupNode();
                g.label = labelFor(field, fieldPrefix);
                g.tooltip = tooltipFor(field, fieldPrefix);
                for (Field cf : instanceFields(t)) {
                    Node child = buildNode(value, cf, fieldPrefix);
                    if (child != null) {
                        g.children.add(child);
                    }
                }
                return g;
            }

            LOGGER.warn("Skipping field {}: unsupported type {}", field.getName(), t.getSimpleName());
            return null;
        } catch (Exception ex) {
            LOGGER.error("Failed to construct node for field {}", field.getName(), ex);
            return null;
        }
    }

    private static int textMaxLength(final Field field) {
        TextConfig textConfig = field.getAnnotation(TextConfig.class);
        return textConfig != null ? textConfig.maxLength() : 256;
    }

    private Node buildListNode(final Object config, final Field field, final String fieldPrefix)
            throws IllegalAccessException {
        final Class<?> element = resolveListElementType(field);
        if (element == null) {
            LOGGER.warn("Skipping list {}: element type could not be determined", field.getName());
            return null;
        }

        ListConfig cfg = field.getAnnotation(ListConfig.class);
        final boolean expanded = cfg != null && cfg.expanded();
        final boolean expandElements = cfg == null || cfg.expandElements();
        final boolean addToFront = cfg != null && cfg.addToFront();
        final boolean editable = cfg == null || cfg.editable();
        final boolean translateElements = cfg != null && cfg.translateElements();
        final Slider sliderCfg = field.getAnnotation(Slider.class);
        final Color colorCfg = field.getAnnotation(Color.class);

        List<?> current = (List<?>) field.get(config);
        Supplier<List<?>> defaults = () -> (List<?>) defaultValue(config, field);

        if (isSupportedListElement(element)) {
            List<Element> initial = new ArrayList<>();
            if (current != null) {
                for (Object o : current) {
                    if (element == Component.class && o instanceof Component c) {
                        initial.add(new ScalarElement(c));
                    } else {
                        initial.add(new ScalarElement(String.valueOf(o)));
                    }
                }
            }
            final ListModel model = new ListModel(initial, editable, expanded, addToFront, element,
                    () -> new ScalarElement(
                            element == Boolean.class ? "false"
                                    : colorCfg  != null ? "0"
                                      : sliderCfg != null ? String.valueOf(sliderCfg.min())
                                        : Number.class.isAssignableFrom(element) ? "0" : ""));
            for (int i = 0; i < initial.size(); i++) {
                int index = i;
                initial.get(i).resetFactory = () -> {
                    List<?> values = defaults.get();
                    Object value = values != null && index < values.size() ? values.get(index) : null;
                    if (value instanceof Component component) return new ScalarElement(component);
                    return value != null ? new ScalarElement(String.valueOf(value)) : model.factory.get();
                };
            }
            model.textMaxLength = textMaxLength(field);
            model.itemSelector = element == String.class && field.isAnnotationPresent(ItemSelector.class);

            if (element == Integer.class) {
                model.sliderCfg = sliderCfg;
                model.colorCfg = colorCfg;
            }

            model.applyAction = () -> {
                List<Object> result = new ArrayList<>();
                for (Element e : model.elements) {
                    if (e instanceof ScalarElement s) {
                        if (element == Component.class) {
                            result.add(s.component != null ? s.component : Component.literal(s.value));
                        } else {
                            Object parsed = parseElement(element, s.value);
                            if (parsed == null) {
                                this.invalidListValues = true;
                                return;
                            }
                            result.add(parsed);
                        }
                    }
                }
                setQuietly(config, field, result);
            };

            ListNode n = new ListNode();
            n.label = labelFor(field, fieldPrefix);
            n.tooltip = tooltipFor(field, fieldPrefix);
            n.model = model;
            return n;
        }

        if (isNestedObject(element)) {
            List<Element> initial = new ArrayList<>();
            if (current != null) {
                for (int i = 0; i < current.size(); i++) {
                    int index = i;
                    Object value = current.get(i);
                    Supplier<Object> factory = () -> {
                        List<?> values = defaults.get();
                        return values != null && index < values.size() && values.get(index) != null
                                ? values.get(index) : instantiate(element);
                    };
                    this.defaultFactories.put(value, factory);
                    ObjectElement entry = buildObjectElement(element, value, field, translateElements, expandElements);
                    entry.resetFactory = () -> defaultObjectElement(element, factory, field, translateElements, expandElements);
                    initial.add(entry);
                }
            }
            final ListModel model = new ListModel(initial, editable, expanded, addToFront, element,
                    () -> defaultObjectElement(element, () -> instantiate(element), field, translateElements, expandElements));

            model.applyAction = () -> {
                List<Object> result = new ArrayList<>();
                for (Element entry : model.elements) {
                    if (entry instanceof ObjectElement object && object.value != null) result.add(object.value);
                }
                setQuietly(config, field, result);
            };

            ListNode n = new ListNode();
            n.label = labelFor(field);
            n.tooltip = tooltipFor(field);
            n.model = model;
            return n;
        }

        LOGGER.warn("Skipping list {}: unsupported element type {}", field.getName(), element.getSimpleName());
        return null;
    }

    private Object defaultValue(Object config, Field field) {
        Supplier<Object> factory = this.defaultFactories.get(config);
        try {
            return field.get(factory != null ? factory.get() : instantiate(config.getClass()));
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to read default config field " + field.getName(), e);
        }
    }

    private ObjectElement defaultObjectElement(Class<?> element, Supplier<Object> defaults, Field field,
                                               boolean translate, boolean expanded) {
        Object value = defaults.get();
        this.defaultFactories.put(value, defaults);
        ObjectElement entry = buildObjectElement(element, value, field, translate, expanded);
        entry.resetFactory = () -> defaultObjectElement(element, defaults, field, translate, expanded);
        return entry;
    }

    private ObjectElement buildObjectElement(final Class<?> element, final Object obj,
                                             final Field ownerField, final boolean translateElements,
                                             final boolean expandElements) {
        GroupNode g = new GroupNode();
        g.label = elementLabel(ownerField, element, translateElements);
        g.expanded = expandElements;
        for (Field cf : instanceFields(element)) {
            Node child = buildNode(obj, cf, ownerField.getName());
            if (child != null) {
                g.children.add(child);
            }
        }
        return new ObjectElement(g, obj);
    }

    @SuppressWarnings("rawtypes")
    private AbstractWidget buildWidget(final Object config, final Field field) throws IllegalAccessException {
        Class<?> t = field.getType();

        if (t == Component.class) {
            return new StringWidget(Component.empty(), font);
        }

        if (t == boolean.class || t == Boolean.class) {
            boolean initial = (Boolean) field.get(config);
            BooleanConfig cfg = field.getAnnotation(BooleanConfig.class);
            final boolean yesNo = cfg == null || cfg.yesNo();
            return CycleButton.builder((Boolean v) -> booleanLabel(v, yesNo), initial)
                    .withValues(true, false)
                    .displayOnlyValue()
                    .create(0, 0, DEFAULT_WIDGET_WIDTH, 20, Component.empty(),
                            (b, v) -> apply(config, field, v));
        }

        if ((t == int.class || t == Integer.class) && field.isAnnotationPresent(Slider.class)) {
            return SimpleConfigClient.getInstance().get().slidersAsNumbers
                    ? numberBox(config, field) : sliderWidget(config, field);
        }

        if ((t == int.class || t == Integer.class) && field.isAnnotationPresent(Color.class)) {
            Color c = field.getAnnotation(Color.class);
            int initial = (Integer) field.get(config);
            return new ColorWidget(this.font, initial, c.alpha(), v -> apply(config, field, v));
        }

        if (t.isEnum()) {
            Enum initial = (Enum) field.get(config);
            Enum[] values = (Enum[]) t.getEnumConstants();
            EnumConfig cfg = field.getAnnotation(EnumConfig.class);
            final boolean translate = this.holder.getMeta().translate() && cfg != null && cfg.translate();
            return CycleButton.builder((Enum v) -> enumValueLabel(v, translate), initial)
                    .withValues(values)
                    .displayOnlyValue()
                    .create(0, 0, DEFAULT_WIDGET_WIDTH, 20, Component.empty(),
                            (b, v) -> apply(config, field, v));
        }

        if (t == String.class) {
            if (field.isAnnotationPresent(ItemSelector.class)) {
                return new ItemWidget(this.font, (String) field.get(config), v -> apply(config, field, v), this::openItemPicker);
            }
            EditBox box = new EditBox(this.font, 0, 0, DEFAULT_WIDGET_WIDTH, 20, Component.empty());
            box.setMaxLength(Math.max(textMaxLength(field), ((String) field.get(config)).length()));
            box.setValue((String) field.get(config));
            box.setResponder(v -> apply(config, field, v));
            return box;
        }

        if (t == int.class || t == Integer.class || t == long.class || t == Long.class
                || t == float.class || t == Float.class || t == double.class || t == Double.class) {
            return numberBox(config, field);
        }

        return null;
    }

    private Component labelFor(final Field field) {
        return labelFor(field, null);
    }

    private Component labelFor(final Field field, final String fieldPrefix) {
        if (!this.holder.getMeta().translate()) {
            return Component.literal(KeyFormatter.humanize(field.getName()));
        }
        String fieldKey = fieldPrefix != null
                ? key(fieldPrefix) + "." + key(field.getName())
                : key(field.getName());
        return Component.translatable(
                "text." + this.holder.getMeta().name() + ".option." + fieldKey,
                field.getName());
    }

    private Component groupLabel(final String groupId) {
        return this.holder.getMeta().translate()
                ? Component.translatable("text." + this.holder.getMeta().name() + ".group." + groupId)
                : Component.literal(KeyFormatter.humanize(groupId));
    }

    private Component elementLabel(final Field ownerField, final Class<?> element,
                                   final boolean translateElements) {
        if (!translateElements) {
            return Component.literal(element.getSimpleName());
        }
        return Component.translatable(
                "text." + this.holder.getMeta().name() + ".option." + key(ownerField.getName()) + ".element",
                element.getSimpleName());
    }

    private Component tooltipFor(final Field field) {
        return tooltipFor(field, null);
    }

    private Component tooltipFor(final Field field, final String fieldPrefix) {
        Tooltip tip = field.getAnnotation(Tooltip.class);
        if (tip == null) {
            return null;
        }
        String fieldKey = fieldPrefix != null
                ? key(fieldPrefix) + "." + key(field.getName())
                : key(field.getName());
        String key = "text." + this.holder.getMeta().name() + ".option." + fieldKey + ".tooltip";
        return Component.translatable(key);
    }

    private Component enumValueLabel(final Enum<?> value, final boolean translate) {
        if (!translate) {
            return Component.literal(value.name());
        }
        if (isToStringOverridden(value)) {
            return Component.translatable(value.toString());
        }
        String key = "text." + this.holder.getMeta().name()
                + ".enum." + key(value.getDeclaringClass().getSimpleName())
                + "." + key(value.name());
        return Component.translatable(key);
    }

    private static boolean isToStringOverridden(final Enum<?> value) {
        try {
            Method m = value.getDeclaringClass().getMethod("toString");
            Class<?> declaring = m.getDeclaringClass();
            return declaring != Enum.class && declaring != Object.class;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private Component booleanLabel(final boolean value, final boolean yesNo) {
        String text = yesNo ? (value ? "gui.yes" : "gui.no") : (value ? "options.on" : "options.off");
        return Component.translatable(text)
                .withStyle(value ? ChatFormatting.GREEN : ChatFormatting.RED);
    }

    private static boolean isSupportedListElement(final Class<?> type) {
        return type == String.class
                || type == Integer.class
                || type == Long.class
                || type == Float.class
                || type == Double.class
                || type == Boolean.class
                || type == Component.class;
    }

    private static boolean isNestedObject(final Class<?> t) {
        return !t.isPrimitive()
                && !t.isEnum()
                && !t.isArray()
                && t != String.class
                && !Number.class.isAssignableFrom(t)
                && t != Boolean.class
                && t != Character.class
                && !t.getName().startsWith("java.")
                && !t.getName().startsWith("javax.");
    }

    private static List<Field> instanceFields(final Class<?> type) {
        List<Field> result = new ArrayList<>();
        for (Field f : type.getDeclaredFields()) {
            int mods = f.getModifiers();
            if (Modifier.isStatic(mods) || Modifier.isTransient(mods)) {
                continue;
            }
            f.setAccessible(true);
            result.add(f);
        }
        return result;
    }

    private static Object instantiate(final Class<?> type) {
        try {
            var ctor = type.getDeclaredConstructor();
            ctor.setAccessible(true);
            return ctor.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("The nested class " + type.getName()
                    + " must have a no-argument constructor.", e);
        }
    }

    private static Class<?> resolveListElementType(final Field field) {
        Type generic = field.getGenericType();
        if (generic instanceof ParameterizedType pt && pt.getActualTypeArguments().length == 1) {
            Type arg = pt.getActualTypeArguments()[0];
            if (arg instanceof Class<?> c) {
                return c;
            }
        }
        return null;
    }

    static Object parseElement(final Class<?> type, final String raw) {
        String s = raw.trim();
        try {
            if (type == String.class) return raw;
            if (type == Boolean.class) return Boolean.parseBoolean(s);
            if (s.isEmpty()) return null;
            if (type == Integer.class) return Integer.parseInt(s);
            if (type == Long.class) return Long.parseLong(s);
            if (type == Float.class) {
                float value = Float.parseFloat(s);
                return Float.isFinite(value) ? value : null;
            }
            if (type == Double.class) {
                double value = Double.parseDouble(s);
                return Double.isFinite(value) ? value : null;
            }
        } catch (NumberFormatException ignored) {
            return null;
        }
        return raw;
    }

    private void apply(final Object config, final Field field, final Object value) {
        setQuietly(config, field, value);
        markDirty();
    }

    private static void setQuietly(final Object config, final Field field, final Object value) {
        try {
            field.setAccessible(true);
            field.set(config, value);
        } catch (IllegalAccessException ignored) {
        }
    }

    private IntSlider sliderWidget(final Object config, final Field field) throws IllegalAccessException {
        Slider slider = field.getAnnotation(Slider.class);
        return new IntSlider(slider.min(), slider.max(), slider.step(), (Integer) field.get(config),
                value -> apply(config, field, value));
    }

    private EditBox numberBox(final Object config, final Field field) throws IllegalAccessException {
        EditBox box = new EditBox(this.font, 0, 0, DEFAULT_WIDGET_WIDTH, 20, labelFor(field));
        box.setValue(String.valueOf(field.get(config)));
        box.setResponder(raw -> {
            Object value = parseElement(boxed(field.getType()), raw);
            if (value == null) {
                this.invalidInputs.add(box);
                box.setTextColor(0xFFFF5555);
            } else {
                this.invalidInputs.remove(box);
                box.setTextColor(0xFFE0E0E0);
                setQuietly(config, field, value);
            }
            markDirty();
        });
        return box;
    }

    private static Class<?> boxed(Class<?> type) {
        if (type == int.class) return Integer.class;
        if (type == long.class) return Long.class;
        if (type == float.class) return Float.class;
        if (type == double.class) return Double.class;
        return type;
    }

    public static final class IntSlider extends AbstractSliderButton {
        private final int min, max, step;
        private final IntConsumer onApply;
        private int current;

        IntSlider(final int min, final int max, final int step, final int initial, final IntConsumer onApply) {
            super(0, 0, DEFAULT_WIDGET_WIDTH, 20, Component.empty(),
                    max == min ? 0.0 : (double) (initial - min) / (max - min));
            this.min = min;
            this.max = max;
            this.step = Math.max(1, step);
            this.current = initial;
            this.onApply = onApply;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.literal(Integer.toString(this.current)));
        }

        @Override
        protected void applyValue() {
            int raw = Mth.floor(Mth.clampedLerp(this.value, this.min, this.max));
            int snapped = this.min + Math.round((raw - this.min) / (float) this.step) * this.step;
            this.current = Mth.clamp(snapped, this.min, this.max);
            this.onApply.accept(this.current);
        }
    }
}
