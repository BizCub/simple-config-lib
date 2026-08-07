package io.github.bizcub.test.config;

import io.github.bizcub.simpleConfigLib.autoconfig.annotation.Slider;

import java.util.ArrayList;
import java.util.List;

public class ChunkProfile {
    public boolean async = true;

    @Slider(min = 1, max = 16)
    public int workerThreads = 4;

    public String name = "profile";

    public List<Boolean> test = new ArrayList<>(List.of(true));
}
