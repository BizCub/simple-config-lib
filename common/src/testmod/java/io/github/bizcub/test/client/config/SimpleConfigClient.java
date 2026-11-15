package io.github.bizcub.test.client.config;

import io.github.bizcub.simpleConfigLib.autoconfig.ConfigHolder;
import io.github.bizcub.simpleConfigLib.autoconfig.ConfigSide;
import io.github.bizcub.simpleConfigLib.autoconfig.annotation.*;
import io.github.bizcub.test.main.TestModMain;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;

import java.util.ArrayList;
import java.util.List;

@AutoConfig(name = TestModMain.MOD_ID, fileName = TestModMain.MOD_ID + "_client", side = ConfigSide.CLIENT)
public class SimpleConfigClient {
    public static ConfigHolder<SimpleConfigClient> getInstance() {
        return ConfigHolder.register(SimpleConfigClient.class).onSave(config -> System.out.println("Client config saved!"));
    }

    public boolean testClient = false;

    @BooleanConfig(yesNo = false)
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
    @TextConfig(maxLength = 32)
    @ListConfig(expanded = true, addToFront = true)
    public List<String> ignoredMods = new ArrayList<>(List.of("examplemod"));

    @ListConfig(editable = false)
    public List<Boolean> perDimensionCulling = new ArrayList<>(List.of(true, false, true));

    public Component headerNote = Component.literal("Performance Settings")
            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
            .append(Component.literal(" (beta)").withStyle(style -> style
                    .withColor(ChatFormatting.GRAY)
                    .withHoverEvent(new HoverEvent.ShowText(Component.literal(" (beta)")))
                    .withClickEvent(new ClickEvent.CopyToClipboard(" (beta)"))
                    .withItalic(true)));

    public Component emptyNote = Component.literal("Tweak with care").withStyle(style -> style
            .withColor(ChatFormatting.YELLOW)
            .withUnderlined(true)
            .withHoverEvent(new HoverEvent.ShowText(Component.literal("Changing these may affect FPS")))
            .withClickEvent(new ClickEvent.CopyToClipboard("456")));

    @ListConfig(editable = false)
    public List<Component> hints = new ArrayList<>(List.of(headerNote, emptyNote));

    @Tooltip
    @TextConfig(maxLength = 10)
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
}
