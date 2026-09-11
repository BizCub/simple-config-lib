//? fabric {
package io.github.bizcub.simpleConfigLib.client.platform;

import io.github.bizcub.simpleConfigLib.autoconfig.ConfigHolder;
import io.github.bizcub.simpleConfigLib.autoconfig.network.ConfigSyncPayload;
import io.github.bizcub.simpleConfigLib.util.Keymapper;
import io.github.bizcub.simpleConfigLib.util.network.PayloadRegistryFabricClient;
import net.fabricmc.api.ClientModInitializer;

//? >=1.21.6 {
import io.github.bizcub.simpleConfigLib.util.widget.ScaledItemPIPRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.PictureInPictureRendererRegistry;
//?}

public class FabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Keymapper.onRegisterKeyMappings();

        //? >=1.21.6 {
        PictureInPictureRendererRegistry.register(ctx ->
                new ScaledItemPIPRenderer(/*? <26.2 >>+ ')'*/ /*ctx.bufferSource()*/));//?}

        PayloadRegistryFabricClient.registerClientboundReceiver(
                ConfigSyncPayload.class,
                (payload, client) ->
                        ConfigHolder.applyServerSnapshot(payload.name(), payload.json()));
    }
}//?}
