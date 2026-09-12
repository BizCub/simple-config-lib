package io.github.bizcub.simpleConfigLib.util;

//? fabric {
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.api.EnvType;
//?} forge {
/*import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
*///?} neoforge {
/*import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
*///?}

public class Platform {

    public static boolean isModLoaded(String modId) {
        /*? fabric*/ return FabricLoader.getInstance().isModLoaded(modId);
        /*? (forge && <26.1) || neoforge*/ //return ModList.get().isLoaded(modId);
        /*? forge && >=26.1*/ //return ModList.isLoaded(modId);
    }

    public static boolean isPhysicalClient() {
        /*? fabric*/ return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
        /*? forge || neoforge*/ //return FMLEnvironment.dist.isClient();
    }
}
