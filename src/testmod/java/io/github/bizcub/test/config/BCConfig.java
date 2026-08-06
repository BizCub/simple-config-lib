package io.github.bizcub.test.config;

import io.github.bizcub.lib.autoconfig.ConfigHolder;
import io.github.bizcub.lib.autoconfig.annotation.*;
import io.github.bizcub.lib.autoconfig.gui.AutoConfigScreen;
import io.github.bizcub.lib.util.component.ClickEventBuilder;
import io.github.bizcub.lib.util.component.ComponentBuilder;
import io.github.bizcub.lib.util.component.HoverEventBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

@AutoConfig(name = "test")
public class BCConfig implements Config {
    private static ConfigHolder<BCConfig> INSTANCE;

    public static ConfigHolder<BCConfig> getInstance() {
        if (INSTANCE == null) {
            INSTANCE = ConfigHolder.register(BCConfig.class).onSave(BCConfig::onConfigSave);
        }
        return INSTANCE;
    }

    public static Screen getScreen(Screen parent) {
        return new AutoConfigScreen(parent, BCConfig.getInstance());
    }

    public static void onConfigSave(Config config) {
        System.out.println("Config saved!");
    }

    public boolean enableOptimizations = Config.super.enableOptimizations();

    @Tooltip
    @Slider(min = 30, max = 240, step = 10)
    public int fpsLimit = Config.super.fpsLimit();

    @Tooltip
    @Slider(min = 2, max = 32)
    public int renderDistance = Config.super.renderDistance();

    @Tooltip
    @EnumConfig(translate = true)
    public PerformancePreset preset = Config.super.preset();

    @Color
    public int fpsOverlayColor = Config.super.fpsOverlayColor();

    @Color(alpha = true)
    public int profilerBackground = Config.super.profilerBackground();

    @Tooltip
    @ListConfig(expanded = true, addToFront = true)
    public List<String> ignoredMods = Config.super.ignoredMods();

    @ListConfig(editable = false)
    public List<Boolean> perDimensionCulling = Config.super.perDimensionCulling();

    public Component headerNote = ComponentBuilder.literal("Performance Settings")
            .color(ChatFormatting.GOLD)
            .bold()
            .build()
            .copy()
            .append(ComponentBuilder.literal(" (beta)")
                    .color(ChatFormatting.GRAY)
                    .italic()
                    .build()
            );

    public Component emptyNote = ComponentBuilder.literal("Tweak with care")
            .color(ChatFormatting.YELLOW)
            .underline()
            .hoverEvent(HoverEventBuilder.create().showText("Changing these may affect FPS").build())
            .clickEvent(ClickEventBuilder.create().copyToClipboard("456").build())
            .build();

    @ListConfig(editable = false)
    public List<Component> hints = new ArrayList<>(List.of(headerNote, emptyNote));

    @Tooltip
    @ConfigGroup("gameplay")
    public String worldProfile = Config.super.worldProfile();

    @ConfigGroup("gameplay")
    @Slider(min = 0, max = 100)
    public int entityLodBias = Config.super.entityLodBias();

    @ConfigGroup("gameplay")
    public RenderQuality renderQuality = Config.super.renderQuality();

    @ConfigGroup("gameplay")
    public List<ChunkProfile> chunkProfiles = Config.super.chunkProfiles();

    @Category
    public Graphics graphics = Config.super.graphics();

    public static class Graphics {
        public boolean vsync = true;

        @Slider(min = 0, max = 100)
        public int brightness = 50;

        @Color(alpha = true)
        public int overlay = 0x80FF0000;

        public List<String> shaderPacks = new ArrayList<>(List.of("none"));
    }

    public static class ChunkProfile {
        public boolean async = true;

        @Slider(min = 1, max = 16)
        public int workerThreads = 4;

        public String name = "profile";

        public List<Boolean> test = new ArrayList<>(List.of(true));
    }

    public enum PerformancePreset {
        POTATO, FAST, BALANCED, QUALITY;
    }

    public enum RenderQuality {
        LOW, MEDIUM, HIGH, ULTRA;
    }

    @Override
    public boolean enableOptimizations() {
        return this.enableOptimizations;
    }

    @Override
    public int fpsLimit() {
        return this.fpsLimit;
    }

    @Override
    public int renderDistance() {
        return this.renderDistance;
    }

    @Override
    public BCConfig.PerformancePreset preset() {
        return this.preset;
    }

    @Override
    public int fpsOverlayColor() {
        return this.fpsOverlayColor;
    }

    @Override
    public int profilerBackground() {
        return this.profilerBackground;
    }

    @Override
    public List<String> ignoredMods() {
        return this.ignoredMods;
    }

    @Override
    public List<Boolean> perDimensionCulling() {
        return this.perDimensionCulling;
    }

    @Override
    public String worldProfile() {
        return this.worldProfile;
    }

    @Override
    public int entityLodBias() {
        return this.entityLodBias;
    }

    @Override
    public BCConfig.RenderQuality renderQuality() {
        return this.renderQuality;
    }

    @Override
    public List<BCConfig.ChunkProfile> chunkProfiles() {
        return this.chunkProfiles;
    }

    @Override
    public BCConfig.Graphics graphics() {
        return this.graphics;
    }
}
