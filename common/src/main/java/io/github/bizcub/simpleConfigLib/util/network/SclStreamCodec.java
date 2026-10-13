package io.github.bizcub.simpleConfigLib.util.network;

import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.function.BiConsumer;
import java.util.function.Function;

public final class SclStreamCodec<T> {

    public interface Writer<T> extends BiConsumer<RegistryFriendlyByteBuf, T> {
    }

    public interface Reader<T> extends Function<RegistryFriendlyByteBuf, T> {
    }

    private final Writer<T> writer;
    private final Reader<T> reader;

    private SclStreamCodec(Writer<T> writer, Reader<T> reader) {
        this.writer = writer;
        this.reader = reader;
    }

    public static <T> SclStreamCodec<T> of(Writer<T> writer, Reader<T> reader) {
        return new SclStreamCodec<>(writer, reader);
    }

    public void write(RegistryFriendlyByteBuf buf, T value) {
        writer.accept(buf, value);
    }

    public T read(RegistryFriendlyByteBuf buf) {
        return reader.apply(buf);
    }
}
