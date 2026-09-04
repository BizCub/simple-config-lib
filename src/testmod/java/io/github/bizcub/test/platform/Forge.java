//? forge {
/*package io.github.bizcub.test.platform;

import io.github.bizcub.test.Main;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@Mod(Main.MOD_ID)
@EventBusSubscriber(modid = Main.MOD_ID)
public class Forge {

    public Forge() {
        Main.init();

        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () ->
                new ConfigScreenHandler.ConfigScreenFactory((minecraft, screen) ->
                        Main.getConfigScreen(screen)));
    }

    @SubscribeEvent //~ if <=1.20.2 'ClientTickEvent.Post' -> 'ClientTickEvent'
    public static void onClientTick(TickEvent.ClientTickEvent.Post event) {
        Main.setTestScreen();
    }
}*///?}
