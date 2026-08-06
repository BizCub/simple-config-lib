package io.github.bizcub.test.config;

import io.github.bizcub.lib.autoconfig.annotation.*;
import io.github.bizcub.lib.util.component.ClickEventBuilder;
import io.github.bizcub.lib.util.component.ComponentBuilder;
import io.github.bizcub.lib.util.component.HoverEventBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

@AutoConfig(name = "test", snakeCaseKeys = true, translate = true)
public class MyModConfig {
    public boolean enableOptimizations = true;

    @Tooltip
    @Slider(min = 30, max = 240, step = 10)
    public int fpsLimit = 120;

    @Tooltip
    @Slider(min = 2, max = 32)
    public int renderDistance = 12;

    @Tooltip
    @EnumConfig(translate = true)
    public PerformancePreset preset = PerformancePreset.BALANCED;

    @Color
    public int fpsOverlayColor = 0x00FF00;

    @Color(alpha = true)
    public int profilerBackground = 0x80202020;

    @Tooltip
    @ListConfig(expanded = true, addToFront = true)
    public List<String> ignoredMods = new ArrayList<>(List.of("examplemod"));

    @ListConfig(editable = false)
    public List<Boolean> perDimensionCulling = new ArrayList<>(List.of(true, false, true));

    public Component headerNote = ComponentBuilder.literal("Performance Settings")
            .color(ChatFormatting.RED)
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
    public String worldProfile = "default";

    @ConfigGroup("gameplay")
    @Slider(min = 0, max = 100)
    public int entityLodBias = 50;

    @ConfigGroup("gameplay")
    public RenderQuality renderQuality = RenderQuality.MEDIUM;

    @ConfigGroup("gameplay")
    public List<ChunkProfile> chunkProfiles = new ArrayList<>(List.of(new ChunkProfile(), new ChunkProfile()));

    @Category
    public Graphics graphics = new Graphics();

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
}
