package io.github.bizcub.test.platform.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.github.bizcub.test.client.TestModClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class TestModFabricClientEntrypoint implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        TestModClient.init();

        ClientTickEvents.END_CLIENT_TICK.register(minecraft -> TestModClient.onClientTick());
    }

    public static class ModMenu implements ModMenuApi {

        @Override
        public ConfigScreenFactory<?> getModConfigScreenFactory() {
            return TestModClient::getConfigScreen;
        }
    }
}
