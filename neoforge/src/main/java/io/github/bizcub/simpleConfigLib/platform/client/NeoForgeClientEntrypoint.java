package io.github.bizcub.simpleConfigLib.platform.client;

import io.github.bizcub.simpleConfigLib.SimpleConfigLib;
import io.github.bizcub.simpleConfigLib.autoconfig.ConfigHolder;
import io.github.bizcub.simpleConfigLib.autoconfig.network.ConfigSyncPayload;
import io.github.bizcub.simpleConfigLib.internal.ClientHooks;
import io.github.bizcub.simpleConfigLib.util.widget.ScaledItemPIPRenderer;
import io.github.bizcub.simpleConfigLib.util.widget.ScaledItemRenderState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent;

@EventBusSubscriber(modid = SimpleConfigLib.MOD_ID, value = Dist.CLIENT)
public class NeoForgeClientEntrypoint {

    public static void init() {
        ClientHooks.get().registerClientboundReceiver(
                ConfigSyncPayload.class,
                (payload, client) ->
                        ConfigHolder.applyServerSnapshot(payload.name(), payload.json()));
    }

    @EventBusSubscriber(modid = SimpleConfigLib.MOD_ID, value = Dist.CLIENT)
    public static class ModBus {

        @SubscribeEvent
        public static void registerKeymappings(RegisterKeyMappingsEvent event) {
            NeoForgeClientHooks.consumeKeyMappings(event);
        }

        @SubscribeEvent
        public static void registerPIPRenderers(RegisterPictureInPictureRenderersEvent event) {
            event.register(ScaledItemRenderState.class, ScaledItemPIPRenderer::new);
        }
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        SimpleConfigLib.onClientDisconnect();
    }
}
