//? fabric {
package io.github.bizcub.simpleConfigLib.util.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.BiConsumer;

public final class PayloadRegistryFabric {

    public static <T extends SclPayload> void registerServerbound(Class<T> type, BiConsumer<T, ServerPlayer> handler) {
        Identifier id = PayloadRegistry.idOf(type);

        CustomPacketPayload.Type<T> payloadType = PayloadRegistry.typeOf(type);
        StreamCodec<RegistryFriendlyByteBuf, T> codec = PayloadRegistry.codecOf(type);

        PayloadTypeRegistry.serverboundPlay().register(payloadType, codec);

        ServerPlayNetworking.registerGlobalReceiver(payloadType, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> handler.accept(payload, player));
        });
    }

    public static <T extends SclPayload> void registerClientbound(Class<T> type) {
        CustomPacketPayload.Type<T> payloadType = PayloadRegistry.typeOf(type);
        StreamCodec<RegistryFriendlyByteBuf, T> codec = PayloadRegistry.codecOf(type);

        PayloadTypeRegistry.clientboundPlay().register(payloadType, codec);
    }
}//?}
