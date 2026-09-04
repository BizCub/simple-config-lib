//? fabric {
package io.github.bizcub.simpleConfigLib.platform;

import io.github.bizcub.simpleConfigLib.util.Keymapper;
import net.fabricmc.api.ClientModInitializer;

//? >=1.21.6 {
import io.github.bizcub.simpleConfigLib.util.widget.ScaledItemPIPRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.PictureInPictureRendererRegistry;
//?}

public class Fabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Keymapper.onRegisterKeyMappings();

        //? >=1.21.6 {
        PictureInPictureRendererRegistry.register(ctx ->
                new ScaledItemPIPRenderer(/*? <26.2 >>+ ')'*/ /*ctx.bufferSource()*/));//?}
    }
}//?}
