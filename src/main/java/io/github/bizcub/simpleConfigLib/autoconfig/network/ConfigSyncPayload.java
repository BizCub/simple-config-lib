package io.github.bizcub.simpleConfigLib.autoconfig.network;

import io.github.bizcub.simpleConfigLib.main.SimpleConfigLibMain;
import io.github.bizcub.simpleConfigLib.util.Id;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ConfigSyncPayload(String name, String json) implements CustomPacketPayload {
    public static final Type<ConfigSyncPayload> TYPE = new Type<>(Id.fromNamespaceAndPath(SimpleConfigLibMain.MOD_ID, "config_sync"));

    public static final StreamCodec<FriendlyByteBuf, ConfigSyncPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, ConfigSyncPayload::name,
                    ByteBufCodecs.STRING_UTF8, ConfigSyncPayload::json,
                    ConfigSyncPayload::new);

    @Override
    public Type<ConfigSyncPayload> type() {
        return TYPE;
    }
}
