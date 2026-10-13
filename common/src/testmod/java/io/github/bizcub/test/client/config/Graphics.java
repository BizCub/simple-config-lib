package io.github.bizcub.test.client.config;

import io.github.bizcub.simpleConfigLib.autoconfig.annotation.Color;
import io.github.bizcub.simpleConfigLib.autoconfig.annotation.Slider;

import java.util.ArrayList;
import java.util.List;

public class Graphics {
    public boolean vsync = true;

    public double brightness = 50;

    @Color(alpha = true)
    public int overlay = 0x80FF0000;

    public List<String> shaderPacks = new ArrayList<>(List.of("none"));
}
