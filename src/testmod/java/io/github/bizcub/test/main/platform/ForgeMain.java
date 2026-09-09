//? forge {
/*package io.github.bizcub.test.main.platform;

import io.github.bizcub.test.client.platform.ForgeClient;
import io.github.bizcub.test.main.TestModMain;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(TestModMain.MOD_ID)
@EventBusSubscriber(modid = TestModMain.MOD_ID)
public class ForgeMain {

    public ForgeMain() {
        TestModMain.init();

        if (FMLEnvironment.dist.isClient()) {
            ForgeClient.init();
        }
    }

    @SubscribeEvent //~ if >=1.20.3 'ServerTickEvent' -> 'ServerTickEvent.Post'
    public static void onServerTick(TickEvent.ServerTickEvent.Post event) {
        TestModMain.onServerTick(event.server());
    }
}*///?}
