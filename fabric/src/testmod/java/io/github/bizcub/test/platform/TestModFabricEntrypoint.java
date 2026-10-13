package io.github.bizcub.test.platform;

import io.github.bizcub.test.main.TestModMain;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class TestModFabricEntrypoint implements ModInitializer {

    @Override
    public void onInitialize() {
        TestModMain.init();

        ServerTickEvents.END_SERVER_TICK.register(TestModMain::onServerTick);
    }
}
