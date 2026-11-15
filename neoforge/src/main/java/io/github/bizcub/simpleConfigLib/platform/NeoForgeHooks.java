package io.github.bizcub.simpleConfigLib.platform;

import io.github.bizcub.simpleConfigLib.SimpleConfigLib;
import io.github.bizcub.simpleConfigLib.autoconfig.ConfigHolder;
import io.github.bizcub.simpleConfigLib.autoconfig.gui.ConfigScreens;
import io.github.bizcub.simpleConfigLib.autoconfig.network.ConfigApplyPayload;
import io.github.bizcub.simpleConfigLib.autoconfig.network.ConfigResultPayload;
import io.github.bizcub.simpleConfigLib.autoconfig.network.ConfigSyncPayload;
import io.github.bizcub.simpleConfigLib.internal.PlatformHooks;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.nio.file.Path;

public class NeoForgeHooks implements PlatformHooks {

    @Override
    public boolean isPhysicalClient() {
        return FMLEnvironment.getDist().isClient();
    }

    @Override
    public Path gameDir() {
        return FMLPaths.GAMEDIR.get();
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    @Override
    public void sendToAllPlayers(MinecraftServer server, ConfigSyncPayload payload) {
        PacketDistributor.sendToAllPlayers(payload);
    }

    static void registerPayloads(PayloadRegistrar registrar) {
        registrar.playToServer(ConfigApplyPayload.TYPE, ConfigApplyPayload.CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) SimpleConfigLib.handleApply(payload, player);
                }));
        registrar.playToClient(ConfigSyncPayload.TYPE, ConfigSyncPayload.CODEC, (payload, context) ->
                context.enqueueWork(() -> ConfigHolder.applyServerSnapshot(payload.name(), payload.json())));
        registrar.playToClient(ConfigResultPayload.TYPE, ConfigResultPayload.CODEC, (payload, context) ->
                context.enqueueWork(() -> ConfigScreens.handleResult(payload)));
    }
}
