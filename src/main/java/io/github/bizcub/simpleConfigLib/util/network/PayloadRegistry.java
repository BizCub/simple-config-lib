package io.github.bizcub.simpleConfigLib.util.network;

import net.minecraft.resources.Identifier;

//? >=1.20.5 {
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
//?} else
//import net.minecraft.network.FriendlyByteBuf;

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PayloadRegistry {
    private static final Map<Class<?>, Identifier> ID_CACHE = new ConcurrentHashMap<>();

    //? >=1.20.5 {
    private static final Map<Class<?>, CustomPacketPayload.Type<?>> TYPE_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, StreamCodec<RegistryFriendlyByteBuf, ?>> CODEC_CACHE = new ConcurrentHashMap<>();
    //?}

    public static Identifier idOf(Class<?> type) {
        return ID_CACHE.computeIfAbsent(type, t -> {
            try {
                return (Identifier) t.getField("ID").get(null);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(
                        "Payload " + t.getName() + " must declare public static final Identifier ID", e);
            }
        });
    }

    //? >=1.20.5 {
    @SuppressWarnings("unchecked")
    public static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> typeOf(Class<T> type) {
        return (CustomPacketPayload.Type<T>) TYPE_CACHE.computeIfAbsent(
                type, key -> new CustomPacketPayload.Type<>(idOf(key)));
    }

    @SuppressWarnings("unchecked")
    public static <T> StreamCodec<RegistryFriendlyByteBuf, T> codecOf(Class<T> type) {
        return (StreamCodec<RegistryFriendlyByteBuf, T>) CODEC_CACHE.computeIfAbsent(type, t -> {
            RecordComponent[] comps = t.getRecordComponents();
            return new StreamCodec<RegistryFriendlyByteBuf, T>() {
                @Override
                public T decode(RegistryFriendlyByteBuf buf) {
                    Object[] args = new Object[comps.length];
                    for (int i = 0; i < comps.length; i++) {
                        args[i] = SclCodecs.byType(comps[i].getType()).read(buf);
                    }
                    return construct(type, comps, args);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, T value) {
                    writeComponents(comps, value, buf);
                }
            };
        });
    }

    @SuppressWarnings("unchecked")
    private static <T> void writeComponents(RecordComponent[] comps, T value, RegistryFriendlyByteBuf buf) {
        for (RecordComponent comp : comps) {
            try {
                Object field = comp.getAccessor().invoke(value);
                ((SclStreamCodec<Object>) SclCodecs.byType(comp.getType())).write(buf, field);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Serialization error " + comp.getName(), e);
            }
        }
    }

    //?} else {
    /*@SuppressWarnings("unchecked")
    public static void write(Object payload, FriendlyByteBuf buf) {
        for (RecordComponent comp : payload.getClass().getRecordComponents()) {
            try {
                Object field = comp.getAccessor().invoke(payload);
                ((SclStreamCodec<Object>) SclCodecs.byType(comp.getType())).write(buf, field);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Serialization error " + comp.getName(), e);
            }
        }
    }

    public static <T> T read(Class<T> type, FriendlyByteBuf buf) {
        RecordComponent[] comps = type.getRecordComponents();
        Object[] args = new Object[comps.length];
        for (int i = 0; i < comps.length; i++) {
            args[i] = SclCodecs.byType(comps[i].getType()).read(buf);
        }
        return construct(type, comps, args);
    }*///?}

    private static <T> T construct(Class<T> type, RecordComponent[] comps, Object[] args) {
        try {
            Class<?>[] paramTypes = new Class<?>[comps.length];
            for (int i = 0; i < comps.length; i++) paramTypes[i] = comps[i].getType();
            Constructor<T> ctor = type.getDeclaredConstructor(paramTypes);
            ctor.setAccessible(true);
            return ctor.newInstance(args);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create " + type.getName(), e);
        }
    }

    public static void validate(Class<?> type) {
        idOf(type);
        for (RecordComponent comp : type.getRecordComponents()) {
            SclCodecs.byType(comp.getType());
        }
    }
}
