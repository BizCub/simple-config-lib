package io.github.bizcub.simpleConfigLib.autoconfig.gui;

import io.github.bizcub.simpleConfigLib.autoconfig.ConfigHolder;
import io.github.bizcub.simpleConfigLib.autoconfig.annotation.Side.Env;
import io.github.bizcub.simpleConfigLib.util.component.ComponentBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.server.permissions.Permissions;

import java.util.List;

public class ConfigHubScreen extends Screen {
    private final Screen lastScreen;
    private final List<ConfigHolder<?>> holders;

    public ConfigHubScreen(Screen lastScreen, List<ConfigHolder<?>> holders) {
        super(ComponentBuilder.translatable("text.simple_config_lib.hub.title").build());
        this.lastScreen = lastScreen;
        this.holders = holders;
    }

    public static Screen open(Screen parent) {
        List<ConfigHolder<?>> holders = ConfigHolder.registered();

        if (holders.size() == 1) {
            return screenFor(holders.get(0), parent);
        }
        return new ConfigHubScreen(parent, holders);
    }

    static Screen screenFor(ConfigHolder<?> holder, Screen parent) {
        Env env = holder.getMeta().env();
        boolean readOnly = env == Env.SERVER && !hasServerAccess();
        return AutoConfigScreen.create(holder, parent, env, readOnly);
    }

    private static boolean hasServerAccess() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.hasSingleplayerServer()) {
            return true;
        }
        //? >=26.2 {
        return mc.player.permissions().hasPermission(Permissions.COMMANDS_ADMIN);
        //?} else
        //return mc.player.hasPermissions(2);
    }

    static boolean readOnlyFor(final ConfigHolder<?> holder) {
        return holder.getMeta().env() == Env.SERVER && !hasServerAccess();
    }

    @Override
    protected void init() {
        GridLayout grid = new GridLayout().spacing(8);
        GridLayout.RowHelper rows = grid.createRowHelper(1);

        for (ConfigHolder<?> holder : this.holders) {
            Env env = holder.getMeta().env();
            boolean readOnly = readOnlyFor(holder);

            Button b = Button.builder(
                            ComponentBuilder.translatable("text." + holder.getMeta().name() + ".title").build(),
                            btn -> this.minecraft.gui.setScreen(
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

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(this.lastScreen);
    }
}
