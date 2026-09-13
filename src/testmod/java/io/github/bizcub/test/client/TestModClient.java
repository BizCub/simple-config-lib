package io.github.bizcub.test.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.bizcub.simpleConfigLib.autoconfig.gui.ConfigScreenFactory;
import io.github.bizcub.simpleConfigLib.util.Keymapper;
import io.github.bizcub.simpleConfigLib.util.component.ComponentBuilder;
import io.github.bizcub.simpleConfigLib.util.network.NetworkClient;
import io.github.bizcub.test.client.config.ConfigClient;
import io.github.bizcub.test.client.config.SimpleConfigClient;
import io.github.bizcub.test.client.screen.TestScreen;
import io.github.bizcub.test.main.TestModMain;
import io.github.bizcub.test.main.config.ConfigMain;
import io.github.bizcub.test.network.PingPayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public class TestModClient {
    public static final KeyMapping TOGGLE_VISIBILITY = Keymapper.register(
            "key." + TestModMain.MOD_ID + ".open_test_screen",
            InputConstants.KEY_H,
            Keymapper.getCategoryId("misc")
    );
    public static final KeyMapping SEND_PING = Keymapper.register(
            "key." + TestModMain.MOD_ID + ".send_ping",
            InputConstants.KEY_J,
            Keymapper.getCategoryId("misc"));

    public static void init() {
        if (TestModMain.isModLoaded("simple_config_lib")) {
            ConfigClient.set(SimpleConfigClient.getInstance().get());
        }
    }

    public static void onClientTick() {
        Minecraft client = Minecraft.getInstance();
        while (TOGGLE_VISIBILITY.consumeClick()) {
            client.gui.setScreen(new TestScreen(ComponentBuilder.literal("test").build()));
        }
        while (SEND_PING.consumeClick()) {
            NetworkClient.sendToServer(new PingPayload("ping", client.player.getMainHandItem()));
        }
        if (ConfigMain.get().testClientInCommon() && client.player != null) {
            client.player.sendOverlayMessage(ComponentBuilder.literal("test client-in-common").build());
        }
    }

    public static Screen getConfigScreen(Screen parent) {
        return ConfigScreenFactory.open(parent);
    }
}
