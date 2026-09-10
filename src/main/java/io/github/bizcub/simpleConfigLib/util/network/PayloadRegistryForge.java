//? forge {
/*package io.github.bizcub.simpleConfigLib.util.network;

import io.github.bizcub.simpleConfigLib.util.Id;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

//? >=1.20.5 {
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
//?} >=1.20.2 {
/^import net.minecraftforge.network.SimpleChannel;
import net.minecraftforge.network.ChannelBuilder;
^///?} else {
/^import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
^///?}

public final class PayloadRegistryForge {
    private record Serverbound<T extends SclPayload>(Class<T> type, BiConsumer<T, ServerPlayer> handler) {}
    private record Clientbound<T extends SclPayload>(Class<T> type, Consumer<T> handler) {}

    public static Builder builder(String channelPath) {
        return new Builder(channelPath);
    }

    public static final class Builder {
        private final String channelPath;
        private final List<Serverbound<?>> serverbound = new ArrayList<>();
        private final List<Clientbound<?>> clientbound = new ArrayList<>();

        private Builder(String channelPath) { this.channelPath = channelPath; }

        public <T extends SclPayload> Builder serverbound(Class<T> type, BiConsumer<T, ServerPlayer> handler) {
            serverbound.add(new Serverbound<>(type, handler));
            return this;
        }

        public <T extends SclPayload> Builder clientbound(Class<T> type, Consumer<T> handler) {
            clientbound.add(new Clientbound<>(type, handler));
            return this;
        }

        //? >=1.20.5 {
        @SuppressWarnings("unchecked")
        public Channel<CustomPacketPayload> build() {
            var play = ChannelBuilder
                    .named(Id.withDefaultNamespace(channelPath))
                    .networkProtocolVersion(1)
                    .optional()
                    .payloadChannel()
                    .play();

            var cb = play.clientbound();
            for (Clientbound<?> reg : clientbound) {
                Clientbound<SclPayload> r = (Clientbound<SclPayload>) reg;
                cb = cb.add(
                        PayloadRegistry.typeOf(r.type()),
                        PayloadRegistry.codecOf(r.type()),
                        (payload, ctx) -> {
                            ctx.enqueueWork(() -> r.handler().accept(payload));
                            ctx.setPacketHandled(true);
                        });
            }

            var sb = cb.serverbound();
            for (Serverbound<?> reg : serverbound) {
                Serverbound<SclPayload> r = (Serverbound<SclPayload>) reg;
                sb = sb.add(
                        PayloadRegistry.typeOf(r.type()),
                        PayloadRegistry.codecOf(r.type()),
                        (payload, ctx) -> {
                            ctx.enqueueWork(() -> {
                                if (!(ctx.getSender() instanceof ServerPlayer player)) return;
                                r.handler().accept(payload, player);
                            });
                            ctx.setPacketHandled(true);
                        });
            }
            return sb.build();
        }
        //?} >=1.20.2 {
        /^@SuppressWarnings("unchecked")
        public SimpleChannel build() {
            var mb = ChannelBuilder
                    .named(Id.withDefaultNamespace(channelPath))
                    .networkProtocolVersion(1)
                    .acceptedVersions((status, ver) -> true)
                    .simpleChannel();

            for (Clientbound<?> reg : clientbound) {
                Clientbound<SclPayload> r = (Clientbound<SclPayload>) reg;
                mb = mb.messageBuilder((Class<SclPayload>) r.type(), NetworkDirection.PLAY_TO_CLIENT)
                        .encoder((payload, buf) -> PayloadRegistry.write(payload, buf))
                        .decoder(buf -> PayloadRegistry.read(r.type(), buf))
                        .consumerMainThread((payload, ctx) -> r.handler().accept(payload))
                        .add();
            }
            for (Serverbound<?> reg : serverbound) {
                Serverbound<SclPayload> r = (Serverbound<SclPayload>) reg;
                mb = mb.messageBuilder((Class<SclPayload>) r.type(), NetworkDirection.PLAY_TO_SERVER)
                        .encoder((payload, buf) -> PayloadRegistry.write(payload, buf))
                        .decoder(buf -> PayloadRegistry.read(r.type(), buf))
                        .consumerMainThread((payload, ctx) -> {
                            ServerPlayer player = ctx.getSender();
                            if (player == null) return;
                            r.handler().accept(payload, player);
                        })
                        .add();
            }
            return mb.build();
        }
        ^///?} else {
        /^private static final String PROTO = "1";

        @SuppressWarnings("unchecked")
        public SimpleChannel build() {
            SimpleChannel channel = NetworkRegistry.newSimpleChannel(
                    Id.withDefaultNamespace(channelPath),
                    () -> PROTO, PROTO::equals, PROTO::equals);

            int id = 0;
            for (Clientbound<?> reg : clientbound) {
                Clientbound<SclPayload> r = (Clientbound<SclPayload>) reg;
                channel.registerMessage(id++, (Class<SclPayload>) r.type(),
                        (payload, buf) -> PayloadRegistry.write(payload, buf),
                        buf -> PayloadRegistry.read(r.type(), buf),
                        (payload, ctx) -> r.handler().accept(payload),
                        Optional.of(NetworkDirection.PLAY_TO_CLIENT));
            }
            for (Serverbound<?> reg : serverbound) {
                Serverbound<SclPayload> r = (Serverbound<SclPayload>) reg;
                channel.registerMessage(id++, (Class<SclPayload>) r.type(),
                        (payload, buf) -> PayloadRegistry.write(payload, buf),
                        buf -> PayloadRegistry.read(r.type(), buf),
                        (payload, ctx) -> {
                            ServerPlayer player = ctx.get().getSender();
                            if (player == null) return;
                            r.handler().accept(payload, player);
                        },
                        Optional.of(NetworkDirection.PLAY_TO_SERVER));
            }
            return channel;
        }
        ^///?}
    }
}*///?}
