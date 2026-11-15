package io.github.bizcub.simpleConfigLib.platform.client;

import io.github.bizcub.simpleConfigLib.autoconfig.network.ConfigApplyPayload;
import io.github.bizcub.simpleConfigLib.internal.ClientHooks;
import io.github.bizcub.simpleConfigLib.platform.ForgeHooks;
import net.minecraft.client.Minecraft;

public class ForgeClientHooks implements ClientHooks {

    @Override
    public void sendToServer(ConfigApplyPayload payload) {
        ForgeHooks.CHANNEL.send(payload, Minecraft.getInstance().getConnection().getConnection());
    }
}
