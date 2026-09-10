package io.github.bizcub.simpleConfigLib.main;

import io.github.bizcub.simpleConfigLib.autoconfig.ConfigHolder;
import io.github.bizcub.simpleConfigLib.autoconfig.annotation.Side;
import io.github.bizcub.simpleConfigLib.autoconfig.network.ConfigApplyPayload;
import io.github.bizcub.simpleConfigLib.autoconfig.network.ConfigSyncPayload;
import io.github.bizcub.simpleConfigLib.util.network.PayloadRegistry;
import net.minecraft.server.level.ServerPlayer;
/*? >=1.21.11*/ import net.minecraft.server.permissions.Permissions;

//? fabric {
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
//?} forge {
/*import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import static io.github.bizcub.simpleConfigLib.main.platform.ForgeMain.CHANNEL;
*///?} neoforge {
/*import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.network.PacketDistributor;
*///?}

import java.nio.file.Path;

public class SimpleConfigLibMain {
    public static final String MOD_ID = /*$ mod_id*/ "simple_config_lib";

    public static Path gameDir() {
        return
                /*? fabric*/ FabricLoader.getInstance().getGameDir();
                /*? forge || neoforge*/ //FMLPaths.GAMEDIR.get();
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

        ConfigSyncPayload sync = new ConfigSyncPayload(holder.getMeta().name(), holder.snapshot());
        for (ServerPlayer p : player.level().getServer().getPlayerList().getPlayers()) {
            sendPayloadS2C(p, sync);
        }
    }

    public static void onPlayerJoin(ServerPlayer player) {
        for (ConfigHolder<?> holder : ConfigHolder.registered()) {
            if (holder.getMeta().env() == Side.Env.SERVER) {
                sendPayloadS2C(player, new ConfigSyncPayload(holder.getMeta().name(), holder.snapshot()));
            }
        }
    }

    public static void sendPayloadS2C(ServerPlayer player, ConfigSyncPayload payload) {
        /*? fabric*/ ServerPlayNetworking.send(player, /*? <1.20.5 {*/ /*ConfigApplyPayload.ID, PayloadRegistry.toBuffer(payload) *//*?} else >> ')'*/ payload);
        /*? forge && >=1.20.2*/ //CHANNEL.send(payload, PacketDistributor.PLAYER.with(player));
        /*? forge && 1.20.1*/ //CHANNEL.sendTo(payload, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
        /*? neoforge*/ //PacketDistributor.sendToPlayer(player, payload);
    }
}
