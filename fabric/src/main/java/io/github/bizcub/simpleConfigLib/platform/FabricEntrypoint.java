package io.github.bizcub.simpleConfigLib.platform;

import io.github.bizcub.simpleConfigLib.SimpleConfigLib;
import io.github.bizcub.simpleConfigLib.util.network.Network;
import io.github.bizcub.simpleConfigLib.util.network.SclPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.BiConsumer;

public class FabricEntrypoint implements ModInitializer {

    @Override
    @SuppressWarnings("unchecked")
    public void onInitialize() {
        SimpleConfigLib.registerPayloads();

        for (Network.Serverbound<?> registration : Network.serverbound()) {
            FabricHooks.registerServerbound(
                    (Class<SclPayload>) registration.type(),
                    (BiConsumer<SclPayload, ServerPlayer>) registration.handler());
        }

        for (Network.Clientbound<?> registration : Network.clientbound()) {
            FabricHooks.registerClientboundType(registration.type());
        }

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                SimpleConfigLib.onPlayerJoin(handler.player));

        CommandRegistrationCallback.EVENT.register((dispatcher, registry, env) ->
                dispatcher.register(SimpleConfigLib.buildReloadCommand()));
    }
}
