//? fabric {
package io.github.bizcub.simpleConfigLib.main.platform;

import io.github.bizcub.simpleConfigLib.autoconfig.network.ConfigApplyPayload;
import io.github.bizcub.simpleConfigLib.autoconfig.network.ConfigSyncPayload;
import io.github.bizcub.simpleConfigLib.main.SimpleConfigLibMain;
import net.fabricmc.api.ModInitializer;
/*? >=1.20.5*/ import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public class FabricMain implements ModInitializer {

    @Override
    public void onInitialize() {
        //? >=1.20.5 {
        /*~ if >=26.1 'playC2S' -> 'serverboundPlay'*/ /*~ if >=26.1 'playS2C' -> 'clientboundPlay' {*/
        PayloadTypeRegistry.serverboundPlay().register(ConfigApplyPayload.TYPE, ConfigApplyPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ConfigSyncPayload.TYPE, ConfigSyncPayload.CODEC);//~}

        ServerPlayNetworking.registerGlobalReceiver(ConfigApplyPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            //~ if !1.20.5 'player().getServer()' -> 'server()'
            context.server().execute(() -> SimpleConfigLibMain.handleApply(player, payload));
        });

        //?} else {
        /*ServerPlayNetworking.registerGlobalReceiver(ConfigApplyPayload.ID, (server, player, listener, buf, sender) ->
                server.execute(() -> SimpleConfigLibMain.handleApply(player, ConfigApplyPayload.read(buf))));*///?}

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                SimpleConfigLibMain.onPlayerJoin(handler.player));
    }
}//?}
