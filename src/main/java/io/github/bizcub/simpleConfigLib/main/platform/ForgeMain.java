//? forge {
/*package io.github.bizcub.simpleConfigLib.main.platform;

import io.github.bizcub.simpleConfigLib.main.SimpleConfigLibMain;
import io.github.bizcub.simpleConfigLib.autoconfig.network.ConfigApplyPayload;
import io.github.bizcub.simpleConfigLib.autoconfig.network.ConfigSyncPayload;
import io.github.bizcub.simpleConfigLib.autoconfig.ConfigHolder;
import io.github.bizcub.simpleConfigLib.autoconfig.annotation.Side;
import io.github.bizcub.simpleConfigLib.util.Id;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.fml.common.Mod;

@Mod(SimpleConfigLibMain.MOD_ID)
public class ForgeMain {

    public static final Channel<CustomPacketPayload> CHANNEL =
            ChannelBuilder
                    .named(Id.withDefaultNamespace("config"))
                    .networkProtocolVersion(1)
                    .optional()
                    .payloadChannel()
                    .play()
                    .clientbound()
                    .add(ConfigSyncPayload.TYPE, ConfigSyncPayload.CODEC, (payload, ctx) -> {
                        ctx.enqueueWork(() -> {
                            ConfigHolder<?> holder = ConfigHolder.byName(payload.name());
                            if (holder != null) holder.applySnapshot(payload.json());
                        });
                        ctx.setPacketHandled(true);
                    })
                    .serverbound()
                    .add(ConfigApplyPayload.TYPE, ConfigApplyPayload.CODEC, (payload, ctx) -> {
                        ctx.enqueueWork(() -> {
                            if (!(ctx.getSender() instanceof ServerPlayer player)) return;

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
                                ForgeMain.CHANNEL.send(
                                        new ConfigSyncPayload(holder.getMeta().name(), holder.snapshot()),
                                        PacketDistributor.PLAYER.with(p));
                            }
                        });
                        ctx.setPacketHandled(true);
                    })
                    .build();

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        for (ConfigHolder<?> holder : ConfigHolder.registered()) {
            if (holder.getMeta().env() == Side.Env.SERVER) {
                CHANNEL.send(new ConfigSyncPayload(holder.getMeta().name(), holder.snapshot()),
                        PacketDistributor.PLAYER.with(player));
            }
        }
    }
}*///?}
