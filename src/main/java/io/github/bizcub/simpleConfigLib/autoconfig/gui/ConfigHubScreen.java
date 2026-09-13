package io.github.bizcub.simpleConfigLib.autoconfig.gui;

import io.github.bizcub.simpleConfigLib.autoconfig.ConfigHolder;
import io.github.bizcub.simpleConfigLib.autoconfig.ConfigSide;
import io.github.bizcub.simpleConfigLib.util.KeyFormatter;
import io.github.bizcub.simpleConfigLib.util.component.ComponentBuilder;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ConfigHubScreen extends Screen {
    private final Screen lastScreen;
    private final List<ConfigHolder<?>> holders;

    public ConfigHubScreen(Screen lastScreen, List<ConfigHolder<?>> holders) {
        super(ComponentBuilder.translatable("text.simple_config_lib.hub.title").build());
        this.lastScreen = lastScreen;
        this.holders = holders;
    }

    @Override
    protected void init() {
        GridLayout grid = new GridLayout().spacing(8);
        GridLayout.RowHelper rows = grid.createRowHelper(1);

        for (ConfigHolder<?> holder : this.holders) {
            ConfigSide env = holder.getMeta().side();
            boolean readOnly = ConfigScreenFactory.readOnlyFor(holder);

            String pretty = KeyFormatter.humanize(holder.getMeta().name());
            Component envLabel = ComponentBuilder.translatable("text.simple_config_lib.env." + env.name().toLowerCase()).build();

            Button b = Button.builder(
                            ComponentBuilder.translatable("text.simple_config_lib.hub.entry", pretty, envLabel).build(),
                            button -> this.minecraft.gui.setScreen(
                                    AutoConfigScreen.create(holder, this, env, readOnly)))
                    .width(200)
                    .build();

            rows.addChild(b);
        }

        rows.addChild(Button.builder(CommonComponents.GUI_BACK, b -> onClose()).width(200).build());

        grid.arrangeElements();
        FrameLayout.centerInRectangle(grid, 0, 0, this.width, this.height);
        grid.visitWidgets(this::addRenderableWidget);
    }

    //? >=1.20.2 && <=1.20.4 {
    /*@Override
    public void renderBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderDirtBackground(guiGraphics);
    }

    *///?} 1.20.1 {
    /*@Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderDirtBackground(guiGraphics);
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
    }*///?}

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(this.lastScreen);
    }
}
