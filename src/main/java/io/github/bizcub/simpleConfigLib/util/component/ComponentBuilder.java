package io.github.bizcub.simpleConfigLib.util.component;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;

public final class ComponentBuilder {

    private final MutableComponent component;

    private ComponentBuilder(MutableComponent component) {
        this.component = component;
    }

    public static ComponentBuilder literal(String text) {
        return new ComponentBuilder(
                //? >=1.19 {
                Component.literal(text)
                //?} else
                //new TextComponent(text)
        );
    }

    public static ComponentBuilder translatable(String text, Object... args) {
        return new ComponentBuilder(
                //? >=1.19 {
                Component.translatable(text, args)
                //?} else
                //new TranslatableComponent(text, args)
        );
    }

    public static ComponentBuilder empty() {
        return new ComponentBuilder(
                //? >=1.19 {
                Component.empty()
                //?} else
                //new TextComponent("")
        );
    }

    public ComponentBuilder bold() {
        component.withStyle(s -> s.withBold(true));
        return this;
    }

    public ComponentBuilder italic() {
        component.withStyle(s -> s.withItalic(true));
        return this;
    }

    public ComponentBuilder underline() {
        component.withStyle(s -> s.withUnderlined(true));
        return this;
    }

    public ComponentBuilder strikethrough() {
        component.withStyle(s -> s.applyFormat(ChatFormatting.STRIKETHROUGH));
        return this;
    }

    public ComponentBuilder obfuscated() {
        component.withStyle(s -> s.applyFormat(ChatFormatting.OBFUSCATED));
        return this;
    }

    public ComponentBuilder color(int rgb) {
        component.withStyle(s -> s.withColor(TextColor.fromRgb(rgb)));
        return this;
    }

    public ComponentBuilder color(ChatFormatting color) {
        component.withStyle(color);
        return this;
    }

    public ComponentBuilder format(ChatFormatting... formats) {
        component.withStyle(formats);
        return this;
    }

    public ComponentBuilder clickEvent(ClickEvent event) {
        component.withStyle(s -> s.withClickEvent(event));
        return this;
    }

    public ComponentBuilder hoverEvent(HoverEvent event) {
        component.withStyle(s -> s.withHoverEvent(event));
        return this;
    }

    public ComponentBuilder insertion(String insertion) {
        component.withStyle(s -> s.withInsertion(insertion));
        return this;
    }

    public Component build() {
        return component;
    }
}
