//? fabric {
package io.github.bizcub.simpleConfigLib.main.platform;

import io.github.bizcub.simpleConfigLib.main.SimpleConfigLibMain;
import io.github.bizcub.simpleConfigLib.util.network.Network;
import io.github.bizcub.simpleConfigLib.util.network.PayloadRegistryFabric;
import io.github.bizcub.simpleConfigLib.util.network.SclPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.BiConsumer;

public class FabricMain implements ModInitializer {

    @Override
    @SuppressWarnings("unchecked")
    public void onInitialize() {
        SimpleConfigLibMain.registerPayloads();

        for (Network.Serverbound<?> registration : Network.serverbound()) {
            PayloadRegistryFabric.registerServerbound(
                    (Class<SclPayload>) registration.type(),
                    (BiConsumer<SclPayload, ServerPlayer>) registration.handler());
        }
        for (Network.Clientbound<?> registration : Network.clientbound()) {
            PayloadRegistryFabric.registerClientbound(registration.type());
        }

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                SimpleConfigLibMain.onPlayerJoin(handler.player));
    }
}//?}
