package io.github.bizcub.test.main.config;

import io.github.bizcub.simpleConfigLib.autoconfig.ConfigHolder;
import io.github.bizcub.simpleConfigLib.autoconfig.ConfigSide;
import io.github.bizcub.simpleConfigLib.autoconfig.annotation.AutoConfig;
import io.github.bizcub.test.main.TestModMain;

@AutoConfig(name = TestModMain.MOD_ID, fileName = TestModMain.MOD_ID + "_server", side = ConfigSide.SERVER)
public class SimpleConfigServer {
    public static ConfigHolder<SimpleConfigServer> getInstance() {
        return ConfigHolder.register(SimpleConfigServer.class)
                .onSave(config -> System.out.println("Server config saved!"))
                .onChange(config -> System.out.println("Server config changed!"));
    }

    public boolean testServer = false;
}
