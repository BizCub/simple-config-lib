//? fabric {
package io.github.bizcub.test.platform;

import io.github.bizcub.test.Main;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class Fabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Main.init();

        ClientTickEvents.END_CLIENT_TICK.register(minecraft -> Main.setTestScreen());
    }

    public static class ModMenu implements ModMenuApi {

        @Override
        public ConfigScreenFactory<?> getModConfigScreenFactory() {
            return Main::getConfigScreen;
        }
    }
}//?}
