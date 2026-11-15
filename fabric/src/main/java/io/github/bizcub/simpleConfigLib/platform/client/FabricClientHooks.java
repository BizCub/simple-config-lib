package io.github.bizcub.simpleConfigLib.platform.client;

import io.github.bizcub.simpleConfigLib.internal.ClientHooks;
import io.github.bizcub.simpleConfigLib.autoconfig.network.ConfigApplyPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class FabricClientHooks implements ClientHooks {

    @Override
    public void sendToServer(ConfigApplyPayload payload) {
        ClientPlayNetworking.send(payload);
    }
}
