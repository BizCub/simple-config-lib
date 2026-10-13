package io.github.bizcub.simpleConfigLib.platform;

import io.github.bizcub.simpleConfigLib.internal.PlatformHooks;
import io.github.bizcub.simpleConfigLib.SimpleConfigLib;
import io.github.bizcub.simpleConfigLib.util.Id;
import io.github.bizcub.simpleConfigLib.util.network.Network;
import io.github.bizcub.simpleConfigLib.util.network.PayloadRegistry;
import io.github.bizcub.simpleConfigLib.util.network.SclPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

public class ForgeHooks implements PlatformHooks {

    public static Channel<CustomPacketPayload> CHANNEL;

    static final List<ClientboundRegistration<?>> PENDING_CLIENTBOUND = new ArrayList<>();

    public record ClientboundRegistration<T extends SclPayload>(
            Class<T> type, BiConsumer<T, Minecraft> handler) {}

    static final List<ServerboundRegistration<?>> PENDING_SERVERBOUND = new ArrayList<>();

    public record ServerboundRegistration<T extends SclPayload>(
            Class<T> type, BiConsumer<T, ServerPlayer> handler) {}

    @Override
    public <T extends SclPayload> void registerServerbound(Class<T> type, BiConsumer<T, ServerPlayer> handler) {
        PENDING_SERVERBOUND.add(new ServerboundRegistration<>(type, handler));
    }

    @Override
    public <T extends SclPayload> void registerClientboundType(Class<T> type) {
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.isLoaded(modId);
    }

    @Override
    public boolean isPhysicalClient() {
        return FMLEnvironment.dist == Dist.CLIENT;
    }

    @Override
    public Path gameDir() {
        return FMLPaths.GAMEDIR.get();
    }

    @Override
    public void sendToPlayer(ServerPlayer player, SclPayload payload) {
        CHANNEL.send(payload, PacketDistributor.PLAYER.with(player));
    }

    @Override
    public void sendToAllPlayers(MinecraftServer server, SclPayload payload) {
        CHANNEL.send(payload, PacketDistributor.ALL.noArg());
    }

    @SuppressWarnings("unchecked")
    static void buildChannel() {
        SimpleConfigLib.registerPayloads();

        var play = ChannelBuilder
                .named(Id.withDefaultNamespace("config"))
                .networkProtocolVersion(1)
                .optional()
                .payloadChannel()
                .play();

        Set<Class<?>> registeredTypes = new HashSet<>();

        var clientboundBuilder = play.clientbound();
        for (ClientboundRegistration<?> entry : PENDING_CLIENTBOUND) {
            ClientboundRegistration<SclPayload> typed = (ClientboundRegistration<SclPayload>) entry;
            clientboundBuilder = clientboundBuilder.add(
                    PayloadRegistry.typeOf(typed.type()),
                    PayloadRegistry.codecOf(typed.type()),
                    (payload, ctx) -> {
                        ctx.enqueueWork(() ->
                                typed.handler().accept(payload, Minecraft.getInstance()));
                        ctx.setPacketHandled(true);
                    });
            registeredTypes.add(typed.type());
        }
        for (Network.Clientbound<?> entry : Network.clientbound()) {
            if (registeredTypes.contains(entry.type())) {
                continue;
            }
            Network.Clientbound<SclPayload> typed = (Network.Clientbound<SclPayload>) entry;
            clientboundBuilder = clientboundBuilder.add(
                    PayloadRegistry.typeOf(typed.type()),
                    PayloadRegistry.codecOf(typed.type()),
                    (payload, ctx) -> {
                        ctx.enqueueWork(() -> typed.handler().accept(payload));
                        ctx.setPacketHandled(true);
                    });
            registeredTypes.add(typed.type());
        }

        var serverboundBuilder = clientboundBuilder.serverbound();
        for (ServerboundRegistration<?> entry : PENDING_SERVERBOUND) {
            ServerboundRegistration<SclPayload> typed = (ServerboundRegistration<SclPayload>) entry;
            serverboundBuilder = serverboundBuilder.add(
                    PayloadRegistry.typeOf(typed.type()),
                    PayloadRegistry.codecOf(typed.type()),
                    (payload, ctx) -> {
                        ctx.enqueueWork(() -> {
                            if (!(ctx.getSender() instanceof ServerPlayer player)) return;
                            typed.handler().accept(payload, player);
                        });
                        ctx.setPacketHandled(true);
                    });
        }

        CHANNEL = serverboundBuilder.build();
        PENDING_CLIENTBOUND.clear();
        PENDING_SERVERBOUND.clear();
    }

    public static <T extends SclPayload> void addPendingClientbound(
            Class<T> type, BiConsumer<T, Minecraft> handler) {
        PENDING_CLIENTBOUND.add(new ClientboundRegistration<>(type, handler));
    }
}
