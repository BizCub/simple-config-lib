package io.github.bizcub.test.main;

import io.github.bizcub.simpleConfigLib.util.component.ComponentBuilder;
import io.github.bizcub.test.main.config.SimpleConfigMain;
import io.github.bizcub.test.main.config.ConfigMain;
import net.minecraft.server.MinecraftServer;

/*? fabric*/ import net.fabricmc.loader.api.FabricLoader;
/*? forge*/ //import net.minecraftforge.fml.ModList;
/*? neoforge*/ //import net.neoforged.fml.ModList;

public class TestModMain {
    public static final String MOD_ID = "test_lib";

    public static void init() {
        if (isModLoaded("simple_config_lib")) {
            ConfigMain.set(SimpleConfigMain.getInstance().get());
        }
    }

    public static boolean isModLoaded(String modId) {
        /*? fabric*/ return FabricLoader.getInstance().isModLoaded(modId);
        /*? (forge && <26.1) || neoforge*/ //return ModList.get().isLoaded(modId);
        /*? forge && >=26.1*/ //return ModList.isLoaded(modId);
    }

    public static void onServerTick(MinecraftServer server) {
        if (ConfigMain.get().testBoolean()) {
            server.getPlayerList().getPlayers().forEach(player ->
                    player.sendSystemMessage(ComponentBuilder.literal("test").build()));
        }
    }
}
