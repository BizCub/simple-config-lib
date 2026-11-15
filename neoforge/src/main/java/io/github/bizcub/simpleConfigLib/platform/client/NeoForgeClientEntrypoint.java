package io.github.bizcub.simpleConfigLib.platform.client;

import io.github.bizcub.simpleConfigLib.SimpleConfigLib;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

@EventBusSubscriber(modid = SimpleConfigLib.MOD_ID, value = Dist.CLIENT)
public class NeoForgeClientEntrypoint {

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        SimpleConfigLib.onClientDisconnect();
    }
}
