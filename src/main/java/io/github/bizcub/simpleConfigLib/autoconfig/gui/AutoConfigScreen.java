package io.github.bizcub.simpleConfigLib.autoconfig.gui;

import com.mojang.logging.LogUtils;
import io.github.bizcub.simpleConfigLib.autoconfig.network.ConfigApplyPayload;
import io.github.bizcub.simpleConfigLib.autoconfig.ConfigHolder;
import io.github.bizcub.simpleConfigLib.autoconfig.annotation.*;
import io.github.bizcub.simpleConfigLib.autoconfig.annotation.Tooltip;
import io.github.bizcub.simpleConfigLib.util.KeyFormatter;
import io.github.bizcub.simpleConfigLib.autoconfig.gui.AutoConfigList.Element;
import io.github.bizcub.simpleConfigLib.autoconfig.gui.AutoConfigList.GroupNode;
import io.github.bizcub.simpleConfigLib.autoconfig.gui.AutoConfigList.ListModel;
import io.github.bizcub.simpleConfigLib.autoconfig.gui.AutoConfigList.ListNode;
import io.github.bizcub.simpleConfigLib.autoconfig.gui.AutoConfigList.Node;
import io.github.bizcub.simpleConfigLib.autoconfig.gui.AutoConfigList.ObjectElement;
import io.github.bizcub.simpleConfigLib.autoconfig.gui.AutoConfigList.ScalarElement;
import io.github.bizcub.simpleConfigLib.autoconfig.gui.AutoConfigList.WidgetNode;
import io.github.bizcub.simpleConfigLib.util.component.ComponentBuilder;
import io.github.bizcub.simpleConfigLib.util.network.NetworkClient;
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
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.slf4j.Logger;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

//? >=1.21.9 {
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;//?}

public class AutoConfigScreen extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String DEFAULT_GROUP = "general";
    private static final int DEFAULT_WIDGET_WIDTH = 150;

    private final Screen lastScreen;
    private final ConfigHolder<?> holder;
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private final List<Runnable> applyActions = new ArrayList<>();
    private final List<AutoConfigList> lists = new ArrayList<>();
    private final TabManager tabManager = new TabManager(this::addRenderableWidget, this::removeWidget);

    private final Map<ObjectElement, Object> objectBacking = new IdentityHashMap<>();

    private MenuTabBar tabNavigationBar;

    private Button resetButton;
    private Button doneButton;
    private boolean dirty;
    private String initialSnapshot;

    private final Side.Env viewEnv;
    private final boolean readOnly;

    public AutoConfigScreen(Screen lastScreen, ConfigHolder<?> holder) {
        this(lastScreen, holder, holder.getMeta().env(), false);
    }

    public AutoConfigScreen(Screen lastScreen, ConfigHolder<?> holder, Side.Env viewEnv, boolean readOnly) {
        super(ComponentBuilder.translatable("text." + holder.getMeta().name() + ".title").build());
        this.lastScreen = lastScreen;
        this.holder = holder;
        this.viewEnv = viewEnv;
        this.readOnly = readOnly;
        if (viewEnv != Side.Env.SERVER) {
            this.holder.load();
        }
    }

    public static <T> Screen create(ConfigHolder<T> holder, Screen parent) {
        return new AutoConfigScreen(parent, holder);
    }

    public static <T> Screen create(ConfigHolder<T> holder, Screen parent, Side.Env viewEnv, boolean readOnly) {
        return new AutoConfigScreen(parent, holder, viewEnv, readOnly);
    }

    @Override
    protected void init() {
        this.applyActions.clear();
        this.lists.clear();
        this.objectBacking.clear();

        List<ConfigTab> tabs = new ArrayList<>();
        groupFields().forEach((key, value) ->
                tabs.add(new ConfigTab(key, value))
        );

        this.tabNavigationBar = this.addRenderableWidget(MenuTabBar.builder(this.tabManager, this.width)
                .addTabs(tabs.toArray(new Tab[0]))
                .build()
        );

        //? >=1.20.2 {
        LinearLayout footer = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
        //?} else {
        /*LinearLayout footer = this.layout.addToFooter(new LinearLayout(0, 0, LinearLayout.Orientation.HORIZONTAL));
        footer.defaultChildLayoutSetting().padding(4, 0);*///?}

        Button cancelButton = Button.builder(CommonComponents.GUI_CANCEL, b -> onClose()).build();
        this.doneButton = Button.builder(CommonComponents.GUI_DONE, b -> {
            for (AutoConfigList l : this.lists) {
                l.commitElements();
            }
            this.applyActions.forEach(Runnable::run);

            if (this.viewEnv == Side.Env.SERVER && !this.readOnly) {
                NetworkClient.sendToServer(new ConfigApplyPayload(this.holder.getMeta().name(), this.holder.snapshot()));
            } else {
                this.holder.save();
            }

            this.dirty = false;
            this.minecraft.gui.setScreen(this.lastScreen);
        }).build();
        this.doneButton.active = false;

        List<Button> buttons = new ArrayList<>(List.of(cancelButton, this.doneButton));
        //? <1.20.2
        //Collections.reverse(buttons);
        buttons.forEach(footer::addChild);

        this.layout.visitWidgets(w -> {
            w.setTabOrderGroup(1);
            this.addRenderableWidget(w);
        });
        this.tabNavigationBar.selectTab(0, false);
        repositionElements();

        this.resetButton = this.addRenderableWidget(
                Button.builder(ComponentBuilder.literal("\uD83D\uDDD8").build(), b -> {
                            onReset();
                            markDirty();
                        })
                        .size(20, 20)
                        .pos(this.layout.getFooterHeight() / 2 - 10, this.height - this.layout.getFooterHeight() / 2 - 10)
                        .build());
        this.resetButton.active = Minecraft.getInstance().hasShiftDown();
        if (this.initialSnapshot == null) {
            this.initialSnapshot = this.holder.snapshot();
        }
        this.doneButton.active = this.dirty;

        if (this.readOnly) {
            this.doneButton.active = false;
            this.doneButton.visible = false;
            this.resetButton.active = false;
            this.resetButton.visible = false;
            for (AutoConfigList l : this.lists) {
                l.setReadOnly(true);
            }
        }
    }

    public void markDirty() {
        for (AutoConfigList l : this.lists) {
            l.commitElements();
        }
        this.applyActions.forEach(Runnable::run);

        boolean changed = this.initialSnapshot != null
                && !this.initialSnapshot.equals(this.holder.snapshot());
        this.dirty = changed;
        if (this.doneButton != null) {
            this.doneButton.active = changed;
        }
    }

    private void onReset() {
        this.holder.reset();
        this.rebuildWidgets();
    }

    private String key(final String raw) {
        return this.holder.getMeta().snakeCaseKeys() ? KeyFormatter.toSnakeCase(raw) : raw;
    }

    private boolean matchesCurrentView(final Side.Env fieldEnv) {
        return fieldEnv == this.viewEnv;
    }

    private Map<String, List<Field>> groupFields() {
        Map<String, List<Field>> groups = new LinkedHashMap<>();
        groups.put(DEFAULT_GROUP, new ArrayList<>());
        for (Field field : this.holder.type().getDeclaredFields()) {
            int mods = field.getModifiers();
            if (Modifier.isStatic(mods) || Modifier.isTransient(mods)) {
                continue;
            }
            if (!matchesCurrentView(this.holder.envOf(field))) {
                continue;
            }
            ConfigGroup g = field.getAnnotation(ConfigGroup.class);
            String key = g != null ? g.value() : DEFAULT_GROUP;
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
        //? <1.20.2
        //this.renderBackground(graphics);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        if (this.resetButton != null) {
            boolean shift = Minecraft.getInstance().hasShiftDown();
            this.resetButton.active = shift;
            Component resetComponent = ComponentBuilder.translatable("config.reset").build();
            this.resetButton.setTooltip(net.minecraft.client.gui.components.Tooltip.create(
                    shift
                            ? resetComponent
                            : resetComponent.copy().append("\n").append(ComponentBuilder.translatable("config.reset.shift").build())));
        }
    }

    //? <=1.20.4 {
    /*//? >=1.20.2 {
    @Override
    public void renderBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

    //?} else {
    /^@Override
    public void renderBackground(GuiGraphicsExtractor guiGraphics) {
        super.renderBackground(guiGraphics);^///?}
        guiGraphics.setColor(0.25F, 0.25F, 0.25F, 1.0F);
        guiGraphics.blit(Screen.BACKGROUND_LOCATION, 0, 0, 0, 0.0F, 0.0F, this.width, this.height, 32, 32);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }*///?}

    @Override
    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean doubleClick) {
        AutoConfigList list = currentList();

        //~ mb_event
        if (list != null && list.isPopupOpenAt(mouseButtonEvent.x(), mouseButtonEvent.y())) { //~ !mb_event
            list.mouseClicked(mouseButtonEvent, doubleClick);
            return true;
        }

        if (list != null) {
            //~ mb_event
            list.closePickersExceptSwatchAt(mouseButtonEvent.x(), mouseButtonEvent.y()); //~ !mb_event
        }
        return super.mouseClicked(mouseButtonEvent, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent mouseButtonEvent, double dx, double dy) {
        AutoConfigList list = currentList();
        if (list != null && list.isPickerDragging()) {
            return list.mouseDragged(mouseButtonEvent, dx, dy);
        }
        return super.mouseDragged(mouseButtonEvent, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent mouseButtonEvent) {
        AutoConfigList list = currentList();
        if (list != null && list.isPickerDragging()) {
            list.mouseReleased(mouseButtonEvent);
            return true;
        }
        return super.mouseReleased(mouseButtonEvent);
    }

    @Override
    protected void repositionElements() {
        if (this.tabNavigationBar != null) {
            //? <26.1
            //this.tabNavigationBar.setWidth(this.width);
            //~ if >=26.2 || <=1.21.11 'updateWidth' -> 'arrangeElements'
            this.tabNavigationBar.arrangeElements(/*? >=26.1 >> ')'*/ this.width);
            int tabAreaTop = this.tabNavigationBar.getRectangle().bottom();
            ScreenRectangle tabArea = new ScreenRectangle(0, tabAreaTop, this.width,
                    this.height - this.layout.getFooterHeight() - tabAreaTop);
            this.tabManager.setTabArea(tabArea);
            this.layout.setHeaderHeight(tabAreaTop);
            this.layout.arrangeElements();
        }
        if (this.resetButton != null) {
            this.resetButton.setPosition(this.layout.getFooterHeight() / 2 - 10, this.height - this.layout.getFooterHeight() / 2 - 10);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        return (this.tabNavigationBar != null && this.tabNavigationBar.keyPressed(keyEvent)) || super.keyPressed(keyEvent);
    }

    @Override
    public void onClose() {
        if (!this.dirty) {
            this.holder.load();
            this.minecraft.gui.setScreen(this.lastScreen);
            return;
        }

        this.minecraft.gui.setScreen(new ConfirmScreen(
                yes -> {
                    if (yes) {
                        this.holder.load();
                        this.minecraft.gui.setScreen(this.lastScreen);
                    } else {
                        this.minecraft.gui.setScreen(this);
                    }
                },
                ComponentBuilder.translatable("config.confirm.title").build(),
                ComponentBuilder.translatable("config.confirm.message").build(),
                ComponentBuilder.translatable("config.confirm.discard").build(),
                ComponentBuilder.translatable("config.confirm.keep").build()
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
                Node node = AutoConfigScreen.this.buildNode(AutoConfigScreen.this.holder.get(), field);
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
            //? >=1.20.3
            consumer.accept(this.list);
        }

        @Override
        public void doLayout(final ScreenRectangle rectangle) {
            //? >=1.20.5 {
            this.list.updateSizeAndPosition(rectangle.width(), rectangle.height(), rectangle.top());

            //?} >=1.20.3 {
            /*this.list.setSize(rectangle.width(), rectangle.height());
            this.list.setPosition(0, rectangle.top());

            *///?} else {
            /*this.list.updateSize(rectangle.width(), rectangle.height(),
                    rectangle.top(), rectangle.top() + rectangle.height());

            for (AutoConfigList l : AutoConfigScreen.this.lists) {
                AutoConfigScreen.this.removeWidget(l);
            }
            AutoConfigScreen.this.addRenderableWidget(this.list);*///?}
        }
    }

    private Node buildNode(final Object config, final Field field) {
        return buildNode(config, field, null);
    }

    private Node buildNode(final Object config, final Field field, final String fieldPrefix) {
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
                GroupNode g = new GroupNode();
                g.label = field.getType() == Component.class
                        ? (Component) field.get(config)
                        : labelFor(field, fieldPrefix);
                g.tooltip = tooltipFor(field, fieldPrefix);
                g.expanded = false;
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

    private Node buildListNode(final Object config, final Field field) throws IllegalAccessException {
        return buildListNode(config, field, null);
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
                                        : ""));

            if (element == Integer.class) {
                model.sliderCfg = sliderCfg;
                model.colorCfg = colorCfg;
            }

            applyActions.add(() -> {
                List<Object> result = new ArrayList<>();
                for (Element e : model.elements) {
                    if (e instanceof ScalarElement s) {
                        if (element == Component.class) {
                            result.add(s.component != null ? s.component : ComponentBuilder.literal(s.value).build());
                        } else {
                            Object parsed = parseElement(element, s.value);
                            if (parsed != null) {
                                result.add(parsed);
                            }
                        }
                    }
                }
                setQuietly(config, field, result);
            });

            ListNode n = new ListNode();
            n.label = labelFor(field, fieldPrefix);
            n.tooltip = tooltipFor(field, fieldPrefix);
            n.model = model;
            return n;
        }

        if (isNestedObject(element)) {
            List<Element> initial = new ArrayList<>();
            if (current != null) {
                for (Object o : current) {
                    initial.add(buildObjectElement(element, o, field, translateElements, expandElements));
                }
            }
            final ListModel model = new ListModel(initial, editable, expanded, addToFront, element,
                    () -> buildObjectElement(element, instantiate(element), field, translateElements, expandElements));

            applyActions.add(() -> {
                List<Object> result = new ArrayList<>();
                for (Element e : model.elements) {
                    if (e instanceof ObjectElement oe) {
                        Object backing = this.objectBacking.get(oe);
                        if (backing != null) {
                            result.add(backing);
                        }
                    }
                }
                setQuietly(config, field, result);
            });

            ListNode n = new ListNode();
            n.label = labelFor(field);
            n.tooltip = tooltipFor(field);
            n.model = model;
            return n;
        }

        LOGGER.warn("Skipping list {}: unsupported element type {}", field.getName(), element.getSimpleName());
        return null;
    }

    private ObjectElement buildObjectElement(final Class<?> element, final Object obj,
                                             final Field ownerField, final boolean translateElements,
                                             final boolean expandElements) {
        GroupNode g = new GroupNode();
        g.label = elementLabel(ownerField, element, translateElements);
        g.tooltip = null;
        g.expanded = expandElements;
        for (Field cf : instanceFields(element)) {
            Node child = buildNode(obj, cf, ownerField.getName());
            if (child != null) {
                g.children.add(child);
            }
        }
        ObjectElement oe = new ObjectElement(g);
        this.objectBacking.put(oe, obj);
        return oe;
    }

    @SuppressWarnings("rawtypes")
    private AbstractWidget buildWidget(final Object config, final Field field) throws IllegalAccessException {
        Class<?> t = field.getType();

        if (t == Component.class) {
            return new StringWidget(ComponentBuilder.empty().build(), font);
        }

        if (t == boolean.class || t == Boolean.class) {
            boolean initial = (Boolean) field.get(config);
            BooleanConfig cfg = field.getAnnotation(BooleanConfig.class);
            final boolean yesNo = cfg == null || cfg.yesNo();
            return CycleButton.builder((Boolean v) -> booleanLabel(v, yesNo) /*? >=1.21.11 >> ')'*/, initial)
                    .withValues(true, false)
                    //? <1.21.11
                    //.withInitialValue(initial)
                    .displayOnlyValue()
                    .create(0, 0, DEFAULT_WIDGET_WIDTH, 20, ComponentBuilder.empty().build(),
                            (b, v) -> apply(config, field, v));
        }

        if ((t == int.class || t == Integer.class) && field.isAnnotationPresent(Slider.class)) {
            Slider r = field.getAnnotation(Slider.class);
            int initial = (Integer) field.get(config);
            return new IntSlider(r.min(), r.max(), r.step(), initial, v -> apply(config, field, v));
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
            return CycleButton.builder((Enum v) -> enumValueLabel(v, translate) /*? >=1.21.11 >> ')'*/, initial)
                    .withValues(values)
                    //? <1.21.11
                    //.withInitialValue(initial)
                    .displayOnlyValue()
                    .create(0, 0, DEFAULT_WIDGET_WIDTH, 20, ComponentBuilder.empty().build(),
                            (b, v) -> apply(config, field, v));
        }

        if (t == String.class) {
            EditBox box = new EditBox(this.font, 0, 0, DEFAULT_WIDGET_WIDTH, 20, ComponentBuilder.empty().build());
            box.setValue((String) field.get(config));
            box.setResponder(v -> apply(config, field, v));
            return box;
        }

        if (t == int.class || t == Integer.class || t == long.class || t == Long.class
                || t == float.class || t == Float.class || t == double.class || t == Double.class) {
            EditBox box = new EditBox(this.font, 0, 0, DEFAULT_WIDGET_WIDTH, 20, ComponentBuilder.empty().build());
            box.setValue(String.valueOf(field.get(config)));
            box.setResponder(v -> parseNumber(config, field, v));
            return box;
        }

        return null;
    }

    private Component labelFor(final Field field) {
        return labelFor(field, null);
    }

    private Component labelFor(final Field field, final String fieldPrefix) {
        if (!this.holder.getMeta().translate()) {
            return ComponentBuilder.literal(KeyFormatter.humanize(field.getName())).build();
        }
        String fieldKey = fieldPrefix != null
                ? key(fieldPrefix) + "." + key(field.getName())
                : key(field.getName());
        return ComponentBuilder.translatable(
                "text." + this.holder.getMeta().name() + ".option." + fieldKey,
                field.getName()).build();
    }

    private Component groupLabel(final String groupId) {
        return this.holder.getMeta().translate()
                ? ComponentBuilder.translatable("text." + this.holder.getMeta().name() + ".group." + groupId).build()
                : ComponentBuilder.literal(KeyFormatter.humanize(groupId)).build();
    }

    private Component elementLabel(final Field ownerField, final Class<?> element,
                                   final boolean translateElements) {
        if (!translateElements) {
            return ComponentBuilder.literal(element.getSimpleName()).build();
        }
        return ComponentBuilder.translatable(
                "text." + this.holder.getMeta().name() + ".option." + key(ownerField.getName()) + ".element",
                element.getSimpleName()).build();
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
        return ComponentBuilder.translatable(key).build();
    }

    private Component enumValueLabel(final Enum<?> value, final boolean translate) {
        if (!translate) {
            return ComponentBuilder.literal(value.name()).build();
        }
        if (isToStringOverridden(value)) {
            return ComponentBuilder.translatable(value.toString()).build();
        }
        String key = "text." + this.holder.getMeta().name()
                + ".enum." + key(value.getDeclaringClass().getSimpleName())
                + "." + key(value.name());
        return ComponentBuilder.translatable(key).build();
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
        return ComponentBuilder.translatable(text)
                .color(value ? ChatFormatting.GREEN : ChatFormatting.RED)
                .build();
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

    private Class<?> resolveListElementType(final Field field) {
        Type generic = field.getGenericType();
        if (generic instanceof ParameterizedType pt && pt.getActualTypeArguments().length == 1) {
            Type arg = pt.getActualTypeArguments()[0];
            if (arg instanceof Class<?> c) {
                return c;
            }
        }
        return null;
    }

    private static Object parseElement(final Class<?> type, final String raw) {
        String s = raw.trim();
        try {
            if (type == String.class) return raw;
            if (type == Boolean.class) return Boolean.parseBoolean(s);
            if (s.isEmpty()) return null;
            if (type == Integer.class) return Integer.parseInt(s);
            if (type == Long.class) return Long.parseLong(s);
            if (type == Float.class) return Float.parseFloat(s);
            if (type == Double.class) return Double.parseDouble(s);
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

    private static void parseNumber(final Object config, final Field field, final String raw) {
        Class<?> t = field.getType();
        try {
            field.setAccessible(true);
            if (t == int.class || t == Integer.class) field.set(config, Integer.parseInt(raw.trim()));
            else if (t == long.class || t == Long.class) field.set(config, Long.parseLong(raw.trim()));
            else if (t == float.class || t == Float.class) field.set(config, Float.parseFloat(raw.trim()));
            else if (t == double.class || t == Double.class) field.set(config, Double.parseDouble(raw.trim()));
        } catch (Exception ignored) {
        }
    }

    public static final class IntSlider extends AbstractSliderButton {
        private final int min, max, step;
        private final IntConsumer onApply;
        private int current;

        IntSlider(final int min, final int max, final int step, final int initial, final IntConsumer onApply) {
            super(0, 0, DEFAULT_WIDGET_WIDTH, 20, ComponentBuilder.empty().build(),
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
            setMessage(ComponentBuilder.literal(Integer.toString(this.current)).build());
        }

        @Override
        protected void applyValue() {
            //~ if >=1.21.11 'this.min, this.max, this.value' -> 'this.value, this.min, this.max'
            int raw = Mth.floor(Mth.clampedLerp(this.value, this.min, this.max));
            int snapped = this.min + Math.round((raw - this.min) / (float) this.step) * this.step;
            this.current = Mth.clamp(snapped, this.min, this.max);
            this.onApply.accept(this.current);
        }
    }
}
