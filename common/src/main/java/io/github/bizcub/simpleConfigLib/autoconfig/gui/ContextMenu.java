package io.github.bizcub.simpleConfigLib.autoconfig.gui;

import io.github.bizcub.simpleConfigLib.internal.GuiSounds;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.Comparator;
import java.util.List;

final class ContextMenu extends AbstractWidget {
    private static final int ROW_HEIGHT = 20;
    private final Font font;
    private final List<AutoConfigScreen.RowAction> actions;
    private final Runnable close;
    private int selected;

    ContextMenu(Font font, List<AutoConfigScreen.RowAction> actions, Runnable close, int x, int y, int screenWidth, int screenHeight) {
        super(0, 0, 120, actions.size() * ROW_HEIGHT + 4, Component.empty());
        this.font = font;
        this.actions = actions.stream().sorted(Comparator.comparing(
                action -> action.key().equals("text.simple_config_lib.reset_value"))).toList();
        this.close = close;
        int width = 120;
        for (var action : actions) width = Math.max(width, font.width(Component.translatable(action.key())) + 16);
        setWidth(Math.min(width, screenWidth - 4));
        setPosition(Math.max(2, Math.min(x, screenWidth - getWidth() - 2)),
                Math.max(2, Math.min(y, screenHeight - getHeight() - 2)));
        this.selected = -1;
        select(1);
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(getX() - 1, getY() - 1, getRight() + 1, getBottom() + 1, 0xFF000000);
        graphics.fill(getX(), getY(), getRight(), getBottom(), 0xFF202020);
        int hovered = isMouseOver(mouseX, mouseY) ? (mouseY - getY() - 2) / ROW_HEIGHT : -1;
        for (int i = 0; i < this.actions.size(); i++) {
            var action = this.actions.get(i);
            int y = getY() + 2 + i * ROW_HEIGHT;
            boolean highlight = hovered == i || (hovered < 0 && this.selected == i);
            if (highlight && action.enabled()) graphics.fill(getX() + 2, y, getRight() - 2, y + ROW_HEIGHT, 0xFF404040);
            graphics.text(this.font, Component.translatable(action.key()), getX() + 6, y + 6,
                    action.enabled() ? 0xFFFFFFFF : 0xFF777777);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (!isMouseOver(event.x(), event.y())) return false;
        if (event.button() == 1) {
            int index = ((int) event.y() - getY() - 2) / ROW_HEIGHT;
            if (index >= 0 && index < this.actions.size()) activate(index);
        }
        return true;
    }

    private void select(int direction) {
        for (int i = 0; i < this.actions.size(); i++) {
            this.selected = Math.floorMod(this.selected + direction, this.actions.size());
            if (this.actions.get(this.selected).enabled()) return;
        }
        this.selected = -1;
    }

    private void activate(int index) {
        var action = this.actions.get(index);
        if (!action.enabled()) return;
        this.close.run();
        GuiSounds.playClick();
        action.action().run();
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.isEscape()) this.close.run();
        else if (event.isUp()) select(-1);
        else if (event.isDown() || event.isCycleFocus()) select(event.hasShiftDown() ? -1 : 1);
        else if (event.isConfirmation() && this.selected >= 0) activate(this.selected);
        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        if (this.selected >= 0) output.add(NarratedElementType.TITLE, Component.translatable(this.actions.get(this.selected).key()));
    }
}
