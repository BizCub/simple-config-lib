//? fabric {
package io.github.bizcub.simpleConfigLib.platform;

import io.github.bizcub.simpleConfigLib.autoconfig.ConfigHolder;
import io.github.bizcub.simpleConfigLib.autoconfig.annotation.Side;
import io.github.bizcub.simpleConfigLib.autoconfig.network.ConfigSyncPayload;
import io.github.bizcub.simpleConfigLib.util.Keymapper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

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

        ClientPlayNetworking.registerGlobalReceiver(ConfigSyncPayload.TYPE, (payload, context) ->
                context.client().execute(() -> {
                    ConfigHolder<?> holder = ConfigHolder.byName(payload.name());
                    if (holder != null && holder.getMeta().env() == Side.Env.SERVER) {
                        holder.applySnapshot(payload.json());
                    }
                }));
    }
}//?}
