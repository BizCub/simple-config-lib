package io.github.bizcub.simpleConfigLib.util;

import io.github.bizcub.simpleConfigLib.internal.ClientHooks;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public class Keymapper {

    private static final List<KeyMapping> PENDING = new ArrayList<>();

    public static KeyMapping register(String key, int keycode, Identifier category) {
        KeyMapping keyMapping = new KeyMapping(key, keycode, getCategory(category));
        PENDING.add(keyMapping);
        return keyMapping;
    }

    public static void onRegisterKeyMappings() {
        for (KeyMapping mapping : PENDING) {
            ClientHooks.get().registerKeyMapping(mapping);
        }
        PENDING.clear();
    }

    public static Identifier getCategoryId(String id) {
        return Id.withDefaultNamespace(id);
    }

    public static Identifier getCategoryId(String namespace, String id) {
        return Id.fromNamespaceAndPath(namespace, id);
    }

    private static KeyMapping.Category getCategory(Identifier id) {
        return KeyMapping.Category.SORT_ORDER.stream()
                .filter(c -> c.id().equals(id))
                .findFirst()
                .orElseGet(() -> KeyMapping.Category.register(id));
    }
}
