package io.github.bizcub.simpleConfigLib.autoconfig.network;

import io.github.bizcub.simpleConfigLib.SimpleConfigLib;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ConfigApplyPayload(String name, String json, int requestId) implements CustomPacketPayload {
    public static final Type<ConfigApplyPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(SimpleConfigLib.MOD_ID, "config_apply"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ConfigApplyPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ConfigApplyPayload::name,
            ByteBufCodecs.STRING_UTF8, ConfigApplyPayload::json,
            ByteBufCodecs.VAR_INT, ConfigApplyPayload::requestId,
            ConfigApplyPayload::new);

    @Override
    public Type<ConfigApplyPayload> type() {
        return TYPE;
    }
}
