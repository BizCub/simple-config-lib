package io.github.bizcub.simpleConfigLib.util;

import net.minecraft.resources.ResourceLocation;

public class Id {

    public static ResourceLocation withDefaultNamespace(String id) {
        /*? >=1.21 {*/ /*return ResourceLocation.withDefaultNamespace(id);
        *//*?} else*/ return new ResourceLocation(id);
    }

    public static ResourceLocation fromNamespaceAndPath(String namespace, String id) {
        /*? >=1.21 {*/ /*return ResourceLocation.fromNamespaceAndPath(namespace, id);
        *//*?} else*/ return new ResourceLocation(namespace, id);
    }
}
