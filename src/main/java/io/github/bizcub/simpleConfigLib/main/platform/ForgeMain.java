//? forge {
/*package io.github.bizcub.simpleConfigLib.main.platform;

import io.github.bizcub.simpleConfigLib.main.SimpleConfigLibMain;
import io.github.bizcub.simpleConfigLib.autoconfig.network.ConfigApplyPayload;
import io.github.bizcub.simpleConfigLib.autoconfig.network.ConfigSyncPayload;
import io.github.bizcub.simpleConfigLib.autoconfig.ConfigHolder;
import io.github.bizcub.simpleConfigLib.util.network.PayloadRegistryForge;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

//? >=1.20.5 {
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraftforge.network.Channel;
//?} else {
/^import net.minecraftforge.network.SimpleChannel;
^///?}

@Mod(SimpleConfigLibMain.MOD_ID)
@EventBusSubscriber(modid = SimpleConfigLibMain.MOD_ID)
public class ForgeMain {

    //? >=1.20.5 {
    public static final Channel<CustomPacketPayload> CHANNEL =
    //?} else {
    /^public static final SimpleChannel CHANNEL =
    ^///?}
            PayloadRegistryForge.builder("config")
                    .clientbound(ConfigSyncPayload.class,
                            payload -> ConfigHolder.applyServerSnapshot(payload.name(), payload.json()))
                    .serverbound(ConfigApplyPayload.class,
                            SimpleConfigLibMain::handleApply)
                    .build();

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        SimpleConfigLibMain.onPlayerJoin(player);
    }
}*///?}
