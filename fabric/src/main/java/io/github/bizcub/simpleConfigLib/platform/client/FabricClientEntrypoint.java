package io.github.bizcub.simpleConfigLib.platform.client;

import io.github.bizcub.simpleConfigLib.autoconfig.ConfigHolder;
import io.github.bizcub.simpleConfigLib.autoconfig.network.ConfigSyncPayload;
import io.github.bizcub.simpleConfigLib.internal.ClientHooks;
import io.github.bizcub.simpleConfigLib.SimpleConfigLib;
import io.github.bizcub.simpleConfigLib.util.Keymapper;
import io.github.bizcub.simpleConfigLib.util.widget.ScaledItemPIPRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.PictureInPictureRendererRegistry;

public class FabricClientEntrypoint implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Keymapper.onRegisterKeyMappings();

        PictureInPictureRendererRegistry.register(ctx -> new ScaledItemPIPRenderer());

        ClientHooks.get().registerClientboundReceiver(
                ConfigSyncPayload.class,
                (payload, client) ->
                        ConfigHolder.applyServerSnapshot(payload.name(), payload.json()));

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) ->
                SimpleConfigLib.onClientDisconnect());
    }
}
