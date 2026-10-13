package io.github.bizcub.simpleConfigLib.internal;

import io.github.bizcub.simpleConfigLib.util.network.SclPayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import java.util.function.BiConsumer;

public interface ClientHooks {

    static ClientHooks get() {
        return Holder.INSTANCE;
    }

    void registerKeyMapping(KeyMapping mapping);

    <T extends SclPayload> void registerClientboundReceiver(Class<T> type, BiConsumer<T, Minecraft> handler);

    void sendToServer(SclPayload payload);

    class Holder {
        static final ClientHooks INSTANCE = java.util.ServiceLoader
                .load(ClientHooks.class)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No SCL client implementation found"));
    }
}
