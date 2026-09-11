//? forge {
/*package io.github.bizcub.test.client.platform;

import io.github.bizcub.test.client.TestModClient;
import io.github.bizcub.test.main.TestModMain;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = TestModMain.MOD_ID, value = Dist.CLIENT)
public class ForgeClient {

    public static void init() {
        TestModClient.init();

        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () ->
                new ConfigScreenHandler.ConfigScreenFactory((minecraft, parent) -> TestModClient.getConfigScreen(parent)));
    }

    @SubscribeEvent //~ if <=1.20.2 'ClientTickEvent.Post' -> 'ClientTickEvent'
    public static void onClientTick(TickEvent.ClientTickEvent.Post event) {
        TestModClient.onClientTick();
    }
}*///?}
