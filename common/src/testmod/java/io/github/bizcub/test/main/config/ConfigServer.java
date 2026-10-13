package io.github.bizcub.test.main.config;

import io.github.bizcub.simpleConfigLib.autoconfig.ConfigProvider;

public interface ConfigServer {
    static ConfigServer get() {
        return ConfigProvider.get(ConfigServer.class);
    }
    static void set(ConfigServer instance) {
        ConfigProvider.set(ConfigServer.class, instance);
    }

    default boolean testServer() {
        return false;
    }
}
