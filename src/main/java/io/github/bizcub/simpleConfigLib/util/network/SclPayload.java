package io.github.bizcub.simpleConfigLib.util.network;

import net.minecraft.resources.Identifier;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

//? <1.20.5 {
/*import net.minecraft.network.FriendlyByteBuf;*///?}

public interface SclPayload extends CustomPacketPayload {

    //? >=1.20.5 {
    @Override
    @SuppressWarnings("unchecked")
    default CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return PayloadRegistry.typeOf(getClass());
    }

    //?} else {
    /*@Override
    default Identifier id() {
        return PayloadRegistry.idOf(getClass());
    }

    @Override
    default void write(FriendlyByteBuf buf) {
        PayloadRegistry.write(this, buf);
    }*///?}
}
