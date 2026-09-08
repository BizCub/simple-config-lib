package io.github.bizcub.test.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.bizcub.simpleConfigLib.autoconfig.ConfigProvider;
import io.github.bizcub.simpleConfigLib.autoconfig.gui.ConfigHubScreen;
import io.github.bizcub.simpleConfigLib.util.Keymapper;
import io.github.bizcub.simpleConfigLib.util.component.ComponentBuilder;
import io.github.bizcub.test.client.config.ConfigClient;
import io.github.bizcub.test.client.config.SimpleConfigClient;
import io.github.bizcub.test.client.screen.TestScreen;
import io.github.bizcub.test.main.TestModMain;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public class TestModClient {
    public static final KeyMapping TOGGLE_VISIBILITY = Keymapper.register(
            "key." + TestModMain.MOD_ID + ".open_test_screen",
            InputConstants.KEY_H,
            Keymapper.getCategoryId("misc")
    );

    public static void init() {
        if (TestModMain.isModLoaded("simple_config_lib")) {
            ConfigProvider.set(ConfigClient.class, SimpleConfigClient.getInstance().get());
        }
    }

    public static void setTestScreen() {
        while (TestModClient.TOGGLE_VISIBILITY.consumeClick()) {
            Minecraft.getInstance().gui.setScreen(new TestScreen(ComponentBuilder.literal("test").build()));
        }
    }

    public static Screen getConfigScreen(Screen parent) {
        return ConfigHubScreen.open(parent);
    }
}
