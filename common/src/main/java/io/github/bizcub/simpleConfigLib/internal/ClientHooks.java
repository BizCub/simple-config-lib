package io.github.bizcub.simpleConfigLib.internal;

import io.github.bizcub.simpleConfigLib.autoconfig.network.ConfigApplyPayload;

public interface ClientHooks {

    static ClientHooks get() {
        return Holder.INSTANCE;
    }

    void sendToServer(ConfigApplyPayload payload);

    class Holder {
        static final ClientHooks INSTANCE = PlatformLoader.create(ClientHooks.class, "client.%sClientHooks");
    }
}
