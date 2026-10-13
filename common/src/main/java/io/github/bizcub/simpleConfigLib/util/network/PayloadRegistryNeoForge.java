//? neoforge {
/*package io.github.bizcub.simpleConfigLib.util.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class PayloadRegistryNeoForge {

    public static <T extends SclPayload> void registerServerbound(PayloadRegistrar registrar, Class<T> type, BiConsumer<T, ServerPlayer> handler) {

        CustomPacketPayload.Type<T> payloadType = PayloadRegistry.typeOf(type);
        StreamCodec<RegistryFriendlyByteBuf, T> codec = PayloadRegistry.codecOf(type);

        registrar.playToServer(payloadType, codec, (payload, context) ->
                context.enqueueWork(() -> {
                    if (!(context.player() instanceof ServerPlayer player)) return;
                    handler.accept(payload, player);
                }));
    }

    public static <T extends SclPayload> void registerClientbound(
            PayloadRegistrar registrar, Class<T> type, Consumer<T> handler) {

        CustomPacketPayload.Type<T> payloadType = PayloadRegistry.typeOf(type);
        StreamCodec<RegistryFriendlyByteBuf, T> codec = PayloadRegistry.codecOf(type);

        registrar.playToClient(payloadType, codec, (payload, context) ->
                context.enqueueWork(() -> handler.accept(payload)));
    }
}*///?}
