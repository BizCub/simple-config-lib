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

        private Builder(String channelPath) {
            this.channelPath = channelPath;
        }

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

            var clientboundBuilder = play.clientbound();
            for (Clientbound<?> registration : clientbound) {
                Clientbound<SclPayload> typedRegistration = (Clientbound<SclPayload>) registration;
                clientboundBuilder = clientboundBuilder.add(
                        PayloadRegistry.typeOf(typedRegistration.type()),
                        PayloadRegistry.codecOf(typedRegistration.type()),
                        (payload, ctx) -> {
                            ctx.enqueueWork(() -> typedRegistration.handler().accept(payload));
                            ctx.setPacketHandled(true);
                        });
            }

            var serverboundBuilder = clientboundBuilder.serverbound();
            for (Serverbound<?> registration : serverbound) {
                Serverbound<SclPayload> typedRegistration = (Serverbound<SclPayload>) registration;
                serverboundBuilder = serverboundBuilder.add(
                        PayloadRegistry.typeOf(typedRegistration.type()),
                        PayloadRegistry.codecOf(typedRegistration.type()),
                        (payload, ctx) -> {
                            ctx.enqueueWork(() -> {
                                if (!(ctx.getSender() instanceof ServerPlayer player)) return;
                                typedRegistration.handler().accept(payload, player);
                            });
                            ctx.setPacketHandled(true);
                        });
            }
            return serverboundBuilder.build();
        }
        //?} >=1.20.2 {
        /^@SuppressWarnings("unchecked")
        public SimpleChannel build() {
            var messageBuilder = ChannelBuilder
                    .named(Id.withDefaultNamespace(channelPath))
                    .networkProtocolVersion(1)
                    .acceptedVersions((status, ver) -> true)
                    .simpleChannel();

            for (Clientbound<?> registration : clientbound) {
                Clientbound<SclPayload> typedRegistration = (Clientbound<SclPayload>) registration;
                messageBuilder = messageBuilder.messageBuilder(typedRegistration.type(), NetworkDirection.PLAY_TO_CLIENT)
                        .encoder(PayloadRegistry::write)
                        .decoder(buffer -> PayloadRegistry.read(typedRegistration.type(), buffer))
                        .consumerMainThread((payload, ctx) -> typedRegistration.handler().accept(payload))
                        .add();
            }
            for (Serverbound<?> registration : serverbound) {
                Serverbound<SclPayload> typedRegistration = (Serverbound<SclPayload>) registration;
                messageBuilder = messageBuilder.messageBuilder(typedRegistration.type(), NetworkDirection.PLAY_TO_SERVER)
                        .encoder(PayloadRegistry::write)
                        .decoder(buffer -> PayloadRegistry.read(typedRegistration.type(), buffer))
                        .consumerMainThread((payload, ctx) -> {
                            ServerPlayer player = ctx.getSender();
                            if (player == null) return;
                            typedRegistration.handler().accept(payload, player);
                        })
                        .add();
            }
            return messageBuilder;
        }
        ^///?} else {
        /^private static final String PROTO = "1";

        @SuppressWarnings("unchecked")
        public SimpleChannel build() {
            SimpleChannel channel = NetworkRegistry.newSimpleChannel(
                    Id.withDefaultNamespace(channelPath),
                    () -> PROTO, PROTO::equals, PROTO::equals);

            int id = 0;
            for (Clientbound<?> registration : clientbound) {
                Clientbound<SclPayload> typedRegistration = (Clientbound<SclPayload>) registration;
                channel.registerMessage(id++, typedRegistration.type(),
                        PayloadRegistry::write,
                        buffer -> PayloadRegistry.read(typedRegistration.type(), buffer),
                        (payload, ctx) -> typedRegistration.handler().accept(payload),
                        Optional.of(NetworkDirection.PLAY_TO_CLIENT));
            }
            for (Serverbound<?> registration : serverbound) {
                Serverbound<SclPayload> typedRegistration = (Serverbound<SclPayload>) registration;
                channel.registerMessage(id++, typedRegistration.type(),
                        PayloadRegistry::write,
                        buffer -> PayloadRegistry.read(typedRegistration.type(), buffer),
                        (payload, ctx) -> {
                            ServerPlayer player = ctx.get().getSender();
                            if (player == null) return;
                            typedRegistration.handler().accept(payload, player);
                        },
                        Optional.of(NetworkDirection.PLAY_TO_SERVER));
            }
            return channel;
        }
        ^///?}
    }
}*///?}
