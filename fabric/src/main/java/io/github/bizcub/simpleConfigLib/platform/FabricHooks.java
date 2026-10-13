package io.github.bizcub.simpleConfigLib.platform;

import io.github.bizcub.simpleConfigLib.internal.PlatformHooks;
import io.github.bizcub.simpleConfigLib.util.network.PayloadRegistry;
import io.github.bizcub.simpleConfigLib.util.network.SclPayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.nio.file.Path;
import java.util.function.BiConsumer;

public class FabricHooks implements PlatformHooks {

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isPhysicalClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    @Override
    public Path gameDir() {
        return FabricLoader.getInstance().getGameDir();
    }

    @Override
    public void sendToPlayer(ServerPlayer player, SclPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    @Override
    public void sendToAllPlayers(MinecraftServer server, SclPayload payload) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    public static <T extends SclPayload> void registerServerbound(
            Class<T> type, BiConsumer<T, ServerPlayer> handler) {
        CustomPacketPayload.Type<T> payloadType = PayloadRegistry.typeOf(type);
        StreamCodec<RegistryFriendlyByteBuf, T> codec = PayloadRegistry.codecOf(type);

        PayloadTypeRegistry.serverboundPlay().register(payloadType, codec);

        ServerPlayNetworking.registerGlobalReceiver(payloadType, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> handler.accept(payload, player));
        });
    }

    public static <T extends SclPayload> void registerClientboundType(Class<T> type) {
        CustomPacketPayload.Type<T> payloadType = PayloadRegistry.typeOf(type);
        StreamCodec<RegistryFriendlyByteBuf, T> codec = PayloadRegistry.codecOf(type);

        PayloadTypeRegistry.clientboundPlay().register(payloadType, codec);
    }
}
