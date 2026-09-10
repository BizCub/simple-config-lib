package io.github.bizcub.simpleConfigLib.util.network;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

//? fabric {
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
//?} forge {
/*import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import static io.github.bizcub.simpleConfigLib.main.platform.ForgeMain.CHANNEL;
*///?} neoforge
//import net.neoforged.neoforge.network.PacketDistributor;

public final class Network {

    public record Serverbound<T extends SclPayload>(Class<T> type, BiConsumer<T, ServerPlayer> handler) {}
    public record Clientbound<T extends SclPayload>(Class<T> type, Consumer<T> handler) {}

    private static final List<Serverbound<?>> SERVERBOUND = new ArrayList<>();
    private static final List<Clientbound<?>> CLIENTBOUND = new ArrayList<>();

    public static <T extends SclPayload> void registerServerbound(Class<T> type, BiConsumer<T, ServerPlayer> handler) {
        PayloadRegistry.validate(type);
        SERVERBOUND.add(new Serverbound<>(type, handler));
    }

    public static <T extends SclPayload> void registerClientbound(Class<T> type, Consumer<T> handler) {
        PayloadRegistry.validate(type);
        CLIENTBOUND.add(new Clientbound<>(type, handler));
    }

    public static List<Serverbound<?>> serverbound() {
        return SERVERBOUND;
    }
    public static List<Clientbound<?>> clientbound() {
        return CLIENTBOUND;
    }

    public static void sendToPlayer(ServerPlayer player, SclPayload payload) {
        /*? fabric*/ ServerPlayNetworking.send(player,/*? <1.20.5 {*/ /*PayloadRegistry.idOf(payload.getClass()), PayloadRegistry.toBuffer(payload) *//*?} else >> ')'*/ payload);
        /*? forge && >=1.20.2*/ //CHANNEL.send(payload, PacketDistributor.PLAYER.with(player));
        /*? forge && 1.20.1*/ //CHANNEL.sendTo(payload, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
        /*? neoforge*/ //PacketDistributor.sendToPlayer(player, payload);
    }

    public static void sendToAllPlayers(MinecraftServer server, SclPayload payload) {
        //? fabric {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            //? >=1.20.5 {
            ServerPlayNetworking.send(player, payload);
            //?} else {
            /*ServerPlayNetworking.send(player, PayloadRegistry.idOf(payload.getClass()), PayloadRegistry.toBuffer(payload));*///?}
        }
        //?} forge {
        /*//? >=1.20.2 {
        CHANNEL.send(payload, PacketDistributor.ALL.noArg());
        //?} else {
        /^CHANNEL.send(PacketDistributor.ALL.noArg(), payload);^///?}
        *///?} neoforge {
        /*PacketDistributor.sendToAllPlayers(payload);*///?}
    }
}
