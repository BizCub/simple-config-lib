package io.github.bizcub.test.main.config;

import io.github.bizcub.simpleConfigLib.autoconfig.ConfigHolder;
import io.github.bizcub.simpleConfigLib.autoconfig.ConfigSide;
import io.github.bizcub.simpleConfigLib.autoconfig.annotation.AutoConfig;

@AutoConfig(name = "test_server", side = ConfigSide.SERVER)
public class SimpleConfigServer implements ConfigServer {
    public static ConfigHolder<SimpleConfigServer> getInstance() {
        return ConfigHolder.register(SimpleConfigServer.class);
    }

    public boolean testServer = ConfigServer.super.testServer();

    @Override
    public boolean testServer() {
        return this.testServer;
    }
}
