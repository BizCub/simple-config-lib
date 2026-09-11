package io.github.bizcub.simpleConfigLib.util.network;

import io.netty.buffer.Unpooled;
import net.minecraft.resources.Identifier;

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//? >=1.20.5 {
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
//?} else
//import net.minecraft.network.FriendlyByteBuf;

public final class PayloadRegistry {
    private static final Map<Class<?>, Identifier> ID_CACHE = new ConcurrentHashMap<>();

    //? >=1.20.5 {
    private static final Map<Class<?>, CustomPacketPayload.Type<?>> TYPE_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, StreamCodec<RegistryFriendlyByteBuf, ?>> CODEC_CACHE = new ConcurrentHashMap<>();
    //?}

    public static Identifier idOf(Class<?> type) {
        return ID_CACHE.computeIfAbsent(type, payloadClass -> {
            try {
                return (Identifier) payloadClass.getField("ID").get(null);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(
                        "Payload " + payloadClass.getName() + " must declare public static final Identifier ID", e);
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
        return (StreamCodec<RegistryFriendlyByteBuf, T>) CODEC_CACHE.computeIfAbsent(type, payloadClass -> {
            RecordComponent[] components = payloadClass.getRecordComponents();
            return new StreamCodec<RegistryFriendlyByteBuf, T>() {
                @Override
                public T decode(RegistryFriendlyByteBuf buffer) {
                    Object[] args = new Object[components.length];
                    for (int i = 0; i < components.length; i++) {
                        args[i] = SclCodecs.byType(components[i].getType()).read(buffer);
                    }
                    return construct(type, components, args);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, T value) {
                    writeComponents(components, value, buffer);
                }
            };
        });
    }

    @SuppressWarnings("unchecked")
    private static <T> void writeComponents(RecordComponent[] components, T value, RegistryFriendlyByteBuf buffer) {
        for (RecordComponent component : components) {
            try {
                Object field = component.getAccessor().invoke(value);
                ((SclStreamCodec<Object>) SclCodecs.byType(component.getType())).write(buffer, field);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Serialization error " + component.getName(), e);
            }
        }
    }

    //?} else {
    /*@SuppressWarnings("unchecked")
    public static void write(Object payload, FriendlyByteBuf buffer) {
        for (RecordComponent component : payload.getClass().getRecordComponents()) {
            try {
                Object field = component.getAccessor().invoke(payload);
                ((SclStreamCodec<Object>) SclCodecs.byType(component.getType())).write(buffer, field);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Serialization error " + component.getName(), e);
            }
        }
    }

    public static <T> T read(Class<T> type, FriendlyByteBuf buffer) {
        RecordComponent[] components = type.getRecordComponents();
        Object[] args = new Object[components.length];
        for (int i = 0; i < components.length; i++) {
            args[i] = SclCodecs.byType(components[i].getType()).read(buffer);
        }
        return construct(type, components, args);
    }

    public static FriendlyByteBuf toBuffer(Object payload) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        write(payload, buffer);
        return buffer;
    }*///?}

    private static <T> T construct(Class<T> type, RecordComponent[] components, Object[] args) {
        try {
            Class<?>[] paramTypes = new Class<?>[components.length];
            for (int i = 0; i < components.length; i++) paramTypes[i] = components[i].getType();
            Constructor<T> constructor = type.getDeclaredConstructor(paramTypes);
            constructor.setAccessible(true);
            return constructor.newInstance(args);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create " + type.getName(), e);
        }
    }

    public static void validate(Class<?> type) {
        idOf(type);
        for (RecordComponent component : type.getRecordComponents()) {
            SclCodecs.byType(component.getType());
        }
    }
}
