package io.github.bizcub.test.screen;

import io.github.bizcub.simpleConfigLib.util.widget.ScaledItemDisplayWidget;
import io.github.bizcub.test.config.Config;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class TestScreen extends Screen {

    public TestScreen(Component title) {
        super(title);
    }

    @Override
    protected void init() {
        super.init();
        ScaledItemDisplayWidget widget = addRenderableWidget(
                new ScaledItemDisplayWidget(
                        this.width / 2,
                        this.height / 2,
                        new ItemStack(Items.DIAMOND_BLOCK),
                        Config.get().entityLodBias()
                )
        );
        widget.setCentred();
    }
}
