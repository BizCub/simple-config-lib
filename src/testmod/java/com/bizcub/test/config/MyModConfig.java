package com.bizcub.test.config;

import com.bizcub.lib.autoconfig.annotation.*;
import net.minecraft.ChatFormatting;
//import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
//import net.minecraft.network.chat.HoverEvent;

import java.util.ArrayList;
import java.util.List;

@Config(name = "test", snakeCaseKeys = true)
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

    public Component headerNote = Component.literal("Performance Settings")
            .withStyle(style -> style
                    .withColor(ChatFormatting.GOLD)
                    .withBold(true))
            .append(Component.literal(" (beta)")
                    .withStyle(style -> style
                            .withColor(ChatFormatting.GRAY)
                            .withItalic(true)));

    public Component emptyNote = Component.literal("Tweak with care")
            .withStyle(style -> style
                    .withColor(ChatFormatting.YELLOW)
                    .withUnderlined(true)
//                    .withHoverEvent(new HoverEvent.ShowText(
//                            Component.literal("Changing these may affect FPS")))
//                    .withClickEvent(new ClickEvent.CopyToClipboard("456"))
//                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
//                            Component.literal("Changing these may affect FPS")))
//                    .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, "456"))
            );

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
