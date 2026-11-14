package io.github.bizcub.simpleConfigLib.internal;

final class PlatformLoader {

    private static final String LOADER = detectLoader();

    static <T> T create(Class<T> type, String implementation) {
        String name = "io.github.bizcub.simpleConfigLib.platform." + implementation.formatted(LOADER);
        try {
            return type.cast(Class.forName(name, true, PlatformLoader.class.getClassLoader())
                    .getDeclaredConstructor().newInstance());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot initialize SCL adapter " + name, e);
        }
    }

    private static String detectLoader() {
        if (isPresent("net.fabricmc.loader.api.FabricLoader")) return "Fabric";
        if (isPresent("net.minecraftforge.fml.loading.FMLLoader")) return "Forge";
        if (isPresent("net.neoforged.fml.loading.FMLLoader")) return "NeoForge";
        throw new IllegalStateException("No supported Minecraft loader found for SCL");
    }

    private static boolean isPresent(String name) {
        try {
            Class.forName(name, false, PlatformLoader.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
