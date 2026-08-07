package io.github.bizcub.test.config;

import io.github.bizcub.lib.autoconfig.annotation.Color;
import io.github.bizcub.lib.autoconfig.annotation.Slider;

import java.util.ArrayList;
import java.util.List;

public class Graphics {
    public boolean vsync = true;

    @Slider(min = 0, max = 100)
    public int brightness = 50;

    @Color(alpha = true)
    public int overlay = 0x80FF0000;

    public List<String> shaderPacks = new ArrayList<>(List.of("none"));
}
