package io.github.bizcub.simpleConfigLib.internal.network;

import io.github.bizcub.simpleConfigLib.autoconfig.network.ConfigApplyPayload;
import io.github.bizcub.simpleConfigLib.internal.ClientHooks;

public final class NetworkClient {
    public static void sendToServer(ConfigApplyPayload payload) {
        ClientHooks.get().sendToServer(payload);
    }
}
