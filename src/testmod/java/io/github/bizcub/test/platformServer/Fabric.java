//? fabric {
package io.github.bizcub.test.platformServer;

import io.github.bizcub.simpleConfigLib.util.component.ComponentBuilder;
import io.github.bizcub.test.config.SimpleConfigServer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class Fabric implements ModInitializer {

    @Override
    public void onInitialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            SimpleConfigServer cfg = SimpleConfigServer.getInstance().get();

            if (cfg.testBoolean) {
                server.getPlayerList().getPlayers().forEach(player ->
                        player.sendSystemMessage(ComponentBuilder.literal("test").build()));
            }
        });
    }
}//?}
