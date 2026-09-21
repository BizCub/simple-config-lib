//? forge {
/*package io.github.bizcub.simpleConfigLib.main.platform;

import io.github.bizcub.simpleConfigLib.main.SimpleConfigLibMain;
import io.github.bizcub.simpleConfigLib.util.network.Network;
import io.github.bizcub.simpleConfigLib.util.network.PayloadRegistryForge;
import io.github.bizcub.simpleConfigLib.util.network.SclPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

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
    public static Channel<CustomPacketPayload> CHANNEL;
     //?} else
    //public static SimpleChannel CHANNEL;

    public ForgeMain(FMLJavaModLoadingContext context) {
        //? >=1.21.6 {
        FMLCommonSetupEvent.getBus(context.getModBusGroup()).addListener(ForgeMain::onCommonSetup);
        //?} else
        //FMLJavaModLoadingContext.get().getModEventBus().addListener(ForgeMain::onCommonSetup);
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ForgeMain::buildChannel);
    }

    @SuppressWarnings("unchecked")
    private static void buildChannel() {
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

        CHANNEL = builder.build();
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        SimpleConfigLibMain.onPlayerJoin(player);
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(SimpleConfigLibMain.buildReloadCommand());
    }
}*///?}
