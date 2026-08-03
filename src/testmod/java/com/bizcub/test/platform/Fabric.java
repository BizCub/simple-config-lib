//? fabric {
package com.bizcub.test.platform;

import com.bizcub.test.Main;
import com.bizcub.lib.autoconfig.screen.AutoConfigScreen;
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
            return parent -> new AutoConfigScreen(parent, Main.CONFIG);
        }
    }
}//?}
