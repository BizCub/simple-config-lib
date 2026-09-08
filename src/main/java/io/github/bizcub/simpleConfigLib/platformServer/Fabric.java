//? fabric {
package io.github.bizcub.simpleConfigLib.platformServer;

import io.github.bizcub.simpleConfigLib.autoconfig.ConfigApplyPayload;
import io.github.bizcub.simpleConfigLib.autoconfig.ConfigHolder;
import io.github.bizcub.simpleConfigLib.autoconfig.annotation.Side;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.server.permissions.Permissions;


public class Fabric implements ModInitializer {

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.serverboundPlay().register(ConfigApplyPayload.TYPE, ConfigApplyPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ConfigApplyPayload.TYPE, (payload, context) -> {
            context.server().execute(() -> {
                boolean allowed = hasServerAccess();
                if (!allowed) return;

                ConfigHolder<?> holder = ConfigHolder.byName(payload.name());
                if (holder == null || holder.getMeta().env() != Side.Env.SERVER) return;

                holder.applySnapshot(payload.json());
                holder.save();
            });
        });
    }

    private static boolean hasServerAccess() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.hasSingleplayerServer()) return true;
        if (mc.player == null) return false;
        //? >=26.2 {
        return mc.player.permissions().hasPermission(Permissions.COMMANDS_ADMIN);
        //?} else
        //return mc.player.hasPermissions(2);
    }
}//?}
