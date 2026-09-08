//? fabric {
package io.github.bizcub.test.main.platform;

import io.github.bizcub.simpleConfigLib.util.component.ComponentBuilder;
import io.github.bizcub.test.main.TestModMain;import io.github.bizcub.test.main.config.SimpleConfigMain;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class FabricMain implements ModInitializer {

    @Override
    public void onInitialize() {
        TestModMain.init();

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            SimpleConfigMain cfg = SimpleConfigMain.getInstance().get();

            if (cfg.testBoolean) {
                server.getPlayerList().getPlayers().forEach(player ->
                        player.sendSystemMessage(ComponentBuilder.literal("test").build()));
            }
        });
    }
}//?}
