package io.github.bizcub.lib.util.component;

//~ hover_event
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
//~ if >=26.1 'ItemStack' -> 'ItemStackTemplate'
import net.minecraft.world.item.ItemStackTemplate;

import java.util.Optional;
import java.util.UUID;

public final class HoverEventBuilder {

    private HoverEvent event;

    private HoverEventBuilder() {}

    public static HoverEventBuilder create() {
        return new HoverEventBuilder();
    }

    public HoverEventBuilder showText(Component value) {
        this.event = new HoverEvent.ShowText(value);
        return this;
    }

    public HoverEventBuilder showText(String text) {
        return showText(ComponentBuilder.literal(text).build());
    }

    public HoverEventBuilder showItem(Item item) {
        //? >=26.1 {
        this.event = new HoverEvent.ShowItem(new ItemStackTemplate(item));
        //?} >=1.21.5 {
        /*this.event = new HoverEvent.ShowItem(new ItemStack(item));
         *///?} else
        //this.event = new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(new ItemStack(item)));
        return this;
    }

    public HoverEventBuilder showEntity(EntityType<?> type, UUID uuid, Component name) {
        //~ if >=1.21.1 'name' -> 'Optional.ofNullable(name)'
        this.event = new HoverEvent.ShowEntity(new HoverEvent.EntityTooltipInfo(type, uuid, Optional.ofNullable(name)));
        return this;
    }

    public HoverEventBuilder showEntity(EntityType<?> type, UUID uuid) {
        return showEntity(type, uuid, null);
    }

    public HoverEvent build() {
        if (this.event == null) {
            throw new IllegalStateException("HoverEvent action not set");
        }
        return this.event;
    }
}
