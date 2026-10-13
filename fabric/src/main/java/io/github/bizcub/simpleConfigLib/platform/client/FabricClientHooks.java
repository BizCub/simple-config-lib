package io.github.bizcub.simpleConfigLib.platform.client;

import io.github.bizcub.simpleConfigLib.internal.ClientHooks;
import io.github.bizcub.simpleConfigLib.util.network.PayloadRegistry;
import io.github.bizcub.simpleConfigLib.util.network.SclPayload;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.function.BiConsumer;

public class FabricClientHooks implements ClientHooks {

    @Override
    public void registerKeyMapping(KeyMapping mapping) {
        KeyMappingHelper.registerKeyMapping(mapping);
    }

    @Override
    public <T extends SclPayload> void registerClientboundReceiver(
            Class<T> type, BiConsumer<T, Minecraft> handler) {
        CustomPacketPayload.Type<T> payloadType = PayloadRegistry.typeOf(type);

        ClientPlayNetworking.registerGlobalReceiver(payloadType, (payload, context) ->
                context.client().execute(() -> handler.accept(payload, context.client())));
    }

    @Override
    public void sendToServer(SclPayload payload) {
        ClientPlayNetworking.send(payload);
    }
}
