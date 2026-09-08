package io.github.bizcub.test.config;

import io.github.bizcub.simpleConfigLib.autoconfig.ConfigHolder;
import io.github.bizcub.simpleConfigLib.autoconfig.annotation.AutoConfig;
import io.github.bizcub.simpleConfigLib.autoconfig.annotation.Side;

@AutoConfig(name = "test_server", env = Side.Env.SERVER)
public class SimpleConfigServer {
    public static ConfigHolder<SimpleConfigServer> getInstance() {
        return ConfigHolder.register(SimpleConfigServer.class);
    }

    public boolean testBoolean = true;

    public boolean testBoolean() {
        return this.testBoolean;
    }
}
