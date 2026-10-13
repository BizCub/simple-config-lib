package io.github.bizcub.simpleConfigLib.platform.client;

import io.github.bizcub.simpleConfigLib.internal.ClientHooks;
import io.github.bizcub.simpleConfigLib.platform.ForgeHooks;
import io.github.bizcub.simpleConfigLib.util.network.SclPayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class ForgeClientHooks implements ClientHooks {

    private static final List<KeyMapping> PENDING_MAPPINGS = new ArrayList<>();

    @Override
    public void registerKeyMapping(KeyMapping mapping) {
        PENDING_MAPPINGS.add(mapping);
    }

    @Override
    public <T extends SclPayload> void registerClientboundReceiver(
            Class<T> type, BiConsumer<T, Minecraft> handler) {
        if (ForgeHooks.CHANNEL != null) {
            throw new IllegalStateException("Clientbound receivers must be registered before channel build");
        }
        ForgeHooks.addPendingClientbound(type, handler);
    }

    @Override
    public void sendToServer(SclPayload payload) {
        ForgeHooks.CHANNEL.send(payload,
                Minecraft.getInstance().getConnection().getConnection());
    }

    static void consumeKeyMappings(RegisterKeyMappingsEvent event) {
        for (KeyMapping mapping : PENDING_MAPPINGS) {
            event.register(mapping);
        }
        PENDING_MAPPINGS.clear();
    }
}
