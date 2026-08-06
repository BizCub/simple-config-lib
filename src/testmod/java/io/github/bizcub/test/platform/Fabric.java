//? fabric {
package io.github.bizcub.test.platform;

import io.github.bizcub.test.Main;
import io.github.bizcub.lib.autoconfig.gui.AutoConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.api.ModInitializer;

public class Fabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Main.init();
    }

    public static class ModMenu implements ModMenuApi {

        @Override
        public ConfigScreenFactory<?> getModConfigScreenFactory() {
            return screen -> new AutoConfigScreen(screen, Main.CONFIG);
        }
    }
}//?}
