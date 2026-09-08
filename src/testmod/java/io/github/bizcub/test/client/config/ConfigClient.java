package io.github.bizcub.test.client.config;

import java.util.ArrayList;
import java.util.List;

public interface ConfigClient {
    static ConfigClient get() {
        return Holder.INSTANCE;
    }

    static void set(final ConfigClient config) {
        if (config != null) {
            Holder.INSTANCE = config;
        }
    }

    class Holder {
        private static ConfigClient INSTANCE = new ConfigClient() { };
    }

    default boolean enableOptimizations() {
        return true;
    }

    default int fpsLimit() {
        return 120;
    }

    default int renderDistance() {
        return 12;
    }

    default PerformancePreset preset() {
        return PerformancePreset.BALANCED;
    }

    default int fpsOverlayColor() {
        return 0x00FF00;
    }

    default int profilerBackground() {
        return 0x80202020;
    }

    default List<String> ignoredMods() {
        return new ArrayList<>(List.of("examplemod"));
    }

    default List<Boolean> perDimensionCulling() {
        return new ArrayList<>(List.of(true, false, true));
    }

    default String worldProfile() {
        return "default";
    }

    default int entityLodBias() {
        return 50;
    }

    default RenderQuality renderQuality() {
        return RenderQuality.MEDIUM;
    }

    default List<ChunkProfile> chunkProfiles() {
        return new ArrayList<>(List.of(new ChunkProfile(), new ChunkProfile()));
    }

    default Graphics graphics() {
        return new Graphics();
    }
}
