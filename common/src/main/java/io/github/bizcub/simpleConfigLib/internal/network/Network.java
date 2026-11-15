package io.github.bizcub.simpleConfigLib.internal.network;

import io.github.bizcub.simpleConfigLib.autoconfig.network.ConfigSyncPayload;
import io.github.bizcub.simpleConfigLib.internal.PlatformHooks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public final class Network {
    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        PlatformHooks.get().sendToPlayer(player, payload);
    }

    public static void sendToAllPlayers(MinecraftServer server, ConfigSyncPayload payload) {
        PlatformHooks.get().sendToAllPlayers(server, payload);
    }
}
