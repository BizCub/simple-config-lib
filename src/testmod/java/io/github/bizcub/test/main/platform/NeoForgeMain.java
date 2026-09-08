//? neoforge {
/*package io.github.bizcub.test.main.platform;

import io.github.bizcub.simpleConfigLib.util.component.ComponentBuilder;
import io.github.bizcub.test.main.TestModMain;
import io.github.bizcub.test.main.config.SimpleConfigMain;
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
        SimpleConfigMain cfg = SimpleConfigMain.getInstance().get();

        if (cfg.testBoolean) {
            event.getServer().getPlayerList().getPlayers().forEach(player ->
                    player.sendSystemMessage(ComponentBuilder.literal("test").build()));
        }
    }
}*///?}
