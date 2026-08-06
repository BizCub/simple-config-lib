package io.github.bizcub.test.config;

import io.github.bizcub.lib.util.Platform;

import java.util.ArrayList;
import java.util.List;

public interface Config {
    Config CONFIG = Platform.isModLoaded("bizcub_lib") ? BCConfig.getInstance().get() : new Config() { };

    static Config get() {
        return CONFIG;
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

    default BCConfig.PerformancePreset preset() {
        return BCConfig.PerformancePreset.BALANCED;
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

    default BCConfig.RenderQuality renderQuality() {
        return BCConfig.RenderQuality.MEDIUM;
    }

    default List<BCConfig.ChunkProfile> chunkProfiles() {
        return new ArrayList<>(List.of(new BCConfig.ChunkProfile(), new BCConfig.ChunkProfile()));
    }

    default BCConfig.Graphics graphics() {
        return new BCConfig.Graphics();
    }
}
