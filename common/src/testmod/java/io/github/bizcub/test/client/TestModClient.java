package io.github.bizcub.test.client;

import io.github.bizcub.simpleConfigLib.autoconfig.gui.ConfigScreens;
import io.github.bizcub.test.client.config.SimpleConfigClient;
import io.github.bizcub.test.main.TestModMain;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TestModClient {
    public static void init() {
        SimpleConfigClient.getInstance();
    }

    public static void onClientTick() {
        var player = Minecraft.getInstance().player;
        if (player == null) return;
        if (SimpleConfigClient.getInstance().get().testClient) {
            player.sendOverlayMessage(Component.literal("test client"));
        }
    }

    public static Screen getConfigScreen(Screen parent) {
        return ConfigScreens.open(TestModMain.MOD_ID, parent);
    }
}
