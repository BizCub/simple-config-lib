package io.github.bizcub.simpleConfigLib.autoconfig.gui;

import io.github.bizcub.simpleConfigLib.internal.GuiSounds;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

final class ItemPicker extends AbstractWidget {
    private static final int ROW_HEIGHT = 20;
    private static final int HEADER = 26;
    private static final int BOTTOM_PADDING = 2;
    private static final int MAX_VISIBLE_ROWS = 8;
    private final Font font;
    private final ItemWidget owner;
    private final Runnable close;
    private final EditBox search;
    private final List<Entry> entries;
    private List<Entry> filtered;
    private int selected;
    private int offset;
    private boolean scrolling;

    private record Entry(String id, ItemStack stack, Component name, String searchText) {}

    ItemPicker(Font font, ItemWidget owner, Runnable close, int screenWidth, int screenHeight) {
        super(0, 0, 0, 0, Component.empty());
        this.font = font;
        this.owner = owner;
        this.close = close;
        this.entries = BuiltInRegistries.ITEM.stream().map(item -> {
            String id = BuiltInRegistries.ITEM.getKey(item).toString();
            ItemStack stack = ItemWidget.previewStack(item);
            Component name = ItemWidget.previewName(item, stack);
            return new Entry(id, stack, name, (name.getString() + " " + id).toLowerCase(Locale.ROOT));
        }).sorted(Comparator.comparing(Entry::searchText)).toList();
        this.search = new EditBox(font, 0, 0, 1, 20,
                Component.translatable("text.simple_config_lib.item.search"));
        this.search.setHint(Component.translatable("gui.recipebook.search_hint"));
        this.search.setMaxLength(256);
        this.search.setResponder(this::filter);
        this.search.setFocused(true);
        filter("");
        updateLayout(screenWidth, screenHeight);
        Identifier currentId = Identifier.tryParse(owner.value());
        for (int i = 0; currentId != null && i < this.filtered.size(); i++) {
            if (this.filtered.get(i).id().equals(currentId.toString())) { this.selected = i; break; }
        }
        revealSelected();
    }

    void updateLayout(int screenWidth, int screenHeight) {
        int width = Math.max(1, Math.min(this.owner.getWidth(), screenWidth - 4));
        int fixedHeight = HEADER + BOTTOM_PADDING;
        int preferredHeight = fixedHeight + ROW_HEIGHT * MAX_VISIBLE_ROWS;
        int below = this.owner.getBottom() + 2;
        int above = this.owner.getY() - 2;
        int spaceBelow = screenHeight - 2 - below;
        int spaceAbove = above - 2;
        boolean openBelow = spaceBelow >= preferredHeight || spaceBelow >= spaceAbove;
        int availableHeight = openBelow ? spaceBelow : spaceAbove;
        int maxRows = Math.clamp(this.filtered.size(), 1, MAX_VISIBLE_ROWS);
        int rows = Math.clamp((availableHeight - fixedHeight) / ROW_HEIGHT, 1, maxRows);
        int height = Math.max(1, Math.min(fixedHeight + ROW_HEIGHT * rows, screenHeight - 4));
        int x = Math.max(2, Math.min(this.owner.getX(), screenWidth - width - 2));
        int y = Math.max(2, Math.min(openBelow ? below : above - height, screenHeight - height - 2));
        if (x == getX() && y == getY() && width == getWidth() && height == getHeight()) return;

        boolean selectedVisible = this.selected >= this.offset && this.selected < this.offset + visibleRows();
        setPosition(x, y);
        setWidth(width);
        setHeight(height);
        this.search.setPosition(x + 4, y + 3);
        this.search.setWidth(Math.max(1, width - 8));
        this.offset = Math.min(this.offset, Math.max(0, this.filtered.size() - visibleRows()));
        if (selectedVisible && visibleRows() > 0) revealSelected();
    }

    private int visibleRows() { return Math.max(0, (getHeight() - HEADER - BOTTOM_PADDING) / ROW_HEIGHT); }

    private void filter(String query) {
        String text = query.trim().toLowerCase(Locale.ROOT);
        this.filtered = this.entries.stream().filter(entry -> entry.searchText().contains(text)).toList();
        this.selected = this.filtered.isEmpty() ? -1 : 0;
        this.offset = 0;
    }

    private void revealSelected() {
        int rows = visibleRows();
        if (this.selected < this.offset) this.offset = Math.max(0, this.selected);
        else if (this.selected >= this.offset + rows) this.offset = Math.max(0, this.selected - rows + 1);
    }

    private int hovered(double x, double y) {
        if (x < getX() + 2 || x >= getRight() - 6 || y < getY() + HEADER) return -1;
        int row = (int) (y - getY() - HEADER) / ROW_HEIGHT;
        int index = this.offset + row;
        return row < visibleRows() && index < this.filtered.size() ? index : -1;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.nextStratum();
        graphics.fill(getX() - 1, getY() - 1, getRight() + 1, getBottom() + 1, 0xFF000000);
        graphics.fill(getX(), getY(), getRight(), getBottom(), 0xFF202020);
        this.search.extractRenderState(graphics, mouseX, mouseY, partialTick);
        int hovered = hovered(mouseX, mouseY);
        for (int row = 0; row < visibleRows() && this.offset + row < this.filtered.size(); row++) {
            int index = this.offset + row;
            Entry entry = this.filtered.get(index);
            int y = getY() + HEADER + row * ROW_HEIGHT;
            if (index == hovered || (hovered < 0 && index == this.selected)) {
                graphics.fill(getX() + 2, y, getRight() - 6, y + ROW_HEIGHT, 0xFF404040);
            }
            ItemWidget.renderIcon(graphics, this.font, entry.stack(), getX() + 4, y + 2);
            graphics.text(this.font, this.font.plainSubstrByWidth(entry.name().getString(), Math.max(0, getWidth() - 32)),
                    getX() + 24, y + 6, 0xFFFFFFFF);
        }
        if (this.filtered.isEmpty()) graphics.text(this.font, Component.translatable("text.simple_config_lib.item.empty"),
                getX() + 6, getY() + HEADER + 6, 0xFFAAAAAA);
        int rows = visibleRows();
        if (rows > 0 && this.filtered.size() > rows) {
            int track = rows * ROW_HEIGHT;
            int thumb = Math.max(8, track * rows / this.filtered.size());
            int y = getY() + HEADER + (track - thumb) * this.offset / (this.filtered.size() - rows);
            graphics.fill(getRight() - 4, y, getRight() - 2, y + thumb, 0xFF808080);
        }
        if (hovered >= 0) graphics.setTooltipForNextFrame(Component.literal(this.filtered.get(hovered).id()), mouseX, mouseY);
    }

    private void select(int index) {
        if (index < 0 || index >= this.filtered.size()) return;
        this.owner.select(this.filtered.get(index).id());
        GuiSounds.playClick();
        this.close.run();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (!isMouseOver(event.x(), event.y())) { this.close.run(); return true; }
        if (event.button() == 1) {
            if (event.x() >= getRight() - 6 && event.y() >= getY() + HEADER && this.filtered.size() > visibleRows()) {
                this.scrolling = true;
                scrollTo(event.y());
            } else if (this.search.isMouseOver(event.x(), event.y())) this.search.mouseClicked(event, doubleClick);
            else select(hovered(event.x(), event.y()));
        }
        return true;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (this.scrolling) scrollTo(event.y());
        else this.search.mouseDragged(event, dx, dy);
        return true;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        this.scrolling = false;
        this.search.mouseReleased(event);
        return true;
    }

    private void scrollTo(double y) {
        int rows = visibleRows();
        int track = rows * ROW_HEIGHT;
        int thumb = Math.max(8, track * rows / Math.max(1, this.filtered.size()));
        double position = (y - getY() - HEADER - thumb / 2.0) / Math.max(1, track - thumb);
        this.offset = Mth.clamp((int) Math.round(position * (this.filtered.size() - rows)),
                0, Math.max(0, this.filtered.size() - rows));
    }

    @Override
    public boolean mouseScrolled(double x, double y, double horizontal, double vertical) {
        if (isMouseOver(x, y)) this.offset = Mth.clamp(this.offset - (int) Math.signum(vertical) * 3,
                0, Math.max(0, this.filtered.size() - visibleRows()));
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.isEscape()) this.close.run();
        else if (event.isUp() || event.isDown()) {
            if (!this.filtered.isEmpty()) this.selected = Mth.clamp(this.selected + (event.isUp() ? -1 : 1), 0, this.filtered.size() - 1);
            revealSelected();
        } else if (event.isConfirmation()) select(this.selected);
        else this.search.keyPressed(event);
        return true;
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        this.search.charTyped(event);
        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        this.search.updateNarration(output);
        if (this.selected >= 0) output.add(NarratedElementType.TITLE, this.filtered.get(this.selected).name());
    }
}
