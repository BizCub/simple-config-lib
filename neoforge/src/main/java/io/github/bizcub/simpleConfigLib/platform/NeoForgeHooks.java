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
import java.util.function.Consumer;

public class NeoForgeHooks implements PlatformHooks {

    static final List<ServerboundRegistration<?>> PENDING_SERVERBOUND = new ArrayList<>();
    static final List<ClientboundRegistration<?>> PENDING_CLIENTBOUND = new ArrayList<>();
    static final List<Class<? extends SclPayload>> PENDING_TYPES = new ArrayList<>();

    private static boolean drained;

    record ServerboundRegistration<T extends SclPayload>(
            Class<T> type, BiConsumer<T, ServerPlayer> handler) {}

    record ClientboundRegistration<T extends SclPayload>(
            Class<T> type, BiConsumer<T, Minecraft> handler) {}

    public static <T extends SclPayload> void addPendingClientbound(
            Class<T> type, BiConsumer<T, Minecraft> handler) {
        checkNotDrained();
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

    @Override
    public <T extends SclPayload> void registerServerbound(
            Class<T> type, BiConsumer<T, ServerPlayer> handler) {
        checkNotDrained();
        PENDING_SERVERBOUND.add(new ServerboundRegistration<>(type, handler));
    }

    @Override
    public <T extends SclPayload> void registerClientboundType(Class<T> type) {
        checkNotDrained();
        PENDING_TYPES.add(type);
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
            Consumer<T> handler) {
        CustomPacketPayload.Type<T> payloadType = PayloadRegistry.typeOf(type);
        StreamCodec<RegistryFriendlyByteBuf, T> codec = PayloadRegistry.codecOf(type);

        registrar.playToClient(payloadType, codec, (payload, context) ->
                context.enqueueWork(() -> handler.accept(payload)));
    }

    @SuppressWarnings("unchecked")
    static void drain(PayloadRegistrar registrar) {
        for (ServerboundRegistration<?> entry : PENDING_SERVERBOUND) {
            ServerboundRegistration<SclPayload> typed = (ServerboundRegistration<SclPayload>) entry;
            registerServerbound(registrar, typed.type(), typed.handler());
        }

        for (ClientboundRegistration<?> entry : PENDING_CLIENTBOUND) {
            ClientboundRegistration<SclPayload> typed = (ClientboundRegistration<SclPayload>) entry;
            registerClientbound(registrar, typed.type(),
                    payload -> typed.handler().accept(payload, Minecraft.getInstance()));
        }

        for (Class<? extends SclPayload> type : PENDING_TYPES) {
            boolean hasHandler = PENDING_CLIENTBOUND.stream()
                    .anyMatch(e -> e.type() == type);
            if (!hasHandler) {
                registerClientbound(registrar, (Class<SclPayload>) type, payload -> {});
            }
        }

        PENDING_SERVERBOUND.clear();
        PENDING_CLIENTBOUND.clear();
        PENDING_TYPES.clear();
        drained = true;
    }

    private static void checkNotDrained() {
        if (drained) {
            throw new IllegalStateException(
                    "Payloads must be registered before RegisterPayloadHandlersEvent");
        }
    }
}
