//? fabric {  
package io.github.bizcub.simpleConfigLib.util.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

//? >=1.20.5 {
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
//?} else {
/*import net.minecraft.resources.Identifier;*///?}

import java.util.function.BiConsumer;

public final class PayloadRegistryFabricClient {

    public static <T extends SclPayload> void registerClientboundReceiver(Class<T> type, BiConsumer<T, Minecraft> handler) {

        //? >=1.20.5 {
        CustomPacketPayload.Type<T> payloadType = PayloadRegistry.typeOf(type);
        ClientPlayNetworking.registerGlobalReceiver(payloadType, (payload, context) ->
                context.client().execute(() -> handler.accept(payload, context.client())));

        //?} else {
        /*Identifier id = PayloadRegistry.idOf(type);
        ClientPlayNetworking.registerGlobalReceiver(id, (client, listener, buffer, sender) -> {
            T payload = PayloadRegistry.read(type, buffer);
            client.execute(() -> handler.accept(payload, client));
        });*///?}
    }
}//?}
