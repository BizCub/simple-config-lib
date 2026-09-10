//? fabric {
package io.github.bizcub.simpleConfigLib.main.platform;

import io.github.bizcub.simpleConfigLib.autoconfig.network.ConfigApplyPayload;
import io.github.bizcub.simpleConfigLib.autoconfig.network.ConfigSyncPayload;
import io.github.bizcub.simpleConfigLib.main.SimpleConfigLibMain;
import io.github.bizcub.simpleConfigLib.util.network.PayloadRegistryFabric;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class FabricMain implements ModInitializer {

    @Override
    public void onInitialize() {
        PayloadRegistryFabric.registerServerbound(ConfigApplyPayload.class, SimpleConfigLibMain::handleApply);

        PayloadRegistryFabric.registerClientbound(ConfigSyncPayload.class);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                SimpleConfigLibMain.onPlayerJoin(handler.player));
    }
}//?}
