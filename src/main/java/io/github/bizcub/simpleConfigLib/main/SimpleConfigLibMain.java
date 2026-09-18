package io.github.bizcub.simpleConfigLib.main;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.bizcub.simpleConfigLib.autoconfig.ConfigHolder;
import io.github.bizcub.simpleConfigLib.autoconfig.ConfigSide;
import io.github.bizcub.simpleConfigLib.autoconfig.network.ConfigApplyPayload;
import io.github.bizcub.simpleConfigLib.autoconfig.network.ConfigSyncPayload;
import io.github.bizcub.simpleConfigLib.util.component.ComponentBuilder;import io.github.bizcub.simpleConfigLib.util.network.Network;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
/*? >=1.21.11*/ import net.minecraft.server.permissions.Permissions;

//? fabric {
import net.fabricmc.loader.api.FabricLoader;
//?} forge {
/*import net.minecraftforge.fml.loading.FMLPaths;
*///?} neoforge
//import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public class SimpleConfigLibMain {
    public static final String MOD_ID = /*$ mod_id*/ "simple_config_lib";

    private static boolean payloadsRegistered = false;

    public static Path gameDir() {
        return
                /*? fabric*/ FabricLoader.getInstance().getGameDir();
                /*? forge || neoforge*/ //FMLPaths.GAMEDIR.get();
    }

    public static void registerPayloads() {
        if (payloadsRegistered) return;
        payloadsRegistered = true;

        Network.registerServerbound(ConfigApplyPayload.class, SimpleConfigLibMain::handleApply);
        Network.registerClientbound(ConfigSyncPayload.class,
                payload -> ConfigHolder.applyServerSnapshot(payload.name(), payload.json()));
    }

    public static int reloadAll(MinecraftServer server) {
        int count = 0;
        for (ConfigHolder<?> holder : ConfigHolder.registered()) {
            ConfigSide env = holder.getMeta().side();
            if (env == ConfigSide.SERVER || env == ConfigSide.COMMON) {
                holder.load();
                count++;
                for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                    Network.sendToPlayer(p, new ConfigSyncPayload(holder.id(), holder.snapshot()));
                }
            }
        }
        return count;
    }

    public static LiteralArgumentBuilder<CommandSourceStack> buildReloadCommand() {
        return Commands.literal("simpleconfig")
                //~ if >=1.21.11 '.hasPermission(2)' -> '.permissions().hasPermission(Permissions.COMMANDS_ADMIN)'
                .requires(src -> src.permissions().hasPermission(Permissions.COMMANDS_ADMIN))
                .then(Commands.literal("reload").executes(ctx -> {
                    int n = SimpleConfigLibMain.reloadAll(ctx.getSource().getServer());
                    ctx.getSource().sendSuccess(
                            () -> ComponentBuilder.translatable("text.simple_config_lib.command.reload", n).build(), true);
                    return n;
                }));
    }

    public static void handleApply(ConfigApplyPayload payload, ServerPlayer player) {
        //~ if >=1.21.11 '.hasPermissions(2)' -> '.permissions().hasPermission(Permissions.COMMANDS_ADMIN)'
        boolean allowed = player.permissions().hasPermission(Permissions.COMMANDS_ADMIN);
        if (!allowed) return;

        ConfigHolder<?> holder = ConfigHolder.byId(payload.name());
        if (holder == null) return;
        ConfigSide env = holder.getMeta().side();
        if (env != ConfigSide.SERVER && env != ConfigSide.COMMON) return;

        holder.applySnapshot(payload.json());
        holder.save();

        for (ServerPlayer p : player.level().getServer().getPlayerList().getPlayers()) {
            Network.sendToPlayer(p, new ConfigSyncPayload(holder.id(), holder.snapshot()));
        }
    }

    public static void onPlayerJoin(ServerPlayer player) {
        for (ConfigHolder<?> holder : ConfigHolder.registered()) {
            ConfigSide env = holder.getMeta().side();
            if (env == ConfigSide.SERVER || env == ConfigSide.COMMON) {
                Network.sendToPlayer(player, new ConfigSyncPayload(holder.id(), holder.snapshot()));
            }
        }
    }

    public static void onClientDisconnect() {
        for (ConfigHolder<?> holder : ConfigHolder.registered()) {
            ConfigSide env = holder.getMeta().side();
            if (env == ConfigSide.SERVER || env == ConfigSide.COMMON) {
                holder.load();
            }
        }
    }
}
