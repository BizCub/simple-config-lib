package io.github.bizcub.simpleConfigLib.platform;

import io.github.bizcub.simpleConfigLib.SimpleConfigLib;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class FabricEntrypoint implements ModInitializer {

    @Override
    public void onInitialize() {
        SimpleConfigLib.registerPayloads();

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                SimpleConfigLib.onPlayerJoin(handler.player));

        CommandRegistrationCallback.EVENT.register((dispatcher, registry, env) ->
                dispatcher.register(SimpleConfigLib.buildReloadCommand()));
    }
}
