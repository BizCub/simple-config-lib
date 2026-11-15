package io.github.bizcub.simpleConfigLib.autoconfig.network;

import io.github.bizcub.simpleConfigLib.SimpleConfigLib;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ConfigResultPayload(String name, int requestId, boolean success) implements CustomPacketPayload {
    public static final Type<ConfigResultPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(SimpleConfigLib.MOD_ID, "config_result"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ConfigResultPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ConfigResultPayload::name,
            ByteBufCodecs.VAR_INT, ConfigResultPayload::requestId,
            ByteBufCodecs.BOOL, ConfigResultPayload::success,
            ConfigResultPayload::new);

    @Override
    public Type<ConfigResultPayload> type() {
        return TYPE;
    }
}
