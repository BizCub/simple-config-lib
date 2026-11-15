package io.github.bizcub.test.platform.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.github.bizcub.test.client.TestModClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;

public class TestModFabricClientEntrypoint implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        TestModClient.init();
        ClientTickEvents.END_CLIENT_TICK.register(client -> TestModClient.onClientTick());
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) ->
                ScreenEvents.afterExtract(screen).register((current, graphics, mouseX, mouseY, tick) ->
                        TestModClient.renderPreview(current, graphics)));
    }

    public static class ModMenu implements ModMenuApi {

        @Override
        public ConfigScreenFactory<?> getModConfigScreenFactory() {
            return TestModClient::getConfigScreen;
        }
    }
}
