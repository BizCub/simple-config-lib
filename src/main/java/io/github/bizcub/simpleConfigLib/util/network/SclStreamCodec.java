package io.github.bizcub.simpleConfigLib.util.network;

//? >=1.20.5 {
import net.minecraft.network.RegistryFriendlyByteBuf;
//?} else
//import net.minecraft.network.FriendlyByteBuf;

import java.util.function.BiConsumer;
import java.util.function.Function;

public final class SclStreamCodec<T> {

    //? >=1.20.5 {
    public interface Writer<T> extends BiConsumer<RegistryFriendlyByteBuf, T> {}
    public interface Reader<T> extends Function<RegistryFriendlyByteBuf, T> {}
    //?} else {
    /*public interface Writer<T> extends BiConsumer<FriendlyByteBuf, T> {}
    public interface Reader<T> extends Function<FriendlyByteBuf, T> {}*///?}

    private final Writer<T> writer;
    private final Reader<T> reader;

    private SclStreamCodec(Writer<T> writer, Reader<T> reader) {
        this.writer = writer;
        this.reader = reader;
    }

    public static <T> SclStreamCodec<T> of(Writer<T> writer, Reader<T> reader) {
        return new SclStreamCodec<>(writer, reader);
    }

    //? >=1.20.5 {
    public void write(RegistryFriendlyByteBuf buf, T value) {
        writer.accept(buf, value);
    }
    public T read(RegistryFriendlyByteBuf buf) {
        return reader.apply(buf);
    }

    //?} else {
    /*public void write(FriendlyByteBuf buf, T value) {
    writer.accept(buf, value);
    }
    public T read(FriendlyByteBuf buf) {
    return reader.apply(buf);
    }*///?}
}
