package io.github.bizcub.test.config;

import io.github.bizcub.simpleConfigLib.autoconfig.ConfigHolder;
import io.github.bizcub.simpleConfigLib.autoconfig.annotation.*;
import io.github.bizcub.simpleConfigLib.util.component.ClickEventBuilder;
import io.github.bizcub.simpleConfigLib.util.component.ComponentBuilder;
import io.github.bizcub.simpleConfigLib.util.component.HoverEventBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

@AutoConfig(name = "test")
public class LibConfig implements Config {
    public static ConfigHolder<LibConfig> getInstance() {
        return ConfigHolder.register(LibConfig.class).onSave(LibConfig::onConfigSave);
    }

    public static void onConfigSave(Config config) {
        System.out.println("Config saved!");
    }

    @BooleanConfig(yesNo = false)
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
                    .hoverEvent(HoverEventBuilder.create().showText(" (beta)").build())
                    .clickEvent(ClickEventBuilder.create().copyToClipboard(" (beta)").build())
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
    public PerformancePreset preset() {
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
    public RenderQuality renderQuality() {
        return this.renderQuality;
    }

    @Override
    public List<ChunkProfile> chunkProfiles() {
        return this.chunkProfiles;
    }

    @Override
    public Graphics graphics() {
        return this.graphics;
    }
}
