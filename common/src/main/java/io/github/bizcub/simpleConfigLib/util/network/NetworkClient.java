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
        /*? fabric*/ ClientPlayNetworking.send(payload);
        /*? forge*/ //CHANNEL.send(payload, Minecraft.getInstance().getConnection().getConnection());
        /*? neoforge*/ //ClientPacketDistributor.sendToServer(payload);
    }
}
