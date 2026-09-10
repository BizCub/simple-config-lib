//? neoforge {
/*package io.github.bizcub.simpleConfigLib.main.platform;

import io.github.bizcub.simpleConfigLib.main.SimpleConfigLibMain;
import io.github.bizcub.simpleConfigLib.autoconfig.network.ConfigApplyPayload;
import io.github.bizcub.simpleConfigLib.autoconfig.network.ConfigSyncPayload;
import io.github.bizcub.simpleConfigLib.autoconfig.ConfigHolder;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@Mod(SimpleConfigLibMain.MOD_ID)
@EventBusSubscriber(modid = SimpleConfigLibMain.MOD_ID)
public class NeoForgeMain {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(
                ConfigApplyPayload.TYPE,
                ConfigApplyPayload.CODEC,
                NeoForgeMain::handleApply);

        registrar.playToClient(
                ConfigSyncPayload.TYPE,
                ConfigSyncPayload.CODEC,
                NeoForgeMain::handleSync);
    }

    private static void handleApply(ConfigApplyPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            SimpleConfigLibMain.handleApply(player, payload);
        });
    }

    private static void handleSync(ConfigSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ConfigHolder.applyServerSnapshot(payload.name(), payload.json()));
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        SimpleConfigLibMain.onPlayerJoin(player);
    }
}*///?}
