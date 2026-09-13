package io.github.bizcub.test.main.config;

import io.github.bizcub.simpleConfigLib.autoconfig.ConfigHolder;
import io.github.bizcub.simpleConfigLib.autoconfig.annotation.AutoConfig;

@AutoConfig(name = "test_common")
public class SimpleConfigMain implements ConfigMain {
    public static ConfigHolder<SimpleConfigMain> getInstance() {
        return ConfigHolder.register(SimpleConfigMain.class);
    }

    public boolean testMain = ConfigMain.super.testMain();

    @Override
    public boolean testMain() {
        return this.testMain;
    }
}
