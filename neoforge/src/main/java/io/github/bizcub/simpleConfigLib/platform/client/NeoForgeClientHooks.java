package io.github.bizcub.simpleConfigLib.platform.client;

import io.github.bizcub.simpleConfigLib.internal.ClientHooks;
import io.github.bizcub.simpleConfigLib.platform.NeoForgeHooks;
import io.github.bizcub.simpleConfigLib.util.network.SclPayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class NeoForgeClientHooks implements ClientHooks {

    private static final List<KeyMapping> PENDING_MAPPINGS = new ArrayList<>();

    @Override
    public void registerKeyMapping(KeyMapping mapping) {
        PENDING_MAPPINGS.add(mapping);
    }

    @Override
    public <T extends SclPayload> void registerClientboundReceiver(
            Class<T> type, BiConsumer<T, Minecraft> handler) {
        NeoForgeHooks.addPendingClientbound(type, handler);
    }

    @Override
    public void sendToServer(SclPayload payload) {
        ClientPacketDistributor.sendToServer(payload);
    }

    static void consumeKeyMappings(RegisterKeyMappingsEvent event) {
        for (KeyMapping mapping : PENDING_MAPPINGS) {
            event.register(mapping);
        }
        PENDING_MAPPINGS.clear();
    }
}
