package io.github.bizcub.test;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.bizcub.lib.util.Keymapper;
import io.github.bizcub.lib.util.component.ComponentBuilder;
import io.github.bizcub.test.config.Config;
import io.github.bizcub.test.screen.TestScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public class Main {
    public static final String MOD_ID = "test_lib";
    public static final KeyMapping TOGGLE_VISIBILITY = Keymapper.register(
            "key." + MOD_ID + ".toggle_frames_visibility",
            InputConstants.KEY_H,
            Keymapper.getCategoryId("misc")
    );

    public static void init() {
        Config.get();
    }

    public static void setTestScreen() {
        while (Main.TOGGLE_VISIBILITY.consumeClick()) {
            Minecraft.getInstance().gui.setScreen(new TestScreen(ComponentBuilder.literal("qq").build()));
        }
    }
}
