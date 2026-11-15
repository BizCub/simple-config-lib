package io.github.bizcub.simpleConfigLib.platform;

import io.github.bizcub.simpleConfigLib.SimpleConfigLib;
import io.github.bizcub.simpleConfigLib.autoconfig.ConfigHolder;
import io.github.bizcub.simpleConfigLib.autoconfig.network.ConfigApplyPayload;
import io.github.bizcub.simpleConfigLib.autoconfig.network.ConfigSyncPayload;
import io.github.bizcub.simpleConfigLib.autoconfig.network.ConfigResultPayload;
import io.github.bizcub.simpleConfigLib.autoconfig.gui.ConfigScreens;
import io.github.bizcub.simpleConfigLib.internal.PlatformHooks;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;

import java.nio.file.Path;

public class ForgeHooks implements PlatformHooks {
    public static Channel<CustomPacketPayload> CHANNEL;

    @Override
    public boolean isPhysicalClient() {
        return FMLEnvironment.dist == Dist.CLIENT;
    }

    @Override
    public Path gameDir() {
        return FMLPaths.GAMEDIR.get();
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        CHANNEL.send(payload, PacketDistributor.PLAYER.with(player));
    }

    @Override
    public void sendToAllPlayers(MinecraftServer server, ConfigSyncPayload payload) {
        CHANNEL.send(payload, PacketDistributor.ALL.noArg());
    }

    static void buildChannel() {
        CHANNEL = ChannelBuilder.named(Identifier.withDefaultNamespace("config"))
                .networkProtocolVersion(3).optional().payloadChannel().play()
                .clientbound().add(ConfigSyncPayload.TYPE, ConfigSyncPayload.CODEC, (payload, context) -> {
                    context.enqueueWork(() -> ConfigHolder.applyServerSnapshot(payload.name(), payload.json()));
                    context.setPacketHandled(true);
                })
                .clientbound().add(ConfigResultPayload.TYPE, ConfigResultPayload.CODEC, (payload, context) -> {
                    context.enqueueWork(() -> ConfigScreens.handleResult(payload));
                    context.setPacketHandled(true);
                })
                .serverbound().add(ConfigApplyPayload.TYPE, ConfigApplyPayload.CODEC, (payload, context) -> {
                    context.enqueueWork(() -> {
                        if (context.getSender() instanceof ServerPlayer player) SimpleConfigLib.handleApply(payload, player);
                    });
                    context.setPacketHandled(true);
                }).build();
    }
}
