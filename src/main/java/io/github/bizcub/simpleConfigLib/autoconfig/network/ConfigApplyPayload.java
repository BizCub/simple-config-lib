package io.github.bizcub.simpleConfigLib.autoconfig.network;

import io.github.bizcub.simpleConfigLib.main.SimpleConfigLibMain;
import io.github.bizcub.simpleConfigLib.util.Id;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ConfigApplyPayload(String name, String json) implements CustomPacketPayload {
    public static final Type<ConfigApplyPayload> TYPE = new Type<>(Id.fromNamespaceAndPath(SimpleConfigLibMain.MOD_ID, "config_apply"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ConfigApplyPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, ConfigApplyPayload::name,
                    ByteBufCodecs.STRING_UTF8, ConfigApplyPayload::json,
                    ConfigApplyPayload::new);

    @Override
    public Type<ConfigApplyPayload> type() {
        return TYPE;
    }
}
