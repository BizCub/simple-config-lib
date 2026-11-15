package io.github.bizcub.test.main.config;

import io.github.bizcub.simpleConfigLib.autoconfig.ConfigHolder;
import io.github.bizcub.simpleConfigLib.autoconfig.annotation.AutoConfig;
import io.github.bizcub.test.main.TestModMain;

@AutoConfig(name = TestModMain.MOD_ID, fileName = TestModMain.MOD_ID + "_common")
public class SimpleConfigMain {
    public static ConfigHolder<SimpleConfigMain> getInstance() {
        return ConfigHolder.register(SimpleConfigMain.class)
                .onSave(config -> System.out.println("Common config saved!"))
                .onChange(config -> System.out.println("Common config changed!"));
    }

    public boolean testMain = false;
}
