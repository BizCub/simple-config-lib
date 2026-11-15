package io.github.bizcub.test.platform;

import io.github.bizcub.test.main.TestModMain;
import io.github.bizcub.test.platform.client.TestModForgeClientEntrypoint;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(TestModMain.MOD_ID)
@EventBusSubscriber(modid = TestModMain.MOD_ID)
public class TestModForgeEntrypoint {

    public TestModForgeEntrypoint(FMLJavaModLoadingContext context) {
        TestModMain.init();

        if (FMLEnvironment.dist.isClient()) {
            TestModForgeClientEntrypoint.init(context);
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent.Post event) {
        TestModMain.onServerTick(event.server());
    }
}
