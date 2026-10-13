package io.github.bizcub.simpleConfigLib.platform;

import io.github.bizcub.simpleConfigLib.SimpleConfigLib;
import io.github.bizcub.simpleConfigLib.platform.client.NeoForgeClientEntrypoint;
import io.github.bizcub.simpleConfigLib.util.network.Network;
import io.github.bizcub.simpleConfigLib.util.network.SclPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(SimpleConfigLib.MOD_ID)
@EventBusSubscriber(modid = SimpleConfigLib.MOD_ID)
public class NeoForgeEntrypoint {

    public NeoForgeEntrypoint() {
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            NeoForgeClientEntrypoint.init();
        }
    }

    @SubscribeEvent
    @SuppressWarnings("unchecked")
    public static void register(RegisterPayloadHandlersEvent event) {
        SimpleConfigLib.registerPayloads();

        PayloadRegistrar registrar = event.registrar("1");

        for (Network.Serverbound<?> entry : Network.serverbound()) {
            Network.Serverbound<SclPayload> typed = (Network.Serverbound<SclPayload>) entry;
            NeoForgeHooks.registerServerbound(registrar, typed.type(), typed.handler());
        }

        for (Network.Clientbound<?> entry : Network.clientbound()) {
            Network.Clientbound<SclPayload> typed = (Network.Clientbound<SclPayload>) entry;
            NeoForgeHooks.registerClientbound(registrar, typed.type(), typed.handler());
        }

        NeoForgeHooks.drainClientbound(registrar);
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        SimpleConfigLib.onPlayerJoin(player);
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(SimpleConfigLib.buildReloadCommand());
    }
}
