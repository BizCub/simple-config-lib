package io.github.bizcub.simpleConfigLib.autoconfig.network;

import io.github.bizcub.simpleConfigLib.SimpleConfigLib;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ConfigSyncPayload(String name, String json) implements CustomPacketPayload {
    public static final Type<ConfigSyncPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(SimpleConfigLib.MOD_ID, "config_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ConfigSyncPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ConfigSyncPayload::name,
            ByteBufCodecs.STRING_UTF8, ConfigSyncPayload::json,
            ConfigSyncPayload::new);

    @Override
    public Type<ConfigSyncPayload> type() {
        return TYPE;
    }
}
