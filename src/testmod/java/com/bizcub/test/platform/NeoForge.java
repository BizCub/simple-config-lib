//? neoforge {
/*package com.bizcub.test.platform;

import com.bizcub.lib.autoconfig.gui.AutoConfigScreen;
import com.bizcub.test.Main;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(Main.MOD_ID)
public class NeoForge {

    public NeoForge() {
        Main.init();

        ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class, () ->
                (container, screen) -> new AutoConfigScreen(screen, Main.CONFIG));
    }
}*///?}
