package io.github.bizcub.simpleConfigLib.platform.client;

import io.github.bizcub.simpleConfigLib.SimpleConfigLib;
import io.github.bizcub.simpleConfigLib.autoconfig.ConfigHolder;
import io.github.bizcub.simpleConfigLib.autoconfig.network.ConfigSyncPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class FabricClientEntrypoint implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(ConfigSyncPayload.TYPE, (payload, context) ->
                context.client().execute(() -> ConfigHolder.applyServerSnapshot(payload.name(), payload.json())));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> SimpleConfigLib.onClientDisconnect());
    }
}
