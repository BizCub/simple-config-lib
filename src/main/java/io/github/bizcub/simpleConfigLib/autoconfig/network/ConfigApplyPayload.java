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

public record ConfigApplyPayload(String name, String json) /*? >=1.20.5 >> ' {'*/ implements CustomPacketPayload {
    public static final Identifier ID = Id.fromNamespaceAndPath(SimpleConfigLibMain.MOD_ID, "config_apply");

    //? >=1.20.5 {
    public static final Type<ConfigApplyPayload> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, ConfigApplyPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, ConfigApplyPayload::name,
                    ByteBufCodecs.STRING_UTF8, ConfigApplyPayload::json,
                    ConfigApplyPayload::new);

    @Override
    public Type<ConfigApplyPayload> type() {
        return TYPE;
    }

    //?} else {
    /*public static final Codec<ConfigApplyPayload> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(ConfigApplyPayload::name),
            Codec.STRING.fieldOf("json").forGetter(ConfigApplyPayload::json)
    ).apply(instance, ConfigApplyPayload::new));

    public static ConfigApplyPayload read(FriendlyByteBuf buf) {
        return new ConfigApplyPayload(buf.readUtf(), buf.readUtf());
    }

    public FriendlyByteBuf toBuffer() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeUtf(name);
        buf.writeUtf(json);
        return buf;
    }*///?}
}
