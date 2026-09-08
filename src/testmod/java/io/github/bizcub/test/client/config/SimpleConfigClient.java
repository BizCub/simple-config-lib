package io.github.bizcub.test.client.config;

import io.github.bizcub.simpleConfigLib.autoconfig.ConfigHolder;
import io.github.bizcub.simpleConfigLib.autoconfig.annotation.*;
import io.github.bizcub.simpleConfigLib.util.component.ClickEventBuilder;
import io.github.bizcub.simpleConfigLib.util.component.ComponentBuilder;
import io.github.bizcub.simpleConfigLib.util.component.HoverEventBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

@AutoConfig(name = "test_client")
public class SimpleConfigClient implements ConfigClient {
    public static ConfigHolder<SimpleConfigClient> getInstance() {
        return ConfigHolder.register(SimpleConfigClient.class).onSave(SimpleConfigClient::onConfigSave);
    }

    public static void onConfigSave(ConfigClient config) {
        System.out.println("Config saved!");
    }

    @BooleanConfig(yesNo = false)
    public boolean enableOptimizations = ConfigClient.super.enableOptimizations();

    @Tooltip
    @Slider(min = 30, max = 240, step = 10)
    public int fpsLimit = ConfigClient.super.fpsLimit();

    @Tooltip
    @Slider(min = 2, max = 32)
    public int renderDistance = ConfigClient.super.renderDistance();

    @Tooltip
    @EnumConfig(translate = true)
    public PerformancePreset preset = ConfigClient.super.preset();

    @Color
    public int fpsOverlayColor = ConfigClient.super.fpsOverlayColor();

    @Color(alpha = true)
    public int profilerBackground = ConfigClient.super.profilerBackground();

    @Tooltip
    @ListConfig(expanded = true, addToFront = true)
    public List<String> ignoredMods = ConfigClient.super.ignoredMods();

    @ListConfig(editable = false)
    public List<Boolean> perDimensionCulling = ConfigClient.super.perDimensionCulling();

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
    public String worldProfile = ConfigClient.super.worldProfile();

    @ConfigGroup("gameplay")
    @Slider(min = 0, max = 100)
    public int entityLodBias = ConfigClient.super.entityLodBias();

    @ConfigGroup("gameplay")
    public RenderQuality renderQuality = ConfigClient.super.renderQuality();

    @ConfigGroup("gameplay")
    public List<ChunkProfile> chunkProfiles = ConfigClient.super.chunkProfiles();

    @Category
    public Graphics graphics = ConfigClient.super.graphics();

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
