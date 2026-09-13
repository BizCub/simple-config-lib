//? neoforge {
/*package io.github.bizcub.simpleConfigLib.client.platform;

import io.github.bizcub.simpleConfigLib.main.SimpleConfigLibMain;
import io.github.bizcub.simpleConfigLib.util.Keymapper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

//? >=1.21.6 {
import io.github.bizcub.simpleConfigLib.util.widget.ScaledItemPIPRenderer;
import io.github.bizcub.simpleConfigLib.util.widget.ScaledItemRenderState;
import net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent;
//?}

@Mod(SimpleConfigLibMain.MOD_ID)
@EventBusSubscriber(modid = SimpleConfigLibMain.MOD_ID, value = Dist.CLIENT)
public class NeoForgeClient {

    @SubscribeEvent
    public static void registerKeymappings(RegisterKeyMappingsEvent event) {
        Keymapper.onRegisterKeyMappings(event);
    }

    //? >=1.21.6 {
    @SubscribeEvent
    public static void registerPIPRenderers(RegisterPictureInPictureRenderersEvent event) {
        event.register(ScaledItemRenderState.class, ScaledItemPIPRenderer::new);
    }//?}

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        SimpleConfigLibMain.onClientDisconnect();
    }
}*///?}
