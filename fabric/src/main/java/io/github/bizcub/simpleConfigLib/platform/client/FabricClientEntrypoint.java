package io.github.bizcub.simpleConfigLib.platform.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.github.bizcub.simpleConfigLib.SimpleConfigLib;
import io.github.bizcub.simpleConfigLib.autoconfig.ConfigHolder;
import io.github.bizcub.simpleConfigLib.autoconfig.gui.ConfigScreens;
import io.github.bizcub.simpleConfigLib.autoconfig.network.ConfigResultPayload;
import io.github.bizcub.simpleConfigLib.autoconfig.network.ConfigSyncPayload;
import io.github.bizcub.simpleConfigLib.client.SimpleConfigClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class FabricClientEntrypoint implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        SimpleConfigClient.getInstance();
        ClientPlayNetworking.registerGlobalReceiver(ConfigSyncPayload.TYPE, (payload, context) ->
                context.client().execute(() -> ConfigHolder.applyServerSnapshot(payload.name(), payload.json())));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> SimpleConfigLib.onClientDisconnect());
        ClientPlayNetworking.registerGlobalReceiver(ConfigResultPayload.TYPE, (payload, context) ->
                context.client().execute(() -> ConfigScreens.handleResult(payload)));
    }

    public static class ModMenu implements ModMenuApi {
        @Override
        public ConfigScreenFactory<?> getModConfigScreenFactory() {
            return parent -> ConfigScreens.open(SimpleConfigClient.getInstance(), parent);
        }
    }
}
