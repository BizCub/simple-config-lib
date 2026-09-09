//? forge {
/*package io.github.bizcub.simpleConfigLib.main.platform;

import io.github.bizcub.simpleConfigLib.main.SimpleConfigLibMain;
import io.github.bizcub.simpleConfigLib.autoconfig.network.ConfigApplyPayload;
import io.github.bizcub.simpleConfigLib.autoconfig.network.ConfigSyncPayload;
import io.github.bizcub.simpleConfigLib.autoconfig.ConfigHolder;
import io.github.bizcub.simpleConfigLib.util.Id;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@Mod(SimpleConfigLibMain.MOD_ID)
@EventBusSubscriber(modid = SimpleConfigLibMain.MOD_ID)
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
                        ctx.enqueueWork(() -> ConfigHolder.applyServerSnapshot(payload.name(), payload.json()));
                        ctx.setPacketHandled(true);
                    })
                    .serverbound()
                    .add(ConfigApplyPayload.TYPE, ConfigApplyPayload.CODEC, (payload, ctx) -> {
                        ctx.enqueueWork(() -> {
                            if (!(ctx.getSender() instanceof ServerPlayer player)) return;
                            SimpleConfigLibMain.handleApply(player, payload);
                        });
                        ctx.setPacketHandled(true);
                    })
                    .build();

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        SimpleConfigLibMain.onPlayerJoin(player);
    }
}*///?}
