//? neoforge {
/*package io.github.bizcub.simpleConfigLib.main.platform;

import io.github.bizcub.simpleConfigLib.main.SimpleConfigLibMain;
import io.github.bizcub.simpleConfigLib.autoconfig.network.ConfigApplyPayload;
import io.github.bizcub.simpleConfigLib.autoconfig.network.ConfigSyncPayload;
import io.github.bizcub.simpleConfigLib.autoconfig.ConfigHolder;
import io.github.bizcub.simpleConfigLib.util.network.PayloadRegistryNeoForge;
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
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        PayloadRegistryNeoForge.registerServerbound(
                registrar,
                ConfigApplyPayload.class,
                (SimpleConfigLibMain::handleApply));

        PayloadRegistryNeoForge.registerClientbound(
                registrar,
                ConfigSyncPayload.class,
                payload -> ConfigHolder.applyServerSnapshot(payload.name(), payload.json()));
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        SimpleConfigLibMain.onPlayerJoin(player);
    }
}*///?}
