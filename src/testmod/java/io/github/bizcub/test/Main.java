package io.github.bizcub.test;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.bizcub.simpleConfigLib.autoconfig.gui.ConfigHubScreen;
import io.github.bizcub.simpleConfigLib.util.Keymapper;
import io.github.bizcub.simpleConfigLib.util.component.ComponentBuilder;
import io.github.bizcub.test.config.SimpleConfig;
import io.github.bizcub.test.config.Config;
import io.github.bizcub.test.config.SimpleConfigServer;
import io.github.bizcub.test.screen.TestScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/*? fabric*/ import net.fabricmc.loader.api.FabricLoader;
/*? forge*/ //import net.minecraftforge.fml.ModList;
/*? neoforge*/ //import net.neoforged.fml.ModList;

public class Main {
    public static final String MOD_ID = "test_lib";
    public static final KeyMapping TOGGLE_VISIBILITY = Keymapper.register(
            "key." + MOD_ID + ".open_test_screen",
            InputConstants.KEY_H,
            Keymapper.getCategoryId("misc")
    );

    public static void init() {
        if (isModLoaded("simple_config_lib")) {
            Config.set(SimpleConfig.getInstance().get());
        }
    }

    public static boolean isModLoaded(String modId) {
        /*? fabric*/ return FabricLoader.getInstance().isModLoaded(modId);
        /*? (forge && <26.1) || neoforge*/ //return ModList.get().isLoaded(modId);
        /*? forge && >=26.1*/ //return ModList.isLoaded(modId);
    }

    public static void setTestScreen() {
        while (Main.TOGGLE_VISIBILITY.consumeClick()) {
            Minecraft.getInstance().gui.setScreen(new TestScreen(ComponentBuilder.literal("test").build()));
        }
    }

    public static Screen getConfigScreen(Screen parent) {
        SimpleConfigServer.getInstance();
        SimpleConfig.getInstance();

        return ConfigHubScreen.open(parent);
    }
}
