//? forge {
/*package io.github.bizcub.simpleConfigLib.platform;

import io.github.bizcub.simpleConfigLib.Main;
import io.github.bizcub.simpleConfigLib.util.Keymapper;
import io.github.bizcub.simpleConfigLib.util.widget.ScaledItemPIPRenderer;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterPictureInPictureRendererEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(Main.MOD_ID)
public class Forge {

    public Forge() {
        RegisterKeyMappingsEvent.BUS.addListener(Keymapper::onRegisterKeyMappings);
        //? >=1.21.6
        RegisterPictureInPictureRendererEvent.BUS.addListener(this::registerPIPRenderers);
    }

    public void registerPIPRenderers(RegisterPictureInPictureRendererEvent event) {
        event.register(new ScaledItemPIPRenderer(event.getBufferSource()));
    }
}*///?}
