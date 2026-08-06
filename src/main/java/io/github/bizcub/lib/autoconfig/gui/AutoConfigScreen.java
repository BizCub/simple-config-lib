package io.github.bizcub.lib.autoconfig.gui;

import io.github.bizcub.lib.autoconfig.ConfigHolder;
import io.github.bizcub.lib.autoconfig.annotation.*;
import io.github.bizcub.lib.autoconfig.annotation.*;
import io.github.bizcub.lib.autoconfig.annotation.*;
import io.github.bizcub.lib.autoconfig.annotation.Tooltip;
import io.github.bizcub.lib.util.KeyFormatter;
import io.github.bizcub.lib.autoconfig.gui.AutoConfigList.Element;
import io.github.bizcub.lib.autoconfig.gui.AutoConfigList.GroupNode;
import io.github.bizcub.lib.autoconfig.gui.AutoConfigList.ListModel;
import io.github.bizcub.lib.autoconfig.gui.AutoConfigList.ListNode;
import io.github.bizcub.lib.autoconfig.gui.AutoConfigList.Node;
import io.github.bizcub.lib.autoconfig.gui.AutoConfigList.ObjectElement;
import io.github.bizcub.lib.autoconfig.gui.AutoConfigList.ScalarElement;
import io.github.bizcub.lib.autoconfig.gui.AutoConfigList.WidgetNode;
import com.mojang.logging.LogUtils;
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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

//? >=1.21.9 {
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;//?}

public class AutoConfigScreen extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String DEFAULT_GROUP = "general";

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

    public AutoConfigScreen(final Screen lastScreen, final ConfigHolder<?> holder) {
        super(Component.translatable("config." + holder.name() + ".title"));
        this.lastScreen = lastScreen;
        this.holder = holder;
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
            this.holder.save();
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
                Button.builder(Component.literal("\uD83D\uDDD8"), b -> {
                            onReset();
                            markDirty();
                        })
                        .size(20, 20)
                        .pos(this.layout.getFooterHeight() / 2 - 10, this.height - this.layout.getFooterHeight() / 2 - 10)
                        .build());
        this.resetButton.active = Minecraft.getInstance().hasShiftDown();
    }

    public void markDirty() {
        this.dirty = true;
        if (this.doneButton != null) {
            this.doneButton.active = true;
        }
    }

    private void onReset() {
        this.holder.reset();
        this.rebuildWidgets();
    }

    private String key(final String raw) {
        return this.holder.snakeCaseKeys() ? KeyFormatter.toSnakeCase(raw) : raw;
    }

    private Map<String, List<Field>> groupFields() {
        Map<String, List<Field>> groups = new LinkedHashMap<>();
        groups.put(DEFAULT_GROUP, new ArrayList<>());
        for (Field field : this.holder.type().getDeclaredFields()) {
            int mods = field.getModifiers();
            if (Modifier.isStatic(mods) || Modifier.isTransient(mods)) {
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
        if (this.resetButton != null) {
            boolean shift = Minecraft.getInstance().hasShiftDown();
            this.resetButton.active = shift;
            Component resetComponent = Component.translatable("config.reset");
            this.resetButton.setTooltip(net.minecraft.client.gui.components.Tooltip.create(
                    shift
                            ? resetComponent
                            : resetComponent.copy().append("\n").append(Component.translatable("config.reset.shift"))));
        }
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

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
                Component.translatable("config.confirm.title"),
                Component.translatable("config.confirm.message"),
                Component.translatable("config.confirm.discard"),
                Component.translatable("config.confirm.keep")
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

            //?} >=1.20.4 {
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
        field.setAccessible(true);
        try {
            Class<?> t = field.getType();

            if (List.class.isAssignableFrom(t)) {
                return buildListNode(config, field);
            }

            AbstractWidget widget = buildWidget(config, field);
            if (widget != null) {
                WidgetNode n = new WidgetNode();
                n.label = field.getType() == Component.class
                        ? (Component) field.get(config)
                        : labelFor(field);

                n.tooltip = tooltipFor(field);
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
                        : labelFor(field);
                g.tooltip = tooltipFor(field);
                g.expanded = false;
                for (Field cf : instanceFields(t)) {
                    Node child = buildNode(value, cf);
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
        final Class<?> element = resolveListElementType(field);
        if (element == null) {
            LOGGER.warn("Skipping list {}: element type could not be determined", field.getName());
            return null;
        }

        ListConfig cfg = field.getAnnotation(ListConfig.class);
        final boolean expanded = cfg != null && cfg.expanded();
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
                            result.add(s.component != null ? s.component : Component.literal(s.value));
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
            n.label = labelFor(field);
            n.tooltip = tooltipFor(field);
            n.model = model;
            return n;
        }

        if (isNestedObject(element)) {
            List<Element> initial = new ArrayList<>();
            if (current != null) {
                for (Object o : current) {
                    initial.add(buildObjectElement(element, o, field, translateElements));
                }
            }
            final ListModel model = new ListModel(initial, editable, expanded, addToFront, element,
                    () -> buildObjectElement(element, instantiate(element), field, translateElements));

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
                                             final Field ownerField, final boolean translateElements) {
        GroupNode g = new GroupNode();
        g.label = elementLabel(ownerField, element, translateElements);
        g.tooltip = null;
        g.expanded = false;
        for (Field cf : instanceFields(element)) {
            Node child = buildNode(obj, cf);
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
            return new StringWidget(Component.empty(), font);
        }

        if (t == boolean.class || t == Boolean.class) {
            boolean initial = (Boolean) field.get(config);
            return CycleButton.onOffBuilder(initial)
                    .displayOnlyValue()
                    .create(0, 0, AutoConfigList.WIDGET_WIDTH, 20, Component.empty(),
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
            final boolean translate = this.holder.translate() && cfg != null && cfg.translate();
            return CycleButton.builder((Enum v) -> enumValueLabel(v, translate) /*? >=1.21.11 >> ')'*/, initial)
                    .withValues(values)
                    //? <1.21.11
                    //.withInitialValue(initial)
                    .displayOnlyValue()
                    .create(0, 0, AutoConfigList.WIDGET_WIDTH, 20, Component.empty(),
                            (b, v) -> apply(config, field, v));
        }

        if (t == String.class) {
            EditBox box = new EditBox(this.font, 0, 0, AutoConfigList.WIDGET_WIDTH, 20, Component.empty());
            box.setValue((String) field.get(config));
            box.setResponder(v -> apply(config, field, v));
            return box;
        }

        if (t == int.class || t == Integer.class || t == long.class || t == Long.class
                || t == float.class || t == Float.class || t == double.class || t == Double.class) {
            EditBox box = new EditBox(this.font, 0, 0, AutoConfigList.WIDGET_WIDTH, 20, Component.empty());
            box.setValue(String.valueOf(field.get(config)));
            box.setResponder(v -> parseNumber(config, field, v));
            return box;
        }

        return null;
    }

    private Component labelFor(final Field field) {
        if (!this.holder.translate()) {
            return Component.literal(KeyFormatter.humanize(field.getName()));
        }
        return Component.translatable(
                "config." + this.holder.name() + ".option." + key(field.getName()), field.getName());
    }

    private Component groupLabel(final String groupId) {
        return this.holder.translate()
                ? Component.translatable("config." + this.holder.name() + ".group." + groupId)
                : Component.literal(KeyFormatter.humanize(groupId));
    }

    private Component elementLabel(final Field ownerField, final Class<?> element,
                                   final boolean translateElements) {
        if (!translateElements) {
            return Component.literal(element.getSimpleName());
        }
        return Component.translatable(
                "config." + this.holder.name() + ".option." + key(ownerField.getName()) + ".element",
                element.getSimpleName());
    }

    private Component tooltipFor(final Field field) {
        Tooltip tip = field.getAnnotation(Tooltip.class);
        if (tip == null) {
            return null;
        }
        String key = "config." + this.holder.name() + ".option." + key(field.getName()) + ".tooltip";
        return Component.translatable(key);
    }

    private Component enumValueLabel(final Enum<?> value, final boolean translate) {
        if (!translate) {
            return Component.literal(value.name());
        }
        String key = "config." + this.holder.name()
                + ".enum." + key(value.getDeclaringClass().getSimpleName())
                + "." + key(value.name());
        return Component.translatable(key);
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
            super(0, 0, AutoConfigList.WIDGET_WIDTH, 20, Component.empty(),
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
            //~ if >=1.21.11 'this.min, this.max, this.value' -> 'this.value, this.min, this.max'
            int raw = Mth.floor(Mth.clampedLerp(this.value, this.min, this.max));
            int snapped = this.min + Math.round((raw - this.min) / (float) this.step) * this.step;
            this.current = Mth.clamp(snapped, this.min, this.max);
            this.onApply.accept(this.current);
        }
    }
}
