package io.github.bizcub.simpleConfigLib.platform;

import io.github.bizcub.simpleConfigLib.internal.PlatformHooks;
import io.github.bizcub.simpleConfigLib.util.network.PayloadRegistry;
import io.github.bizcub.simpleConfigLib.util.network.SclPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class NeoForgeHooks implements PlatformHooks {

    static final List<ClientboundRegistration<?>> PENDING_CLIENTBOUND = new ArrayList<>();

    record ClientboundRegistration<T extends SclPayload>(
            Class<T> type, BiConsumer<T, Minecraft> handler) {}

    public static <T extends SclPayload> void addPendingClientbound(
            Class<T> type, BiConsumer<T, Minecraft> handler) {
        PENDING_CLIENTBOUND.add(new ClientboundRegistration<>(type, handler));
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isPhysicalClient() {
        return FMLEnvironment.getDist().isClient();
    }

    @Override
    public Path gameDir() {
        return FMLPaths.GAMEDIR.get();
    }

    @Override
    public void sendToPlayer(ServerPlayer player, SclPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    @Override
    public void sendToAllPlayers(MinecraftServer server, SclPayload payload) {
        PacketDistributor.sendToAllPlayers(payload);
    }

    public static <T extends SclPayload> void registerServerbound(
            PayloadRegistrar registrar,
            Class<T> type,
            BiConsumer<T, ServerPlayer> handler) {
        CustomPacketPayload.Type<T> payloadType = PayloadRegistry.typeOf(type);
        StreamCodec<RegistryFriendlyByteBuf, T> codec = PayloadRegistry.codecOf(type);

        registrar.playToServer(payloadType, codec, (payload, context) ->
                context.enqueueWork(() -> {
                    if (!(context.player() instanceof ServerPlayer player)) return;
                    handler.accept(payload, player);
                }));
    }

    public static <T extends SclPayload> void registerClientbound(
            PayloadRegistrar registrar,
            Class<T> type,
            java.util.function.Consumer<T> handler) {
        CustomPacketPayload.Type<T> payloadType = PayloadRegistry.typeOf(type);
        StreamCodec<RegistryFriendlyByteBuf, T> codec = PayloadRegistry.codecOf(type);

        registrar.playToClient(payloadType, codec, (payload, context) ->
                context.enqueueWork(() -> handler.accept(payload)));
    }

    @SuppressWarnings("unchecked")
    static void drainClientbound(PayloadRegistrar registrar) {
        for (ClientboundRegistration<?> entry : PENDING_CLIENTBOUND) {
            ClientboundRegistration<SclPayload> typed = (ClientboundRegistration<SclPayload>) entry;
            registerClientbound(registrar, typed.type(),
                    payload -> typed.handler().accept(payload, Minecraft.getInstance()));
        }
        PENDING_CLIENTBOUND.clear();
    }
}
