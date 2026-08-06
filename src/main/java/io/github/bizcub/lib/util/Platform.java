package io.github.bizcub.lib.util;

/*? fabric*/ import net.fabricmc.loader.api.FabricLoader;
/*? neoforge*/ //import net.neoforged.fml.ModList;

public class Platform {

    public static boolean isModLoaded(String modId) {
        /*? fabric*/ return FabricLoader.getInstance().isModLoaded(modId);
        /*? neoforge*/ //return ModList.get().isLoaded(modId);
    }
}
