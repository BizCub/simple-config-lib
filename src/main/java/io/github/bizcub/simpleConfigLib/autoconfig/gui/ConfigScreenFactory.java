package io.github.bizcub.simpleConfigLib.autoconfig.gui;

import io.github.bizcub.simpleConfigLib.autoconfig.ConfigHolder;
import io.github.bizcub.simpleConfigLib.autoconfig.annotation.Env;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
/*? >=1.21.11*/ import net.minecraft.server.permissions.Permissions;

import java.util.List;

public class ConfigScreenFactory {

    public static Screen open(Screen parent) {
        List<ConfigHolder<?>> holders = ConfigHolder.registered();

        if (holders.size() == 1) {
            return screenFor(holders.get(0), parent);
        }
        return new ConfigHubScreen(parent, holders);
    }

    static Screen screenFor(ConfigHolder<?> holder, Screen parent) {
        Env env = holder.getMeta().env();
        boolean readOnly = readOnlyFor(holder);
        return AutoConfigScreen.create(holder, parent, env, readOnly);
    }

    static boolean readOnlyFor(final ConfigHolder<?> holder) {
        Env env = holder.getMeta().env();
        return switch (env) {
            case SERVER -> !hasServerAccess();
            case COMMON -> isConnectedToRemoteServer() && !hasServerAccess();
            case null, default -> false;
        };
    }

    private static boolean isConnectedToRemoteServer() {
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null && !mc.hasSingleplayerServer();
    }

    private static boolean hasServerAccess() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.hasSingleplayerServer()) {
            return true;
        }
        if (mc.player == null) {
            return false;
        }
        //? >=1.21.11 {
        return mc.player.permissions().hasPermission(Permissions.COMMANDS_ADMIN);
        //?} else
        //return mc.player.hasPermissions(2);
    }
}
