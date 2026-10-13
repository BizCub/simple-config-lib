package io.github.bizcub.simpleConfigLib.util.widget;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.state.gui.BlitRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ARGB;

public class ScaledItemPIPRenderer extends PictureInPictureRenderer<ScaledItemRenderState> {

    public ScaledItemPIPRenderer() {
        super();
    }

    @Override
    protected void renderToTexture(ScaledItemRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        poseStack.scale(1.0F, -1.0F, -1.0F);
        ItemStackRenderState itemState = state.itemStackRenderState();
        Minecraft.getInstance().gameRenderer.lighting().setupFor(itemState.usesBlockLight() ? Lighting.Entry.ITEMS_3D : Lighting.Entry.ITEMS_FLAT);
        itemState.submit(poseStack, submitNodeCollector, 15728880, OverlayTexture.NO_OVERLAY, 0);
    }

    @Override
    protected void blitTexture(ScaledItemRenderState state, GuiRenderState guiRenderState) {
        GpuTextureView textureView = this.textureView;
        if (textureView == null) return;

        float alpha = state.alpha();
        int intAlpha = Math.min(255, Math.max(0, (int) (alpha * 255)));

        int r = (int) (255 * alpha);
        int g = (int) (255 * alpha);
        int b = (int) (255 * alpha);
        int color = ARGB.color(intAlpha, r, g, b);

        guiRenderState.addBlitToCurrentLayer(new BlitRenderState(
                RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA,
                TextureSetup.singleTexture(textureView, RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR)),
                state.pose(),
                (int) state.renderX0(),
                (int) state.renderY0(),
                (int) Math.ceil(state.renderX1()),
                (int) Math.ceil(state.renderY1()),
                0.0F,
                1.0F,
                1.0F,
                0.0F,
                color,
                state.scissorArea(),
                (ScreenRectangle) null
        ));
    }

    @Override
    public Class<ScaledItemRenderState> getRenderStateClass() {
        return ScaledItemRenderState.class;
    }

    @Override
    protected float getTranslateY(int height, int guiScale) {
        return (float) height / 2.0F;
    }

    @Override
    protected String getTextureLabel() {
        return "Item Transform";
    }

    @Override
    protected boolean textureIsReadyToBlit(ScaledItemRenderState state) {
        return false;
    }
}
