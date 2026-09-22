package io.github.bizcub.simpleConfigLib.util;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public final class BlockUtil {

    public static Block byId(Identifier id) {
        //? >=1.21.2 {
        return BuiltInRegistries.BLOCK.get(id).map(Holder.Reference::value).orElse(Blocks.AIR);
        //?} else
        //return BuiltInRegistries.BLOCK.get(id);
    }

    public static Identifier idOf(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block);
    }

    public static Block parse(String value) {
        if (value == null || value.isEmpty()) return null;
        Block block = byId(Id.withDefaultNamespace(value.trim()));
        return block == Blocks.AIR ? null : block;
    }
}
