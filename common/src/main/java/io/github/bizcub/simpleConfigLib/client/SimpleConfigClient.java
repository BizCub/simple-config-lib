package io.github.bizcub.simpleConfigLib.client;

import io.github.bizcub.simpleConfigLib.SimpleConfigLib;
import io.github.bizcub.simpleConfigLib.autoconfig.ConfigHolder;
import io.github.bizcub.simpleConfigLib.autoconfig.ConfigSide;
import io.github.bizcub.simpleConfigLib.autoconfig.annotation.AutoConfig;
import io.github.bizcub.simpleConfigLib.autoconfig.annotation.Color;
import io.github.bizcub.simpleConfigLib.autoconfig.annotation.Slider;
import io.github.bizcub.simpleConfigLib.autoconfig.annotation.Tooltip;

@AutoConfig(name = SimpleConfigLib.MOD_ID, side = ConfigSide.CLIENT, translate = true)
public class SimpleConfigClient {
    private static final ConfigHolder<SimpleConfigClient> HOLDER = ConfigHolder.register(SimpleConfigClient.class);

    public static ConfigHolder<SimpleConfigClient> getInstance() {
        return HOLDER;
    }

    @Tooltip
    @Slider(min = 100, max = 500, step = 10)
    public int tooltipMaxWidth = 200;

    @Tooltip
    @Color
    public int accentColor = 0x66BBFF;

    @Tooltip
    @Slider(min = 320, max = 1000, step = 20)
    public int editorMaxWidth = 520;

    @Tooltip
    public boolean alwaysShowSearch = false;

    @Tooltip
    public boolean slidersAsNumbers = false;
}
