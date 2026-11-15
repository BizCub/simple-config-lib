package io.github.bizcub.simpleConfigLib.internal;

import io.github.bizcub.simpleConfigLib.autoconfig.network.ConfigSyncPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import java.nio.file.Path;

public interface PlatformHooks {
    static PlatformHooks get() {
        return Holder.INSTANCE;
    }

    boolean isPhysicalClient();
    Path gameDir();
    void sendToPlayer(ServerPlayer player, CustomPacketPayload payload);
    void sendToAllPlayers(MinecraftServer server, ConfigSyncPayload payload);

    class Holder {
        static final PlatformHooks INSTANCE = PlatformLoader.create(PlatformHooks.class, "%sHooks");
    }
}
