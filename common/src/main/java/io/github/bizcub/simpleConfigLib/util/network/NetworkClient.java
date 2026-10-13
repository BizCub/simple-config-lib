package io.github.bizcub.simpleConfigLib.util.network;

import io.github.bizcub.simpleConfigLib.internal.ClientHooks;

public class NetworkClient {

    public static void sendToServer(SclPayload payload) {
        ClientHooks.get().sendToServer(payload);
    }
}
