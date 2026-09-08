package io.github.bizcub.test.config;

import io.github.bizcub.simpleConfigLib.autoconfig.ConfigHolder;
import io.github.bizcub.simpleConfigLib.autoconfig.annotation.*;
import io.github.bizcub.simpleConfigLib.util.component.ClickEventBuilder;
import io.github.bizcub.simpleConfigLib.util.component.ComponentBuilder;
import io.github.bizcub.simpleConfigLib.util.component.HoverEventBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

@AutoConfig(name = "test_server", env = Side.Env.SERVER)
public class SimpleConfigServer {
    public static ConfigHolder<SimpleConfigServer> getInstance() {
        return ConfigHolder.register(SimpleConfigServer.class);
    }

    public boolean testBoolean = true;
}
