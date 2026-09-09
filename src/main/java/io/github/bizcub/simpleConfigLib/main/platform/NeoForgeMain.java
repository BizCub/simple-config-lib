//? neoforge {
/*package io.github.bizcub.simpleConfigLib.main.platform;

import io.github.bizcub.simpleConfigLib.main.SimpleConfigLibMain;
import io.github.bizcub.simpleConfigLib.autoconfig.network.ConfigApplyPayload;
import io.github.bizcub.simpleConfigLib.autoconfig.network.ConfigSyncPayload;
import io.github.bizcub.simpleConfigLib.autoconfig.ConfigHolder;
import io.github.bizcub.simpleConfigLib.autoconfig.annotation.Side;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@Mod(SimpleConfigLibMain.MOD_ID)
@EventBusSubscriber(modid = SimpleConfigLibMain.MOD_ID)
public class NeoForgeMain {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(
                ConfigApplyPayload.TYPE,
                ConfigApplyPayload.CODEC,
                NeoForgeMain::handleApply);

        registrar.playToClient(
                ConfigSyncPayload.TYPE,
                ConfigSyncPayload.CODEC,
                NeoForgeMain::handleSync);
    }

    private static void handleApply(ConfigApplyPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            //? >=26.2 {
            boolean allowed = player.permissions().hasPermission(Permissions.COMMANDS_ADMIN);
            //?} else
            //boolean allowed = player.hasPermissions(2);
            if (!allowed) return;

            ConfigHolder<?> holder = ConfigHolder.byName(payload.name());
            if (holder == null || holder.getMeta().env() != Side.Env.SERVER) return;

            holder.applySnapshot(payload.json());
            holder.save();

            for (ServerPlayer p : player.level().getServer().getPlayerList().getPlayers()) {
                PacketDistributor.sendToPlayer(p, new ConfigSyncPayload(holder.getMeta().name(), holder.snapshot()));
            }
        });
    }

    private static void handleSync(ConfigSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ConfigHolder<?> holder = ConfigHolder.byName(payload.name());
            if (holder == null || holder.getMeta().env() != Side.Env.SERVER) return;
            holder.applySnapshot(payload.json());
        });
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        for (ConfigHolder<?> holder : ConfigHolder.registered()) {
            if (holder.getMeta().env() == Side.Env.SERVER) {
                PacketDistributor.sendToPlayer(player, new ConfigSyncPayload(holder.getMeta().name(), holder.snapshot()));
            }
        }
    }
}*///?}
