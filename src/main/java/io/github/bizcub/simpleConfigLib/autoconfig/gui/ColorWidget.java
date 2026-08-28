package io.github.bizcub.simpleConfigLib.autoconfig.gui;

import io.github.bizcub.simpleConfigLib.util.component.ComponentBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.util.Mth;

import java.util.Locale;
import java.util.function.IntConsumer;

//? >=1.21.9 {
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;//?}

public final class ColorWidget extends AbstractWidget {
    private static final int SWATCH = 18;
    private static final int GAP = 2;
    private static final int DEFAULT_WIDTH = 150;

    private static final int SV_SIZE = 100;
    private static final int HUE_W = 12;
    private static final int PAD = 4;
    private static final int HUE_STEPS = 60;
    private static final int SV_STEPS = 24;
    private static final int ALPHA_STEPS = 60;
    private static final int CHECKER = 4;
    private static final int CHECKER_A = 0xFFBFBFBF;
    private static final int CHECKER_B = 0xFF7F7F7F;

    private final boolean alpha;
    private final IntConsumer onChange;
    private final EditBox box;

    private int color;
    private float hue;
    private float sat;
    private float val;
    private int alphaByte = 0xFF;

    private boolean pickerOpen;
    private boolean draggingSV;
    private boolean draggingHue;
    private boolean draggingAlpha;
    private boolean updatingBox;

    public ColorWidget(final Font font, final int initial, final boolean alpha, final IntConsumer onChange) {
        super(0, 0, DEFAULT_WIDTH, 20, ComponentBuilder.empty().build());
        this.alpha = alpha;
        this.onChange = onChange;
        setColorFromInt(normalize(initial));
        this.box = new EditBox(font, 0, 0, DEFAULT_WIDTH - SWATCH - GAP, 20, ComponentBuilder.empty().build());
        this.box.setMaxLength(9);
        this.box.setValue(format(this.color));
        this.box.setResponder(this::onText);
    }

    private int normalize(final int c) {
        return alpha ? c : (c | 0xFF000000);
    }

    private void setColorFromInt(final int c) {
        this.color = normalize(c);
        this.alphaByte = alpha ? (c >>> 24) & 0xFF : 0xFF;
        float r = ((c >> 16) & 0xFF) / 255f;
        float g = ((c >> 8) & 0xFF) / 255f;
        float b = (c & 0xFF) / 255f;
        float[] hsv = rgbToHsv(r, g, b);
        this.hue = hsv[0];
        this.sat = hsv[1];
        this.val = hsv[2];
    }

    private void recomputeColor() {
        int rgb = hsvToRgb(this.hue, this.sat, this.val);
        int a = alpha ? this.alphaByte : 0xFF;
        this.color = (a << 24) | (rgb & 0xFFFFFF);
    }

    private String format(final int c) {
        return alpha ? String.format(Locale.ROOT, "#%08X", c)
                : String.format(Locale.ROOT, "#%06X", c & 0xFFFFFF);
    }

    private void onText(final String v) {
        if (this.updatingBox) {
            return;
        }
        String s = v.trim();
        if (s.startsWith("#")) s = s.substring(1);
        if (s.startsWith("0x") || s.startsWith("0X")) s = s.substring(2);
        try {
            setColorFromInt(normalize((int) Long.parseLong(s, 16)));
            this.onChange.accept(this.color);
        } catch (NumberFormatException ignored) {
        }
    }

    private void applyHsv() {
        recomputeColor();
        this.updatingBox = true;
        this.box.setValue(format(this.color));
        this.updatingBox = false;
        this.onChange.accept(this.color);
    }

    private int swatchX() { return getX(); }
    private int swatchY() { return getY() + 1; }

    private int pickerX() { return getX(); }
    private int pickerY() {
        int below = getY() + 20 + 2;
        int screenH = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        if (below + pickerH() > screenH) {
            return getY() - 2 - pickerH();
        }
        return below;
    }
    private int pickerW() {
        int bars = alpha ? 2 : 1;
        return PAD * (2 + bars) + SV_SIZE + bars * HUE_W;
    }
    private int pickerH() { return PAD * 2 + SV_SIZE; }

    private int svX() { return pickerX() + PAD; }
    private int svY() { return pickerY() + PAD; }
    private int hueX() { return svX() + SV_SIZE + PAD; }
    private int hueY() { return svY(); }
    private int alphaX() { return hueX() + HUE_W + PAD; }
    private int alphaY() { return svY(); }

    boolean pickerOpen() { return this.pickerOpen; }

    void closePicker() { this.pickerOpen = false; }

    boolean isOverSwatch(final double mx, final double my) {
        return mx >= swatchX() && mx < swatchX() + SWATCH
                && my >= swatchY() && my < swatchY() + SWATCH;
    }

    boolean isOverPicker(final double mx, final double my) {
        return this.pickerOpen
                && mx >= pickerX() && mx < pickerX() + pickerW()
                && my >= pickerY() && my < pickerY() + pickerH();
    }

    private boolean isOverSV(final double mx, final double my) {
        return mx >= svX() && mx < svX() + SV_SIZE && my >= svY() && my < svY() + SV_SIZE;
    }

    private boolean isOverHue(final double mx, final double my) {
        return mx >= hueX() && mx < hueX() + HUE_W && my >= hueY() && my < hueY() + SV_SIZE;
    }

    private boolean isOverAlpha(final double mx, final double my) {
        return alpha && mx >= alphaX() && mx < alphaX() + HUE_W
                && my >= alphaY() && my < alphaY() + SV_SIZE;
    }

    @Override //$ render_aw >> ' graphics'
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial) {
        int sx = swatchX(), sy = swatchY();
        graphics.fill(sx - 1, sy - 1, sx + SWATCH + 1, sy + SWATCH + 1, 0xFF000000);
        graphics.fill(sx, sy, sx + SWATCH, sy + SWATCH, this.color);

        this.box.setPosition(getX() + SWATCH + GAP, getY());
        this.box.setWidth(getWidth() - SWATCH - GAP);
        this.box.extractRenderState(graphics, mouseX, mouseY, partial);
    }

    void renderPicker(final GuiGraphicsExtractor graphics) {
        if (!this.pickerOpen) return;

        //? <1.21.6 {
        /*graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 400);*///?}

        graphics.fill(pickerX() - 1, pickerY() - 1,
                pickerX() + pickerW() + 1, pickerY() + pickerH() + 1, 0xFF000000);
        graphics.fill(pickerX(), pickerY(),
                pickerX() + pickerW(), pickerY() + pickerH(), 0xFF202020);

        renderSvSquare(graphics);
        renderHueBar(graphics);
        if (alpha) {
            renderAlphaBar(graphics);
        }
        renderCursors(graphics);

        //? <1.21.6
        //graphics.pose().popPose();
    }

    private void renderSvSquare(final GuiGraphicsExtractor graphics) {
        int x0 = svX(), y0 = svY();
        int colW = Math.max(1, SV_SIZE / SV_STEPS);
        int rowH = Math.max(1, SV_SIZE / SV_STEPS);
        for (int cx = 0; cx < SV_SIZE; cx += colW) {
            float s = cx / (float) SV_SIZE;
            for (int cy = 0; cy < SV_SIZE; cy += rowH) {
                float v = 1f - cy / (float) SV_SIZE;
                int rgb = hsvToRgb(this.hue, s, v);
                graphics.fill(x0 + cx, y0 + cy,
                        Math.min(x0 + cx + colW, x0 + SV_SIZE),
                        Math.min(y0 + cy + rowH, y0 + SV_SIZE),
                        0xFF000000 | rgb);
            }
        }
    }

    private void renderHueBar(final GuiGraphicsExtractor graphics) {
        int x0 = hueX(), y0 = hueY();
        int stepH = Math.max(1, SV_SIZE / HUE_STEPS);
        for (int cy = 0; cy < SV_SIZE; cy += stepH) {
            float h = cy / (float) SV_SIZE;
            int rgb = hsvToRgb(h, 1f, 1f);
            graphics.fill(x0, y0 + cy, x0 + HUE_W,
                    Math.min(y0 + cy + stepH, y0 + SV_SIZE), 0xFF000000 | rgb);
        }
    }

    private void renderAlphaBar(final GuiGraphicsExtractor graphics) {
        int x0 = alphaX(), y0 = alphaY();
        int rgb = hsvToRgb(this.hue, this.sat, this.val) & 0xFFFFFF;

        for (int cy = 0; cy < SV_SIZE; cy += CHECKER) {
            for (int cx = 0; cx < HUE_W; cx += CHECKER) {
                boolean even = ((cx / CHECKER) + (cy / CHECKER)) % 2 == 0;
                graphics.fill(x0 + cx, y0 + cy,
                        Math.min(x0 + cx + CHECKER, x0 + HUE_W),
                        Math.min(y0 + cy + CHECKER, y0 + SV_SIZE),
                        even ? CHECKER_A : CHECKER_B);
            }
        }

        int stepH = Math.max(1, SV_SIZE / ALPHA_STEPS);
        for (int cy = 0; cy < SV_SIZE; cy += stepH) {
            int a = Math.round((1f - cy / (float) SV_SIZE) * 255f);
            graphics.fill(x0, y0 + cy, x0 + HUE_W,
                    Math.min(y0 + cy + stepH, y0 + SV_SIZE), (a << 24) | rgb);
        }
    }

    boolean isDraggingPicker() {
        return this.draggingSV || this.draggingHue || this.draggingAlpha;
    }

    private void renderCursors(final GuiGraphicsExtractor graphics) {
        int cx = svX() + Math.round(this.sat * SV_SIZE);
        int cy = svY() + Math.round((1f - this.val) * SV_SIZE);
        int r = 3;
        graphics.fill(cx - r, cy - r, cx + r + 1, cy - r + 1, 0xFFFFFFFF);
        graphics.fill(cx - r, cy + r, cx + r + 1, cy + r + 1, 0xFFFFFFFF);
        graphics.fill(cx - r, cy - r, cx - r + 1, cy + r + 1, 0xFFFFFFFF);
        graphics.fill(cx + r, cy - r, cx + r + 1, cy + r + 1, 0xFFFFFFFF);

        int hy = hueY() + Math.round(this.hue * SV_SIZE);
        graphics.fill(hueX() - 1, hy - 1, hueX() + HUE_W + 1, hy + 1, 0xFFFFFFFF);

        if (alpha) {
            int ay = alphaY() + Math.round((1f - this.alphaByte / 255f) * SV_SIZE);
            graphics.fill(alphaX() - 1, ay - 1, alphaX() + HUE_W + 1, ay + 1, 0xFFFFFFFF);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean doubleClick) {
        //~ mb_event
        double mx = mouseButtonEvent.x(), my = mouseButtonEvent.y(); //~ !mb_event
        if (this.pickerOpen) {
            if (isOverSV(mx, my)) {
                this.draggingSV = true;
                updateSV(mx, my);
                return true;
            }
            if (isOverHue(mx, my)) {
                this.draggingHue = true;
                updateHue(my);
                return true;
            }
            if (isOverAlpha(mx, my)) {
                this.draggingAlpha = true;
                updateAlpha(my);
                return true;
            }
            if (isOverPicker(mx, my)) {
                return true;
            }
        }
        if (isOverSwatch(mx, my)) {
            this.pickerOpen = !this.pickerOpen;
            return true;
        }
        this.pickerOpen = false;
        return this.box.mouseClicked(mouseButtonEvent, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent mouseButtonEvent, double dx, double dy) {
        //~ mb_event
        if (this.draggingSV) {
            updateSV(mouseButtonEvent.x(), mouseButtonEvent.y());
            return true;
        }
        if (this.draggingHue) {
            updateHue(mouseButtonEvent.y());
            return true;
        }
        if (this.draggingAlpha) {
            updateAlpha(mouseButtonEvent.y());
            return true;
        } //~ !mb_event
        return this.box.mouseDragged(mouseButtonEvent, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent mouseButtonEvent) {
        this.draggingSV = false;
        this.draggingHue = false;
        this.draggingAlpha = false;
        return this.box.mouseReleased(mouseButtonEvent);
    }

    private void updateSV(final double mx, final double my) {
        this.sat = Mth.clamp((float) (mx - svX()) / SV_SIZE, 0f, 1f);
        this.val = Mth.clamp(1f - (float) (my - svY()) / SV_SIZE, 0f, 1f);
        applyHsv();
    }

    private void updateHue(final double my) {
        this.hue = Mth.clamp((float) (my - hueY()) / SV_SIZE, 0f, 1f);
        applyHsv();
    }

    private void updateAlpha(final double my) {
        float t = Mth.clamp(1f - (float) (my - alphaY()) / SV_SIZE, 0f, 1f);
        this.alphaByte = Math.round(t * 255f);
        applyHsv();
    }

    @Override
    public boolean isMouseOver(final double mx, final double my) {
        return super.isMouseOver(mx, my) || isOverPicker(mx, my);
    }

    @Override
    public boolean charTyped(CharacterEvent characterEvent) {
        return this.box.charTyped(characterEvent) || super.charTyped(characterEvent);
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        return this.box.keyPressed(keyEvent) || super.keyPressed(keyEvent);
    }

    @Override
    public void setFocused(final boolean focused) {
        super.setFocused(focused);
        this.box.setFocused(focused);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {

    }

    private static int hsvToRgb(final float h, final float s, final float v) {
        float r, g, b;
        int i = (int) Math.floor(h * 6f);
        float f = h * 6f - i;
        float p = v * (1f - s);
        float q = v * (1f - f * s);
        float t = v * (1f - (1f - f) * s);
        switch (Math.floorMod(i, 6)) {
            case 0 -> { r = v; g = t; b = p; }
            case 1 -> { r = q; g = v; b = p; }
            case 2 -> { r = p; g = v; b = t; }
            case 3 -> { r = p; g = q; b = v; }
            case 4 -> { r = t; g = p; b = v; }
            default -> { r = v; g = p; b = q; }
        }
        int ri = Math.round(r * 255f);
        int gi = Math.round(g * 255f);
        int bi = Math.round(b * 255f);
        return (ri << 16) | (gi << 8) | bi;
    }

    private static float[] rgbToHsv(final float r, final float g, final float b) {
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float delta = max - min;
        float h = 0f;
        if (delta > 0f) {
            if (max == r)      h = ((g - b) / delta) % 6f;
            else if (max == g) h = (b - r) / delta + 2f;
            else               h = (r - g) / delta + 4f;
            h /= 6f;
            if (h < 0f) h += 1f;
        }
        float s = max == 0f ? 0f : delta / max;
        return new float[] { h, s, max };
    }
}
