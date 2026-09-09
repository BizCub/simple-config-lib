package io.github.bizcub.simpleConfigLib.autoconfig.network;

import io.github.bizcub.simpleConfigLib.main.SimpleConfigLibMain;
import io.github.bizcub.simpleConfigLib.util.Id;
import net.minecraft.resources.Identifier;

//? >=1.20.5 {
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
//?} else {
/*import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;*///?}

public record ConfigSyncPayload(String name, String json) /*? >=1.20.5 >> ' {'*/ implements CustomPacketPayload {
    public static final Identifier ID = Id.fromNamespaceAndPath(SimpleConfigLibMain.MOD_ID, "config_apply");

    //? >=1.20.5 {
    public static final Type<ConfigSyncPayload> TYPE = new Type<>(Id.fromNamespaceAndPath(SimpleConfigLibMain.MOD_ID, "config_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ConfigSyncPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, ConfigSyncPayload::name,
                    ByteBufCodecs.STRING_UTF8, ConfigSyncPayload::json,
                    ConfigSyncPayload::new);

    @Override
    public Type<ConfigSyncPayload> type() {
        return TYPE;
    }

    //?} else {
    /*public static final Codec<ConfigSyncPayload> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(ConfigSyncPayload::name),
            Codec.STRING.fieldOf("json").forGetter(ConfigSyncPayload::json)
    ).apply(instance, ConfigSyncPayload::new));

    public static ConfigSyncPayload read(FriendlyByteBuf buf) {
        return new ConfigSyncPayload(buf.readUtf(), buf.readUtf());
    }

    public FriendlyByteBuf toBuffer() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeUtf(name);
        buf.writeUtf(json);
        return buf;
    }*///?}
}
