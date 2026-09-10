//? forge {
/*package io.github.bizcub.simpleConfigLib.main.platform;

import io.github.bizcub.simpleConfigLib.main.SimpleConfigLibMain;
import io.github.bizcub.simpleConfigLib.autoconfig.network.ConfigApplyPayload;
import io.github.bizcub.simpleConfigLib.autoconfig.network.ConfigSyncPayload;
import io.github.bizcub.simpleConfigLib.autoconfig.ConfigHolder;
import io.github.bizcub.simpleConfigLib.util.Id;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.network.NetworkDirection;

import java.util.Optional;

//? >=1.20.2 {
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.SimpleChannel;
//?} else {
/^import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
^///?}

@Mod(SimpleConfigLibMain.MOD_ID)
@EventBusSubscriber(modid = SimpleConfigLibMain.MOD_ID)
public class ForgeMain {

    //? >=1.20.5 {
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

    //?} >=1.20.2 {
    /^public static final SimpleChannel CHANNEL =
            ChannelBuilder
                    .named(Id.fromNamespaceAndPath(SimpleConfigLibMain.MOD_ID, "main"))
                    .networkProtocolVersion(1)
                    .acceptedVersions((status, ver) -> true)
                    .simpleChannel()
                    .messageBuilder(ConfigSyncPayload.class, NetworkDirection.PLAY_TO_CLIENT)
                    .encoder((payload, buf) -> buf.writeBytes(payload.toBuffer()))
                    .decoder(ConfigSyncPayload::read)
                    .consumerMainThread((payload, ctx) ->
                            ConfigHolder.applyServerSnapshot(payload.name(), payload.json()))
                    .add()
                    .messageBuilder(ConfigApplyPayload.class, NetworkDirection.PLAY_TO_SERVER)
                    .encoder((payload, buf) -> buf.writeBytes(payload.toBuffer()))
                    .decoder(ConfigApplyPayload::read)
                    .consumerMainThread((payload, ctx) -> {
                        ServerPlayer player = ctx.getSender();
                        if (player == null) return;
                        SimpleConfigLibMain.handleApply(player, payload);
                    })
                    .add();
    ^///?} else {
    /^private static final String NETWORK_PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            Id.fromNamespaceAndPath(SimpleConfigLibMain.MOD_ID, "main"),
            () -> NETWORK_PROTOCOL_VERSION,
            NETWORK_PROTOCOL_VERSION::equals,
            NETWORK_PROTOCOL_VERSION::equals
    );

    private static void registerPayloads() {
        int id = 0;
        CHANNEL.registerMessage(
                id++,
                ConfigSyncPayload.class,
                (payload, buf) -> buf.writeBytes(payload.toBuffer()),
                ConfigSyncPayload::read,
                (payload, ctx) ->
                        ConfigHolder.applyServerSnapshot(payload.name(), payload.json()),
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        CHANNEL.registerMessage(
                id++,
                ConfigApplyPayload.class,
                (payload, buf) -> buf.writeBytes(payload.toBuffer()),
                ConfigApplyPayload::read,
                (payload, ctx) -> {
                    ServerPlayer player = ctx.get().getSender();
                    if (player == null) return;
                    SimpleConfigLibMain.handleApply(player, payload);
                },
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
    }

    public ForgeMain() {
        /^¹? 1.20.1¹^/ //registerPayloads();
    }^///?}

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        SimpleConfigLibMain.onPlayerJoin(player);
    }
}*///?}
