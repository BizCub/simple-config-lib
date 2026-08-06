//? neoforge {
/*package io.github.bizcub.lib.platform;

import io.github.bizcub.lib.Main;
import io.github.bizcub.lib.util.Keymapper;
import io.github.bizcub.lib.util.widget.ScaledItemPIPRenderer;
import io.github.bizcub.lib.util.widget.ScaledItemRenderState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent;

@Mod(Main.MOD_ID)
public class NeoForge {

    public NeoForge(IEventBus modEventBus) {
        modEventBus.addListener(Keymapper::onRegisterKeyMappings);
        //? >=1.21.6
        modEventBus.addListener(NeoForge::registerPIPRenderers);
    }


    //? >=1.21.6 {
    public static void registerPIPRenderers(RegisterPictureInPictureRenderersEvent event) {
        event.register(ScaledItemRenderState.class, ScaledItemPIPRenderer::new);
    }//?}
}*///?}
