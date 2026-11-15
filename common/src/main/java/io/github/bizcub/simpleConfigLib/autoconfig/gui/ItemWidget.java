package io.github.bizcub.simpleConfigLib.autoconfig.gui;

import io.github.bizcub.simpleConfigLib.internal.GuiSounds;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

final class ItemWidget extends AbstractWidget {
    private final Font font;
    private final Consumer<String> onChange;
    private final Consumer<ItemWidget> open;
    private String value;
    private ItemStack icon;

    ItemWidget(Font font, String value, Consumer<String> onChange, Consumer<ItemWidget> open) {
        super(0, 0, 150, 20, Component.empty());
        this.font = font;
        this.onChange = onChange;
        this.open = open;
        update(value);
    }

    String value() { return this.value; }

    void select(String value) {
        update(value);
        this.onChange.accept(value);
    }

    private void update(String value) {
        this.value = value;
        Identifier id = Identifier.tryParse(value);
        boolean available = id != null && BuiltInRegistries.ITEM.containsKey(id);
        Item item = available ? BuiltInRegistries.ITEM.getValue(id) : Items.BARRIER;
        this.icon = previewStack(item);
        setMessage(available ? previewName(item, this.icon) : Component.translatable("text.simple_config_lib.item.unavailable"));
        setTooltip(Tooltip.create(Component.literal(value)));
    }

    static ItemStack previewStack(Item item) {
        if (BuiltInRegistries.ITEM.wrapAsHolder(item).areComponentsBound()) return item.getDefaultInstance();
        return new ItemStack(Holder.direct(item, DataComponentMap.builder()
                .set(DataComponents.ITEM_NAME, Component.translatable(item.getDescriptionId()))
                .set(DataComponents.ITEM_MODEL, BuiltInRegistries.ITEM.getKey(item))
                .build()));
    }

    static Component previewName(Item item, ItemStack stack) {
        return BuiltInRegistries.ITEM.wrapAsHolder(item).areComponentsBound() ? stack.getHoverName() : Component.translatable(item.getDescriptionId());
    }

    static void renderIcon(GuiGraphicsExtractor graphics, Font font, ItemStack stack, int x, int y) {
        try {
            graphics.fakeItem(stack, x, y);
        } catch (RuntimeException ignored) {
            graphics.text(font, "?", x + 5, y + 4, 0xFFAAAAAA);
        }
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int border = isFocused() ? 0xFFFFFFFF : 0xFF000000;
        graphics.fill(getX() - 1, getY() - 1, getRight() + 1, getBottom() + 1, border);
        graphics.fill(getX(), getY(), getRight(), getBottom(), active && isHovered ? 0xFF404040 : 0xFF202020);
        renderIcon(graphics, this.font, this.icon, getX() + 2, getY() + 2);
        graphics.text(this.font, this.font.plainSubstrByWidth(getMessage().getString(), Math.max(0, getWidth() - 36)),
                getX() + 22, getY() + 6, active ? 0xFFFFFFFF : 0xFF777777);
        graphics.text(this.font, Component.literal("▾"), getRight() - 10, getY() + 6, 0xFFAAAAAA);
    }

    private void open() {
        GuiSounds.playClick();
        this.open.accept(this);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (!active || !visible || event.button() != 1 || !isMouseOver(event.x(), event.y())) return false;
        open();
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (!active || !visible || !event.isConfirmation()) return false;
        open();
        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, getMessage().copy().append(" " + this.value));
    }
}
