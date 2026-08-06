//? neoforge {
/*package io.github.bizcub.test.platform;

import io.github.bizcub.lib.autoconfig.gui.AutoConfigScreen;
import io.github.bizcub.test.Main;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(Main.MOD_ID)
public class NeoForge {

    public NeoForge() {
        Main.init();

        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
                (ClientTickEvent.Post event) -> Main.setTestScreen());

        ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class, () ->
                (container, screen) -> new AutoConfigScreen(screen, Main.CONFIG));
    }
}*///?}
