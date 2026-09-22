package io.github.bizcub.simpleConfigLib.util;

import net.minecraft.resources.Identifier;

public class Id {

    public static Identifier withDefaultNamespace(String id) {
        /*? >=1.21 {*/ return Identifier.withDefaultNamespace(id);
        /*?} else*/ //return new Identifier(id);
    }

    public static Identifier fromNamespaceAndPath(String namespace, String id) {
        /*? >=1.21 {*/ return Identifier.fromNamespaceAndPath(namespace, id);
        /*?} else*/ //return new Identifier(namespace, id);
    }

    public static Identifier parse(String id) {
        /*? >=1.21 {*/ return Identifier.parse(id);
        /*?} else*/ //return new Identifier(id);
    }
}
