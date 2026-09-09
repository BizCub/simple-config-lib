//? forge {
/*package io.github.bizcub.test.main.platform;  
  
import io.github.bizcub.simpleConfigLib.util.component.ComponentBuilder;  
import io.github.bizcub.test.main.TestModMain;  
import io.github.bizcub.test.main.config.SimpleConfigMain;  
import net.minecraftforge.event.TickEvent;  
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;  
import net.minecraftforge.fml.common.Mod;  
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;  
  
@Mod(TestModMain.MOD_ID)  
@EventBusSubscriber(modid = TestModMain.MOD_ID)  
public class ForgeMain {  
  
    public ForgeMain() {  
        TestModMain.init();  
    }  
  
    @SubscribeEvent //~ if <=1.20.2 'ServerTickEvent.Post' -> 'ServerTickEvent'
    public static void onServerTick(TickEvent.ServerTickEvent.Post event) {  
        SimpleConfigMain cfg = SimpleConfigMain.getInstance().get();  
  
        if (cfg.testBoolean) {  
            event.server().getPlayerList().getPlayers().forEach(player ->  
                    player.sendSystemMessage(ComponentBuilder.literal("test").build()));  
        }  
    }  
}*///?}
