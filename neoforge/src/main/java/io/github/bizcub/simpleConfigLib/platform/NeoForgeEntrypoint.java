package io.github.bizcub.simpleConfigLib.platform;

import io.github.bizcub.simpleConfigLib.SimpleConfigLib;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@Mod(SimpleConfigLib.MOD_ID)
@EventBusSubscriber(modid = SimpleConfigLib.MOD_ID)
public class NeoForgeEntrypoint {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        NeoForgeHooks.registerPayloads(event.registrar("1"));
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
