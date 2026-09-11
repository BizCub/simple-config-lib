//? forge {
/*package io.github.bizcub.simpleConfigLib.main.platform;

import io.github.bizcub.simpleConfigLib.main.SimpleConfigLibMain;
import io.github.bizcub.simpleConfigLib.util.network.Network;
import io.github.bizcub.simpleConfigLib.util.network.PayloadRegistryForge;
import io.github.bizcub.simpleConfigLib.util.network.SclPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

//? >=1.20.5 {
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraftforge.network.Channel;
//?} >=1.20.2 {
/^import net.minecraftforge.network.SimpleChannel;
^///?} else
//import net.minecraftforge.network.simple.SimpleChannel;

@Mod(SimpleConfigLibMain.MOD_ID)
@EventBusSubscriber(modid = SimpleConfigLibMain.MOD_ID)
public class ForgeMain {

    //? >=1.20.5 {
    public static final Channel<CustomPacketPayload> CHANNEL = buildChannel();
    //?} else
    //public static final SimpleChannel CHANNEL = buildChannel();

    @SuppressWarnings("unchecked")
    private static /^? >=1.20.5 {^/ Channel<CustomPacketPayload> /^?} else >> ^//^SimpleChannel^/ buildChannel() {
    SimpleConfigLibMain.registerPayloads();

    PayloadRegistryForge.Builder builder = PayloadRegistryForge.builder("config");

        for (Network.Serverbound<?> entry : Network.serverbound()) {
            Network.Serverbound<SclPayload> serverboundEntry = (Network.Serverbound<SclPayload>) entry;
            builder.serverbound(serverboundEntry.type(), serverboundEntry.handler());
        }

    for (Network.Clientbound<?> entry : Network.clientbound()) {
        Network.Clientbound<SclPayload> clientboundEntry = (Network.Clientbound<SclPayload>) entry;
        builder.clientbound(clientboundEntry.type(), clientboundEntry.handler());
    }

    return builder.build();
}

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        SimpleConfigLibMain.onPlayerJoin(player);
    }
}*///?}
