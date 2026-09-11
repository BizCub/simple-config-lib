//? >=1.21.6 {
package io.github.bizcub.simpleConfigLib.util.widget;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
//? if >=26.3 {
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuTextureView;
//?} else {
/*import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
*///?}
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
/*? <26.2*/ //import net.minecraft.client.renderer.MultiBufferSource;
/*? >=26.2*/ import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.state.gui.BlitRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.RenderPipelines;
//? >=1.21.9 {
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;//?}
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ARGB;

public class ScaledItemPIPRenderer extends PictureInPictureRenderer<ScaledItemRenderState> {

    //? >=26.2 {
    public ScaledItemPIPRenderer() {
        super();
    }

    //?} else {
    /*public ScaledItemPIPRenderer(MultiBufferSource.BufferSource bufferSource) {
        super(bufferSource);
    }*///?}

    @Override
    protected void renderToTexture(ScaledItemRenderState state, PoseStack poseStack /*? >=26.2 >> ')'*/, SubmitNodeCollector submitNodeCollector) {
        poseStack.scale(1.0F, -1.0F, -1.0F);

        ItemStackRenderState itemState = state.itemStackRenderState();

        //~ if >=26.2 'getLighting()' -> 'lighting()' {
        if (itemState.usesBlockLight()) {
            Minecraft.getInstance().gameRenderer.lighting().setupFor(Lighting.Entry.ITEMS_3D);
        } else {
            Minecraft.getInstance().gameRenderer.lighting().setupFor(Lighting.Entry.ITEMS_FLAT);
        }//~}

        //? >=26.3 {
        itemState.submit(poseStack, submitNodeCollector, 15728880, OverlayTexture.NO_OVERLAY, 0);

        //?} >=26.2 {
        /*itemState.submit(poseStack, submitNodeCollector, 15728880, OverlayTexture.NO_OVERLAY, 0);
        Minecraft.getInstance().gameRenderer.featureRenderDispatcher().renderAllFeatures(new SubmitNodeStorage());

        *///?} >=1.21.9 {
        /*SubmitNodeCollector submitNodeCollector = Minecraft.getInstance().gameRenderer.getSubmitNodeStorage();
        FeatureRenderDispatcher featureRenderDispatcher = Minecraft.getInstance().gameRenderer.getFeatureRenderDispatcher();

        itemState.submit(poseStack, submitNodeCollector, 15728880, OverlayTexture.NO_OVERLAY, 0);
        featureRenderDispatcher.renderAllFeatures();

        *///?} else {
        /*itemState.extractRenderState(poseStack, this.bufferSource, 15728880, OverlayTexture.NO_OVERLAY);*///?}
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
                /*? >=1.21.11*/ TextureSetup.singleTexture(textureView, RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR)),
                /*? <=1.21.10*/ //TextureSetup.singleTexture(textureView),
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
}//?}
