package io.github.bizcub.simpleConfigLib.util;

/*? fabric*/ import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
/*? forge*/ //import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
/*? neoforge*/ //import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class Keymapper {
    private static final List<KeyMapping> PENDING = new ArrayList<>();

    public static KeyMapping register(String key, int keycode, ResourceLocation category) {
        KeyMapping keyMapping = new KeyMapping(key, keycode, getCategory(category));
        PENDING.add(keyMapping);

        return keyMapping;
    }

    public static void onRegisterKeyMappings(/*? forge || neoforge >> ')'*/ /*RegisterKeyMappingsEvent event*/) {
        for (KeyMapping mapping : PENDING) {
            //? fabric {
            KeyBindingHelper.registerKeyBinding(mapping);

            //?} forge || neoforge {
            /*event.register(mapping);*///?}
        }

        PENDING.clear();
    }

    public static ResourceLocation getCategoryId(String id) {
        return Id.withDefaultNamespace(id);
    }

    public static ResourceLocation getCategoryId(String namespace, String id) {
        return Id.fromNamespaceAndPath(namespace, id);
    }

    //? >=1.21.9 {
    /*private static KeyMapping.Category getCategory(ResourceLocation id) {
        return KeyMapping.Category.SORT_ORDER.stream()
                .filter(c -> c.id().equals(id))
                .findFirst()
                .orElseGet(() -> KeyMapping.Category.register(id));
    }

    *///?} else {
    private static String getCategory(ResourceLocation id) {
        return "key.categories." + id.getPath();
    }//?}
}
