package com.bizcub.test;

import com.bizcub.lib.autoconfig.ConfigHolder;
import com.bizcub.test.config.MyModConfig;

public class Main {
    public static final String MOD_ID = "test_lib";
    public static ConfigHolder<MyModConfig> CONFIG;

    public static void init() {
        CONFIG = ConfigHolder.register(MyModConfig.class);
    }
}
