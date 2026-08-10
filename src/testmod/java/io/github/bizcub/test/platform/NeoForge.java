//? neoforge {
/*package io.github.bizcub.test.platform;

import io.github.bizcub.test.Main;
import io.github.bizcub.test.config.SimpleConfig;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(Main.MOD_ID)
@EventBusSubscriber(modid = Main.MOD_ID)
public class NeoForge {

    public NeoForge() {
        Main.init();

        ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class, () ->
                (container, screen) -> SimpleConfig.getInstance().createScreen(screen));
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Main.setTestScreen();
    }
}*///?}
