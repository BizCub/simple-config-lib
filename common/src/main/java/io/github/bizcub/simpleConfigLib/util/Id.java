package io.github.bizcub.simpleConfigLib.util;

import net.minecraft.resources.Identifier;

public class Id {

    public static Identifier withDefaultNamespace(String id) {
        return Identifier.withDefaultNamespace(id);
    }

    public static Identifier fromNamespaceAndPath(String namespace, String id) {
        return Identifier.fromNamespaceAndPath(namespace, id);
    }

    public static Identifier parse(String id) {
        return Identifier.parse(id);
    }
}
