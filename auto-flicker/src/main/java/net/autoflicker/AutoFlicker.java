package net.autoflicker;

import com.google.inject.Inject;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.HotkeyListener;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.widgets.Prayers;
import org.pf4j.Extension;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Extension
@PluginDescriptor(
        name = "Auto Flicker",
        description = "Automates quick prayer flicking",
        enabledByDefault = false
)
@Slf4j
public class AutoFlicker extends Plugin {

    @Inject
    private Client client;
    @Inject
    private KeyManager keyManager;
    @Inject
    private AutoFlickerConfig autoFlickerConfig;

    @Provides
    AutoFlickerConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(AutoFlickerConfig.class);
    }

    private boolean hotKeyToggled;
    private final ExecutorService executor = Executors.newFixedThreadPool(1);

    private final HotkeyListener keyBinding = new HotkeyListener(() -> autoFlickerConfig.keyBinding()) {
        @Override
        public void hotkeyPressed() {
            hotKeyToggled = !hotKeyToggled;
        }
    };

    @Override
    protected void startUp() throws Exception {
        if (client.getGameState() == GameState.LOGGED_IN) {
            keyManager.registerKeyListener(keyBinding);

            super.startUp();
        }
    }

    @Subscribe
    public void onGameTick(GameTick gameTick) {
        if (hotKeyToggled) {
            executor.submit(this::dispatchPrayerToggle);
        }
    }

    private void dispatchPrayerToggle() {
        Prayers.toggleQuickPrayer(false);
        Time.sleep(43, 89);
        Prayers.toggleQuickPrayer(true);
    }

    @Override
    protected void shutDown() throws Exception {
        hotKeyToggled = false;
        keyManager.unregisterKeyListener(keyBinding);

        super.shutDown();
    }
}
