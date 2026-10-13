package io.github.bizcub.simpleConfigLib.util.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ScaledItemDisplayWidget extends AbstractWidget {

    private ItemStack itemStack;
    private int offsetX;
    private int offsetY;
    private int size;

    public ScaledItemDisplayWidget(int offsetX, int offsetY, ItemStack itemStack, int size) {
        super(offsetX, offsetY, size, size, itemStack.getItemName());
        this.itemStack = itemStack;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.size = size;
    }

    public void updateItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public int getSize() {
        return size;
    }

    public void setOffsetX(int offsetX) {
        this.offsetX = offsetX;
    }

    public void setOffsetY(int offsetY) {
        this.offsetY = offsetY;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setCentred() {
        setOffsetX(getX() - getSize() / 2);
        setOffsetY(getY() - getSize() / 2);
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        var itemStackRenderState = new ItemStackRenderState();
        Minecraft mc = Minecraft.getInstance();

        mc.getItemModelResolver().updateForTopItem(itemStackRenderState, itemStack, ItemDisplayContext.GUI, mc.level, mc.player, 0);

        ScaledItemRenderState state = new ScaledItemRenderState(
                itemStackRenderState,
                offsetX,
                offsetY,
                offsetX + size,
                offsetY + size,
                0.0f,
                1.0f,
                null
        );

        graphics.guiRenderState.addPicturesInPictureState(state);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }
}
