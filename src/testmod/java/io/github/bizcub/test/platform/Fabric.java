//? fabric {
package io.github.bizcub.test.platform;

import io.github.bizcub.test.Main;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.github.bizcub.test.config.SimpleConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class Fabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Main.init();

        ClientTickEvents.END_CLIENT_TICK.register(minecraft -> Main.setTestScreen());
    }

    public static class ModMenu implements ModMenuApi {

        @Override
        public ConfigScreenFactory<?> getModConfigScreenFactory() {
            return SimpleConfig.getInstance()::createScreen;
        }
    }
}//?}
