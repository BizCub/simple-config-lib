package io.github.bizcub.test.main.config;

import io.github.bizcub.simpleConfigLib.autoconfig.ConfigHolder;
import io.github.bizcub.simpleConfigLib.autoconfig.annotation.AutoConfig;
import io.github.bizcub.test.main.TestModMain;

@AutoConfig(name = TestModMain.MOD_ID, fileName = TestModMain.MOD_ID + "_common")
public class SimpleConfigMain implements ConfigMain {
    public static ConfigHolder<SimpleConfigMain> getInstance() {
        return ConfigHolder.register(SimpleConfigMain.class).onSave(config -> System.out.println("Common config saved!"));
    }

    public boolean testMain = ConfigMain.super.testMain();

    @Override
    public boolean testMain() {
        return this.testMain;
    }
}
