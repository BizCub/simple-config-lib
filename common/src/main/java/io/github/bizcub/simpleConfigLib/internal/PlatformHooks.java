package io.github.bizcub.simpleConfigLib.internal;

import io.github.bizcub.simpleConfigLib.util.network.SclPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.nio.file.Path;
import java.util.function.BiConsumer;

public interface PlatformHooks {

    static PlatformHooks get() {
        return Holder.INSTANCE;
    }

    boolean isModLoaded(String modId);

    boolean isPhysicalClient();

    Path gameDir();

    void sendToPlayer(ServerPlayer player, SclPayload payload);

    void sendToAllPlayers(MinecraftServer server, SclPayload payload);

    <T extends SclPayload> void registerServerbound(Class<T> type, BiConsumer<T, ServerPlayer> handler);

    <T extends SclPayload> void registerClientboundType(Class<T> type);

    class Holder {
        static final PlatformHooks INSTANCE = java.util.ServiceLoader
                .load(PlatformHooks.class)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No SCL platform implementation found"));
    }
}
