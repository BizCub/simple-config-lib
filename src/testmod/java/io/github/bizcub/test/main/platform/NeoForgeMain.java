//? neoforge {
/*package io.github.bizcub.test.main.platform;

import io.github.bizcub.test.main.TestModMain;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@Mod(TestModMain.MOD_ID)
@EventBusSubscriber(modid = TestModMain.MOD_ID)
public class NeoForgeMain {

    public NeoForgeMain() {
        TestModMain.init();
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        TestModMain.onServerTick(event.getServer());
    }
}*///?}
