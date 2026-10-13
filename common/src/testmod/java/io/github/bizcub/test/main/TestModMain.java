package io.github.bizcub.test.main;

import io.github.bizcub.simpleConfigLib.util.Platform;
import io.github.bizcub.simpleConfigLib.util.component.ComponentBuilder;
import io.github.bizcub.simpleConfigLib.util.network.Network;
import io.github.bizcub.test.main.config.ConfigMain;
import io.github.bizcub.test.main.config.ConfigServer;
import io.github.bizcub.test.main.config.SimpleConfigMain;
import io.github.bizcub.test.main.config.SimpleConfigServer;
import io.github.bizcub.test.network.PingPayload;
import net.minecraft.server.MinecraftServer;

public class TestModMain {
    public static final String MOD_ID = "test_lib";

    public static void init() {
        if (isModLoaded("simple_config_lib")) {
            ConfigMain.set(SimpleConfigMain.getInstance().get());
            ConfigServer.set(SimpleConfigServer.getInstance().get());

            Network.registerServerbound(PingPayload.class, (payload, player) ->
                    player.sendSystemMessage(ComponentBuilder.literal(
                            "ping: " + payload.message() + " item=" + payload.stack().getCount()).build()));
        }
    }

    public static boolean isModLoaded(String modId) {
        return Platform.isModLoaded(modId);
    }

    public static void onServerTick(MinecraftServer server) {
        if (ConfigMain.get().testMain()) {
            server.getPlayerList().getPlayers().forEach(player ->
                    player.sendSystemMessage(ComponentBuilder.literal("test common").build()));
        }
        if (ConfigServer.get().testServer()) {
            server.getPlayerList().getPlayers().forEach(player ->
                    player.sendSystemMessage(ComponentBuilder.literal("test server").build()));
        }
    }
}
