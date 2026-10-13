package io.github.bizcub.simpleConfigLib.util;

import io.github.bizcub.simpleConfigLib.internal.PlatformHooks;

import java.nio.file.Path;

public class Platform {

    public static boolean isModLoaded(String modId) {
        return PlatformHooks.get().isModLoaded(modId);
    }

    public static boolean isPhysicalClient() {
        return PlatformHooks.get().isPhysicalClient();
    }

    public static Path gameDir() {
        return PlatformHooks.get().gameDir();
    }
}
