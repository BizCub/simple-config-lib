package io.github.bizcub.test.network;

import io.github.bizcub.simpleConfigLib.util.Id;
import io.github.bizcub.simpleConfigLib.util.network.SclPayload;
import io.github.bizcub.test.main.TestModMain;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public record PingPayload(String message, ItemStack stack) implements SclPayload {
    public static final Identifier ID = Id.fromNamespaceAndPath(TestModMain.MOD_ID, "ping");
}
