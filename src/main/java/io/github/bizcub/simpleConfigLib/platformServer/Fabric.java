//? fabric {
package io.github.bizcub.simpleConfigLib.platformServer;

import io.github.bizcub.simpleConfigLib.autoconfig.ConfigApplyPayload;
import io.github.bizcub.simpleConfigLib.autoconfig.ConfigHolder;
import io.github.bizcub.simpleConfigLib.autoconfig.annotation.Side;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

public class Fabric implements ModInitializer {

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.serverboundPlay().register(ConfigApplyPayload.TYPE, ConfigApplyPayload.CODEC);

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
            });
        });
    }
}//?}
