package io.github.bizcub.simpleConfigLib.util;

//? fabric {
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.api.EnvType;
//?} forge {
/*import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.FMLEnvironment;
*///?} neoforge {
/*import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
*///?}

import java.nio.file.Path;

public class Platform {

    public static boolean isModLoaded(String modId) {
        /*? fabric*/ return FabricLoader.getInstance().isModLoaded(modId);
        /*? forge*/ //return ModList.isLoaded(modId);
        /*? neoforge*/ //return ModList.get().isLoaded(modId);
    }

    public static boolean isPhysicalClient() {
        /*? fabric*/ return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
        /*? forge*/ //return FMLEnvironment.dist.isClient();
        /*? neoforge*/ //return FMLEnvironment.getDist().isClient();
    }

    public static Path gameDir() {
        return
                /*? fabric*/ FabricLoader.getInstance().getGameDir();
                /*? forge || neoforge*/ //FMLPaths.GAMEDIR.get();
    }
}
