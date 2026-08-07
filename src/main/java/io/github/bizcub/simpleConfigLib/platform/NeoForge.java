//? neoforge {
/*package io.github.bizcub.simpleConfigLib.platform;

import io.github.bizcub.simpleConfigLib.Main;
import io.github.bizcub.simpleConfigLib.util.Keymapper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

//? >=1.21.6 {
/^import io.github.bizcub.simpleConfigLib.util.widget.ScaledItemPIPRenderer;
import io.github.bizcub.simpleConfigLib.util.widget.ScaledItemRenderState;
import net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent;
^///?}

@Mod(Main.MOD_ID)
@EventBusSubscriber(modid = Main.MOD_ID)
public class NeoForge {

    @SubscribeEvent
    public static void registerKeymappings(RegisterKeyMappingsEvent event) {
        Keymapper.onRegisterKeyMappings(event);
    }

    //? >=1.21.6 {
    /^@SubscribeEvent
    public static void registerPIPRenderers(RegisterPictureInPictureRenderersEvent event) {
        event.register(ScaledItemRenderState.class, ScaledItemPIPRenderer::new);
    }^///?}
}*///?}
