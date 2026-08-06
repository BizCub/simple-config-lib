//? fabric {
package io.github.bizcub.lib.platform;

import io.github.bizcub.lib.util.Keymapper;
import net.fabricmc.api.ModInitializer;

//? >=1.21.6 {
import io.github.bizcub.lib.util.widget.ScaledItemPIPRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.PictureInPictureRendererRegistry;
//?}

public class Fabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Keymapper.onRegisterKeyMappings();

        //? >=1.21.6 {
        PictureInPictureRendererRegistry.register(ctx ->
                new ScaledItemPIPRenderer(/*? <26.2 >>+ ')'*/ /*ctx.bufferSource()*/));//?}
    }
}//?}
