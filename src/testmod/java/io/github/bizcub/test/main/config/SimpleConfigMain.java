package io.github.bizcub.test.main.config;

import io.github.bizcub.simpleConfigLib.autoconfig.ConfigHolder;
import io.github.bizcub.simpleConfigLib.autoconfig.annotation.AutoConfig;
import io.github.bizcub.simpleConfigLib.autoconfig.annotation.Env;

@AutoConfig(name = "test_main", env = Env.SERVER)
public class SimpleConfigMain implements ConfigMain {
    public static ConfigHolder<SimpleConfigMain> getInstance() {
        return ConfigHolder.register(SimpleConfigMain.class);
    }

    public boolean testBoolean = ConfigMain.super.testBoolean();

    @Override
    public boolean testBoolean() {
        return this.testBoolean;
    }
}
