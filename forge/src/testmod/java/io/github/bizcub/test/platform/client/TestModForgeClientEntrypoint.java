package io.github.bizcub.test.platform.client;

import io.github.bizcub.test.client.TestModClient;
import io.github.bizcub.test.main.TestModMain;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@EventBusSubscriber(modid = TestModMain.MOD_ID, value = Dist.CLIENT)
public class TestModForgeClientEntrypoint {

    public static void init(FMLJavaModLoadingContext context) {
        TestModClient.init();

        context.registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (minecraft, parent) -> TestModClient.getConfigScreen(parent)));
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent.Post event) {
        TestModClient.onClientTick();
    }

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        TestModClient.renderPreview(event.getScreen(), event.getGuiGraphics());
    }
}
