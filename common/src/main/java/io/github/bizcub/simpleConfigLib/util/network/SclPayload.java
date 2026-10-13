package io.github.bizcub.simpleConfigLib.util.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface SclPayload extends CustomPacketPayload {

    @Override
    default CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return PayloadRegistry.typeOf(getClass());
    }
}
