package io.github.bizcub.simpleConfigLib.util.network;

import net.minecraft.client.Minecraft;

//? fabric
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
//? forge
//import static io.github.bizcub.simpleConfigLib.main.platform.ForgeMain.CHANNEL;
//? neoforge {
/*/^? >=1.21.7^/ import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;*///?}

public class NetworkClient {

    public static void sendToServer(SclPayload payload) {
        /*? fabric*/ ClientPlayNetworking.send(/*? <1.20.5 {*/ /*PayloadRegistry.idOf(payload.getClass()), PayloadRegistry.toBuffer(payload) *//*?} else >> ')'*/ payload);
        /*? forge && >=1.20.2*/ //CHANNEL.send(payload, Minecraft.getInstance().getConnection().getConnection());
        /*? forge && 1.20.1*/ //CHANNEL.sendToServer(payload);
        //~ if >=1.21.7 'PacketDistributor' -> 'ClientPacketDistributor'
        /*? neoforge*/ //ClientPacketDistributor.sendToServer(payload);
    }
}
