package io.github.bizcub.simpleConfigLib.platform.client;

import io.github.bizcub.simpleConfigLib.SimpleConfigLib;
import io.github.bizcub.simpleConfigLib.autoconfig.ConfigHolder;
import io.github.bizcub.simpleConfigLib.autoconfig.network.ConfigSyncPayload;
import io.github.bizcub.simpleConfigLib.internal.ClientHooks;
import io.github.bizcub.simpleConfigLib.util.widget.ScaledItemPIPRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterPictureInPictureRendererEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = SimpleConfigLib.MOD_ID, value = Dist.CLIENT)
public class ForgeClientEntrypoint {

    public static void init() {
        ClientHooks.get().registerClientboundReceiver(
                ConfigSyncPayload.class,
                (payload, client) ->
                        ConfigHolder.applyServerSnapshot(payload.name(), payload.json()));
    }

    @EventBusSubscriber(modid = SimpleConfigLib.MOD_ID, value = Dist.CLIENT,
            bus = EventBusSubscriber.Bus.FORGE)
    public static class ForgeBus {

        @SubscribeEvent
        public static void registerKeymappings(RegisterKeyMappingsEvent event) {
            ForgeClientHooks.consumeKeyMappings(event);
        }
    }

    @SubscribeEvent
    public static void registerPIPRenderers(RegisterPictureInPictureRendererEvent event) {
        event.register(new ScaledItemPIPRenderer());
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        SimpleConfigLib.onClientDisconnect();
    }
}
