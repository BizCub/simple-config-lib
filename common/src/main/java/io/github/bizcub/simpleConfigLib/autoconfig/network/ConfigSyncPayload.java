package io.github.bizcub.simpleConfigLib.autoconfig.network;

import io.github.bizcub.simpleConfigLib.main.SimpleConfigLibMain;
import io.github.bizcub.simpleConfigLib.util.Id;
import io.github.bizcub.simpleConfigLib.util.network.SclPayload;
import net.minecraft.resources.Identifier;

public record ConfigSyncPayload(String name, String json) implements SclPayload {
    public static final Identifier ID = Id.fromNamespaceAndPath(SimpleConfigLibMain.MOD_ID, "config_sync");
}
