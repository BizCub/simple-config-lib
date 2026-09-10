package io.github.bizcub.simpleConfigLib.main;

import io.github.bizcub.simpleConfigLib.autoconfig.ConfigHolder;
import io.github.bizcub.simpleConfigLib.autoconfig.annotation.Side;
import io.github.bizcub.simpleConfigLib.autoconfig.network.ConfigApplyPayload;
import io.github.bizcub.simpleConfigLib.autoconfig.network.ConfigSyncPayload;
import io.github.bizcub.simpleConfigLib.util.network.Network;
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

    public static Path gameDir() {
        return
                /*? fabric*/ FabricLoader.getInstance().getGameDir();
                /*? forge || neoforge*/ //FMLPaths.GAMEDIR.get();
    }

    public static void registerPayloads() {
        Network.registerServerbound(ConfigApplyPayload.class, SimpleConfigLibMain::handleApply);
        Network.registerClientbound(ConfigSyncPayload.class,
                payload -> ConfigHolder.applyServerSnapshot(payload.name(), payload.json()));
    }

    public static void handleApply(ConfigApplyPayload payload, ServerPlayer player) {
        //? >=1.21.11 {
        boolean allowed = player.permissions().hasPermission(Permissions.COMMANDS_ADMIN);
        //?} else
        //boolean allowed = player.hasPermissions(2);
        if (!allowed) return;

        ConfigHolder<?> holder = ConfigHolder.byName(payload.name());
        if (holder == null || holder.getMeta().env() != Side.Env.SERVER) return;

        holder.applySnapshot(payload.json());
        holder.save();

        for (ServerPlayer p : player.level().getServer().getPlayerList().getPlayers()) {
            Network.sendToPlayer(p, new ConfigSyncPayload(holder.getMeta().name(), holder.snapshot()));
        }
    }

    public static void onPlayerJoin(ServerPlayer player) {
        for (ConfigHolder<?> holder : ConfigHolder.registered()) {
            if (holder.getMeta().env() == Side.Env.SERVER) {
                Network.sendToPlayer(player, new ConfigSyncPayload(holder.getMeta().name(), holder.snapshot()));
            }
        }
    }
}
