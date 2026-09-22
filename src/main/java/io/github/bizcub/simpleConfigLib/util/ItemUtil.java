package io.github.bizcub.simpleConfigLib.util;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ItemUtil {

    public static Item byId(Identifier id) {
        //? >=1.21.2 {
        return BuiltInRegistries.ITEM.get(id).map(Holder.Reference::value).orElse(Items.AIR);
        //?} else
        //return BuiltInRegistries.ITEM.get(id);
    }

    public static Identifier idOf(Item item) {
        return BuiltInRegistries.ITEM.getKey(item);
    }

    public static Identifier idOf(ItemStack stack) {
        return idOf(stack.getItem());
    }

    public static ItemStack parse(String value) {
        if (value == null || value.isEmpty()) return ItemStack.EMPTY;

        String idPart = value;
        int count = 1;
        int star = value.indexOf('*');
        if (star >= 0) {
            idPart = value.substring(0, star).trim();
            try {
                count = Integer.parseInt(value.substring(star + 1).trim());
            } catch (NumberFormatException ignored) {
            }
        }

        Item item = byId(Id.withDefaultNamespace(idPart.trim()));
        if (item == Items.AIR) return ItemStack.EMPTY;
        return new ItemStack(item, Math.max(1, count));
    }
}
