package io.github.bizcub.simpleConfigLib.util.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public final class SclCodecs {

    private static final Map<Class<?>, SclStreamCodec<?>> BY_TYPE = new HashMap<>();

    public static <T> void register(Class<T> type, SclStreamCodec<T> codec) {
        BY_TYPE.put(type, codec);
    }

    @SuppressWarnings("unchecked")
    public static <T> SclStreamCodec<T> byType(Class<T> type) {
        SclStreamCodec<T> codec = (SclStreamCodec<T>) BY_TYPE.get(type);
        if (codec == null) {
            throw new IllegalArgumentException("No SclStreamCodec registered for type " + type.getName());
        }
        return codec;
    }

    static {
        register(String.class, SclStreamCodec.of(FriendlyByteBuf::writeUtf, FriendlyByteBuf::readUtf));
        register(Integer.class, SclStreamCodec.of(FriendlyByteBuf::writeVarInt, FriendlyByteBuf::readVarInt));
        register(Boolean.class, SclStreamCodec.of(FriendlyByteBuf::writeBoolean, FriendlyByteBuf::readBoolean));
        register(ItemStack.class,
                //? >=1.20.5 {
                SclStreamCodec.of(ItemStack.OPTIONAL_STREAM_CODEC::encode, ItemStack.OPTIONAL_STREAM_CODEC::decode)
                //?} else
                //SclStreamCodec.of((buffer, value) -> buffer.writeItem(value), buffer -> buffer.readItem())
        );
    }
}
