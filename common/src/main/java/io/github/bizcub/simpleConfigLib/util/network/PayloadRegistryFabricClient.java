//? fabric {
package io.github.bizcub.simpleConfigLib.util.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.function.BiConsumer;

public final class PayloadRegistryFabricClient {

    public static <T extends SclPayload> void registerClientboundReceiver(Class<T> type, BiConsumer<T, Minecraft> handler) {

        CustomPacketPayload.Type<T> payloadType = PayloadRegistry.typeOf(type);
        ClientPlayNetworking.registerGlobalReceiver(payloadType, (payload, context) ->
                context.client().execute(() -> handler.accept(payload, context.client())));
    }
}//?}
