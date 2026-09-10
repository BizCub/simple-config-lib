//? fabric {
package io.github.bizcub.test.client.platform;

import io.github.bizcub.test.client.TestModClient;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class FabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        TestModClient.init();

        ClientTickEvents.END_CLIENT_TICK.register(minecraft -> TestModClient.setTestScreen());
    }

    public static class ModMenu implements ModMenuApi {

        @Override
        public ConfigScreenFactory<?> getModConfigScreenFactory() {
            return TestModClient::getConfigScreen;
        }
    }
}//?}
