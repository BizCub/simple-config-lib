package io.github.bizcub.simpleConfigLib.platform.client;

import io.github.bizcub.simpleConfigLib.autoconfig.network.ConfigApplyPayload;
import io.github.bizcub.simpleConfigLib.internal.ClientHooks;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class NeoForgeClientHooks implements ClientHooks {

    @Override
    public void sendToServer(ConfigApplyPayload payload) {
        ClientPacketDistributor.sendToServer(payload);
    }
}
