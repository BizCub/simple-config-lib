package io.github.bizcub.simpleConfigLib.autoconfig;

import io.github.bizcub.simpleConfigLib.Main;
import io.github.bizcub.simpleConfigLib.util.Id;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ConfigApplyPayload(String name, String json) implements CustomPacketPayload {
    public static final Type<ConfigApplyPayload> TYPE = new Type<>(Id.fromNamespaceAndPath(Main.MOD_ID, "config_apply"));

    public static final StreamCodec<FriendlyByteBuf, ConfigApplyPayload> CODEC =
            StreamCodec.of(
                    (buf, payload) -> {
                        buf.writeUtf(payload.name);
                        buf.writeUtf(payload.json);
                    },
                    buf -> new ConfigApplyPayload(buf.readUtf(), buf.readUtf())
            );

    @Override
    public Type<ConfigApplyPayload> type() {
        return TYPE;
    }
}
