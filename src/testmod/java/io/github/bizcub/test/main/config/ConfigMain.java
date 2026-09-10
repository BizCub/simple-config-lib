package io.github.bizcub.test.main.config;

import io.github.bizcub.simpleConfigLib.autoconfig.ConfigProvider;

public interface ConfigMain {
    static ConfigMain get() {
        return ConfigProvider.get(ConfigMain.class);
    }
    static void set(ConfigMain instance) {
        ConfigProvider.set(ConfigMain.class, instance);
    }

    default boolean testBoolean() {
        return false;
    }
}
