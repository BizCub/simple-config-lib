package io.github.bizcub.simpleConfigLib.util.network;

//? >=1.20.5 {
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface SclPayload extends CustomPacketPayload {

    @Override
    @SuppressWarnings("unchecked")
    default CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return PayloadRegistry.typeOf(getClass());
    }
}
//?} else {
/*public interface SclPayload {
}*///?}
