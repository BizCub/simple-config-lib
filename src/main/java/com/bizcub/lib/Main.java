package com.bizcub.lib;

/*? fabric*/ import net.fabricmc.loader.api.FabricLoader;
/*? forge*/ //import net.minecraftforge.fml.loading.FMLPaths;
/*? neoforge*/ //import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public class Main {
    public static final String MOD_ID = /*$ mod_id*/ "bizcub_lib";

    public static Path gameDir() {
        return
                /*? fabric*/ FabricLoader.getInstance().getGameDir();
                /*? forge*/ //FMLPaths.GAMEDIR.get();
                /*? neoforge*/ //FMLPaths.GAMEDIR.get();
    }
}
