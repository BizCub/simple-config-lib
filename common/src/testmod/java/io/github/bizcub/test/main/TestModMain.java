package io.github.bizcub.test.main;

import io.github.bizcub.test.main.config.SimpleConfigMain;
import io.github.bizcub.test.main.config.SimpleConfigServer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

public class TestModMain {
    public static final String MOD_ID = "test_lib";

    public static void init() {
        SimpleConfigMain.getInstance();
        SimpleConfigServer.getInstance();
    }

    public static void onServerTick(MinecraftServer server) {
        if (SimpleConfigMain.getInstance().get().testMain) {
            server.getPlayerList().getPlayers().forEach(player ->
                    player.sendSystemMessage(Component.literal("test common")));
        }
        if (SimpleConfigServer.getInstance().get().testServer) {
            server.getPlayerList().getPlayers().forEach(player ->
                    player.sendSystemMessage(Component.literal("test server")));
        }
    }
}
