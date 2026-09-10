//? fabric {
package io.github.bizcub.simpleConfigLib.util.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.BiConsumer;

//? >=1.20.5 {
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
//?} else
//import net.minecraft.network.FriendlyByteBuf;

public final class PayloadRegistryFabric {

    public static <T extends SclPayload> void registerServerbound(Class<T> type, BiConsumer<T, ServerPlayer> handler) {
        Identifier id = PayloadRegistry.idOf(type);

        //? >=1.20.5 {
        CustomPacketPayload.Type<T> payloadType = PayloadRegistry.typeOf(type);
        StreamCodec<RegistryFriendlyByteBuf, T> codec = PayloadRegistry.codecOf(type);

        //~ if >=26.1 'playC2S' -> 'serverboundPlay' {
        PayloadTypeRegistry.serverboundPlay().register(payloadType, codec);
        //~}

        ServerPlayNetworking.registerGlobalReceiver(payloadType, (payload, context) -> {
            ServerPlayer player = context.player();
            //~ if !1.20.5 'player().getServer()' -> 'server()'
            context.server().execute(() -> handler.accept(payload, player));
        });

        //?} else {
        /*ServerPlayNetworking.registerGlobalReceiver(id, (server, player, listener, buffer, sender) -> {
            T payload = PayloadRegistry.read(type, buffer);
            server.execute(() -> handler.accept(payload, player));
        });*///?}
    }

    public static <T extends SclPayload> void registerClientbound(Class<T> type) {
        //? >=1.20.5 {
        CustomPacketPayload.Type<T> payloadType = PayloadRegistry.typeOf(type);
        StreamCodec<RegistryFriendlyByteBuf, T> codec = PayloadRegistry.codecOf(type);

        /*~ if >=26.1 'playS2C' -> 'clientboundPlay' {*/
        PayloadTypeRegistry.clientboundPlay().register(payloadType, codec);
        /*~}*/
        //?}
    }
}//?}
