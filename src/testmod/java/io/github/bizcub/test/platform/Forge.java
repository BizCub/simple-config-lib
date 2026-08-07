//? forge {
/*package io.github.bizcub.test.platform;

import io.github.bizcub.test.Main;
import io.github.bizcub.test.config.LibConfig;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;

@Mod(Main.MOD_ID)
public class Forge {

    public Forge() {
        Main.init();

        TickEvent.ClientTickEvent.Post.BUS.addListener(
                (TickEvent.ClientTickEvent.Post event) -> Main.setTestScreen());

        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () ->
                new ConfigScreenHandler.ConfigScreenFactory((minecraft, screen) ->
                        LibConfig.getInstance().createScreen(screen)));
    }
}*///?}
