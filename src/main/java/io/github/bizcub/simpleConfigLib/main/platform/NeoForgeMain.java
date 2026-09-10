//? neoforge {
/*package io.github.bizcub.simpleConfigLib.main.platform;

import io.github.bizcub.simpleConfigLib.main.SimpleConfigLibMain;
import io.github.bizcub.simpleConfigLib.util.network.Network;
import io.github.bizcub.simpleConfigLib.util.network.PayloadRegistryNeoForge;
import io.github.bizcub.simpleConfigLib.util.network.SclPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(SimpleConfigLibMain.MOD_ID)
@EventBusSubscriber(modid = SimpleConfigLibMain.MOD_ID)
public class NeoForgeMain {

    @SubscribeEvent
    @SuppressWarnings("unchecked")
    public static void register(RegisterPayloadHandlersEvent event) {
        SimpleConfigLibMain.registerPayloads();

        PayloadRegistrar registrar = event.registrar("1");

        for (Network.Serverbound<?> entry : Network.serverbound()) {
            Network.Serverbound<SclPayload> serverboundEntry = (Network.Serverbound<SclPayload>) entry;
            PayloadRegistryNeoForge.registerServerbound(registrar, serverboundEntry.type(), serverboundEntry.handler());
        }

        for (Network.Clientbound<?> entry : Network.clientbound()) {
            Network.Clientbound<SclPayload> clientboundEntry = (Network.Clientbound<SclPayload>) entry;
            PayloadRegistryNeoForge.registerClientbound(registrar, clientboundEntry.type(), clientboundEntry.handler());
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        SimpleConfigLibMain.onPlayerJoin(player);
    }
}*///?}
