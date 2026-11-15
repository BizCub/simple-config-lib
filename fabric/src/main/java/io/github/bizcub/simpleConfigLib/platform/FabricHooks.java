package io.github.bizcub.simpleConfigLib.platform;

import io.github.bizcub.simpleConfigLib.SimpleConfigLib;
import io.github.bizcub.simpleConfigLib.autoconfig.network.ConfigApplyPayload;
import io.github.bizcub.simpleConfigLib.autoconfig.network.ConfigSyncPayload;
import io.github.bizcub.simpleConfigLib.internal.PlatformHooks;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import java.nio.file.Path;

public class FabricHooks implements PlatformHooks {

    @Override
    public boolean isPhysicalClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    @Override
    public Path gameDir() {
        return FabricLoader.getInstance().getGameDir();
    }

    @Override
    public void sendToPlayer(ServerPlayer player, ConfigSyncPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    @Override
    public void sendToAllPlayers(MinecraftServer server, ConfigSyncPayload payload) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    static void registerPayloads() {
        PayloadTypeRegistry.serverboundPlay().register(ConfigApplyPayload.TYPE, ConfigApplyPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ConfigSyncPayload.TYPE, ConfigSyncPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ConfigApplyPayload.TYPE, (payload, context) ->
                context.server().execute(() -> SimpleConfigLib.handleApply(payload, context.player())));
    }
}
