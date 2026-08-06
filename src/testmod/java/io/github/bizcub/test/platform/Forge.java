//? forge {
/*package com.bizcub.test.platform;

import com.bizcub.lib.autoconfig.gui.AutoConfigScreen;
import com.bizcub.test.Main;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;

@Mod(Main.MOD_ID)
public class Forge {

    public Forge() {
        Main.init();

        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () ->
                new ConfigScreenHandler.ConfigScreenFactory((minecraft, screen) ->
                        new AutoConfigScreen(screen, Main.CONFIG)));
    }
}*///?}
