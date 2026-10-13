package io.github.bizcub.simpleConfigLib.util.network;

import io.github.bizcub.simpleConfigLib.internal.PlatformHooks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class Network {

    public record Serverbound<T extends SclPayload>(Class<T> type, BiConsumer<T, ServerPlayer> handler) {}
    public record Clientbound<T extends SclPayload>(Class<T> type, Consumer<T> handler) {}

    private static final List<Serverbound<?>> SERVERBOUND = new ArrayList<>();
    private static final List<Clientbound<?>> CLIENTBOUND = new ArrayList<>();

    public static <T extends SclPayload> void registerServerbound(Class<T> type, BiConsumer<T, ServerPlayer> handler) {
        PayloadRegistry.validate(type);
        for (Serverbound<?> existing : SERVERBOUND) {
            if (existing.type() == type) {
                return;
            }
        }
        SERVERBOUND.add(new Serverbound<>(type, handler));
        PlatformHooks.get().registerServerbound(type, handler);
    }

    public static <T extends SclPayload> void registerClientbound(Class<T> type, Consumer<T> handler) {
        PayloadRegistry.validate(type);
        for (Clientbound<?> existing : CLIENTBOUND) {
            if (existing.type() == type) {
                return;
            }
        }
        CLIENTBOUND.add(new Clientbound<>(type, handler));
        PlatformHooks.get().registerClientboundType(type);
    }

    public static List<Serverbound<?>> serverbound() {
        return new ArrayList<>(SERVERBOUND);
    }

    public static List<Clientbound<?>> clientbound() {
        return new ArrayList<>(CLIENTBOUND);
    }

    public static void sendToPlayer(ServerPlayer player, SclPayload payload) {
        PlatformHooks.get().sendToPlayer(player, payload);
    }

    public static void sendToAllPlayers(MinecraftServer server, SclPayload payload) {
        PlatformHooks.get().sendToAllPlayers(server, payload);
    }
}
