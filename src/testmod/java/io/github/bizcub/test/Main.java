package io.github.bizcub.test;

import io.github.bizcub.lib.autoconfig.ConfigHolder;
import io.github.bizcub.test.config.MyModConfig;

public class Main {
    public static final String MOD_ID = "test_lib";
    public static ConfigHolder<MyModConfig> CONFIG;

    public static void init() {
        CONFIG = ConfigHolder.register(MyModConfig.class);
    }
}
