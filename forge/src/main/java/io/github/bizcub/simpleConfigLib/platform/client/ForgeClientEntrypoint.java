package io.github.bizcub.simpleConfigLib.platform.client;

import io.github.bizcub.simpleConfigLib.SimpleConfigLib;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = SimpleConfigLib.MOD_ID, value = Dist.CLIENT)
public class ForgeClientEntrypoint {

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        SimpleConfigLib.onClientDisconnect();
    }
}
