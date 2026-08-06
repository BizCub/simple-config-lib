package io.github.bizcub.lib.util.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.world.item.ItemStack;

//? >=1.21.6 {
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemDisplayContext;//?}
//? >=1.20 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.renderer.entity.ItemRenderer;
 *///?}
//? >=1.19 {
import net.minecraft.client.gui.narration.NarrationElementOutput;
//?}

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

    //? <1.20 {
    /*public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }*///?}

    @Override //$ render_method {
    protected void extractWidgetRenderState(GuiGraphicsExtractor/*$}*/ graphics, int mouseX, int mouseY, float partialTick) {
        //? >=1.21.6 {
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

        //~ if >=26.1 'submitPicturesInPictureState' -> 'addPicturesInPictureState'
        graphics.guiRenderState.addPicturesInPictureState(state);

        //?} >=1.20 {
        /*var pose = graphics.pose();
        int scale = size / 16;

        pose.pushPose();
        pose.scale(scale, scale, scale);
        graphics.renderItem(itemStack, offsetX / scale, offsetY / scale);
        pose.popPose();

        *///?} >=1.19 {
        /*if (this.itemStack == null || this.itemStack.isEmpty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        ItemRenderer itemRenderer = minecraft.getItemRenderer();
        int scale = size / 16;

        PoseStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushPose();
        modelViewStack.translate(this.offsetX, this.offsetY, 0.0);
        modelViewStack.scale(scale, scale, scale);
        RenderSystem.applyModelViewMatrix();

        itemRenderer.renderAndDecorateItem(this.itemStack, 0, 0);

        modelViewStack.popPose();
        RenderSystem.applyModelViewMatrix();

        *///?} else {
        /*if (this.itemStack == null || this.itemStack.isEmpty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        ItemRenderer itemRenderer = minecraft.getItemRenderer();
        int scale = size / 16;

        RenderSystem.pushMatrix();
        RenderSystem.translatef(this.offsetX, this.offsetY, 0.0F);
        RenderSystem.scalef(scale, scale, scale);

        itemRenderer.renderAndDecorateItem(this.itemStack, 0, 0);

        RenderSystem.popMatrix();*///?}
    }

    //? >=1.19 {
    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }//?}
}
