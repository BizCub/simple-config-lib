//? forge {
/*package io.github.bizcub.simpleConfigLib.platform;

import io.github.bizcub.simpleConfigLib.Main;
import io.github.bizcub.simpleConfigLib.util.Keymapper;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

//? >=1.21.6 {
import io.github.bizcub.simpleConfigLib.util.widget.ScaledItemPIPRenderer;
import net.minecraftforge.client.event.RegisterPictureInPictureRendererEvent;
//?}

@Mod(Main.MOD_ID)
@EventBusSubscriber(modid = Main.MOD_ID)
public class Forge {

    @SubscribeEvent
    public static void registerKeymappings(RegisterKeyMappingsEvent event) {
        Keymapper.onRegisterKeyMappings(event);
    }

    //? >=1.21.6 {
    @SubscribeEvent
    public static void registerPIPRenderers(RegisterPictureInPictureRendererEvent event) {
        event.register(new ScaledItemPIPRenderer(/^? <26.2 >>+ ')'^/ /^event.getBufferSource()^/));
    }//?}
}*///?}
