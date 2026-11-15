package io.github.bizcub.simpleConfigLib;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.bizcub.simpleConfigLib.autoconfig.ConfigHolder;
import io.github.bizcub.simpleConfigLib.autoconfig.ConfigSide;
import io.github.bizcub.simpleConfigLib.autoconfig.network.ConfigApplyPayload;
import io.github.bizcub.simpleConfigLib.autoconfig.network.ConfigSyncPayload;
import io.github.bizcub.simpleConfigLib.autoconfig.network.ConfigResultPayload;
import io.github.bizcub.simpleConfigLib.internal.network.Network;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

public class SimpleConfigLib {
    public static final String MOD_ID = "simple_config_lib";

    public static int reloadAll(MinecraftServer server) {
        int count = 0;
        for (ConfigHolder<?> holder : ConfigHolder.registered()) {
            ConfigSide env = holder.getMeta().side();
            if (env == ConfigSide.SERVER || env == ConfigSide.COMMON) {
                holder.load();
                count++;
                Network.sendToAllPlayers(server, new ConfigSyncPayload(holder.id(), holder.snapshot()));
            }
        }
        return count;
    }

    public static LiteralArgumentBuilder<CommandSourceStack> buildReloadCommand() {
        return Commands.literal("simpleconfig")
                .requires(src -> src.permissions().hasPermission(Permissions.COMMANDS_ADMIN))
                .then(Commands.literal("reload").executes(ctx -> {
                    int n = reloadAll(ctx.getSource().getServer());
                    ctx.getSource().sendSuccess(
                            () -> Component.translatable("text.simple_config_lib.command.reload", n), true);
                    return n;
                }));
    }

    public static void handleApply(ConfigApplyPayload payload, ServerPlayer player) {
        boolean allowed = player.permissions().hasPermission(Permissions.COMMANDS_ADMIN)
                || player.level().getServer().isSingleplayerOwner(player.nameAndId());
        ConfigHolder<?> holder = ConfigHolder.byId(payload.name());
        boolean success = allowed && holder != null && holder.getMeta().side() != ConfigSide.CLIENT
                && holder.saveSnapshot(payload.json());
        if (success) {
            Network.sendToAllPlayers(player.level().getServer(), new ConfigSyncPayload(holder.id(), holder.snapshot()));
        }
        Network.sendToPlayer(player, new ConfigResultPayload(payload.name(), payload.requestId(), success));
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
