//? neoforge {
/*package io.github.bizcub.test.client.platform;

import io.github.bizcub.test.client.TestModClient;
import io.github.bizcub.test.main.TestModMain;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = TestModMain.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = TestModMain.MOD_ID)
public class NeoForgeClient {

    public NeoForgeClient() {
        TestModClient.init();

        ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class, () ->
                (container, screen) -> TestModClient.getConfigScreen(screen));
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        TestModClient.setTestScreen();
    }
}*///?}
