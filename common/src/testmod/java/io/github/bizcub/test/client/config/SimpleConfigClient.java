package io.github.bizcub.test.client.config;

import io.github.bizcub.simpleConfigLib.autoconfig.ConfigHolder;
import io.github.bizcub.simpleConfigLib.autoconfig.ConfigSide;
import io.github.bizcub.simpleConfigLib.autoconfig.annotation.*;
import io.github.bizcub.test.main.TestModMain;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;

import java.util.ArrayList;
import java.util.List;

@AutoConfig(name = TestModMain.MOD_ID, fileName = TestModMain.MOD_ID + "_client", side = ConfigSide.CLIENT)
public class SimpleConfigClient {
    private static int changes;

    public static int changes() {
        return changes;
    }

    public static ConfigHolder<SimpleConfigClient> getInstance() {
        return ConfigHolder.register(SimpleConfigClient.class)
                .onSave(config -> {
                    System.out.println("Client config saved! Changes: " + changes);
                    changes = 0;
                })
                .onChange(config -> changes++);
    }

    public boolean testClient = false;

    @ConfigGroup("preview")
    @Slider(min = -100, max = 100)
    public int previewOffsetX = 0;

    @ConfigGroup("preview")
    @Slider(min = -40, max = 40)
    public int previewOffsetY = 0;

    @ConfigGroup("preview")
    @Color
    public int previewColor = 0xFFFFFF;

    @ConfigGroup("items")
    @ItemSelector
    public String selectedItem = "minecraft:diamond";

    @ConfigGroup("items")
    @ItemSelector
    @ListConfig(expanded = true)
    public List<String> selectedItems = new ArrayList<>(List.of("minecraft:apple", "minecraft:diamond_pickaxe"));

    @ConfigGroup("items")
    @ItemSelector
    public String unavailableItem = "missing_mod:example_item";

    @ConfigGroup("lists")
    @ListConfig(expanded = true)
    public List<String> emptyList = new ArrayList<>();

    @ConfigGroup("lists")
    @ListConfig(expanded = true)
    @Slider(min = 0, max = 10)
    public List<Integer> numbers = new ArrayList<>(List.of(1, 5, 9));

    @ConfigGroup("lists")
    @ListConfig(expanded = true)
    public List<Double> decimals = new ArrayList<>(List.of(1.5, 2.5));

    @ConfigGroup("lists")
    @ListConfig(expanded = true)
    public List<String> longList = new ArrayList<>(java.util.stream.IntStream.rangeClosed(1, 20)
            .mapToObj(i -> "Element " + i).toList());

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
