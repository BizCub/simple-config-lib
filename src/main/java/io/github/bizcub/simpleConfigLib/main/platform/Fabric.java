//? fabric {
package io.github.bizcub.simpleConfigLib.main.platform;

import io.github.bizcub.simpleConfigLib.autoconfig.network.ConfigApplyPayload;
import io.github.bizcub.simpleConfigLib.autoconfig.ConfigHolder;
import io.github.bizcub.simpleConfigLib.autoconfig.annotation.Side;
import io.github.bizcub.simpleConfigLib.autoconfig.network.ConfigSyncPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

public class Fabric implements ModInitializer {

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.serverboundPlay().register(ConfigApplyPayload.TYPE, ConfigApplyPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ConfigSyncPayload.TYPE, ConfigSyncPayload.CODEC);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            for (ConfigHolder<?> holder : ConfigHolder.registered()) {
                if (holder.getMeta().env() == Side.Env.SERVER) {
                    sender.sendPacket(new ConfigSyncPayload(holder.getMeta().name(), holder.snapshot()));
                }
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(ConfigApplyPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> {
                //? >=26.2 {
                boolean allowed = player.permissions().hasPermission(Permissions.COMMANDS_ADMIN);
                //?} else
                //boolean allowed = player.hasPermissions(2);
                if (!allowed) return;

                ConfigHolder<?> holder = ConfigHolder.byName(payload.name());
                if (holder == null || holder.getMeta().env() != Side.Env.SERVER) return;

                holder.applySnapshot(payload.json());
                holder.save();

                ConfigSyncPayload sync = new ConfigSyncPayload(holder.getMeta().name(), holder.snapshot());
                for (ServerPlayer p : context.server().getPlayerList().getPlayers()) {
                    ServerPlayNetworking.send(p, sync);
                }
            });
        });
    }
}//?}
