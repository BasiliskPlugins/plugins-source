package net.autoflicker;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.WidgetID;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
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

    private boolean pluginEnabled;

    private final ExecutorService executor = Executors.newFixedThreadPool(1);

    @Override
    protected void startUp() throws Exception {
        pluginEnabled = true;

        super.startUp();
    }

    @Subscribe
    public void onGameTick(GameTick gameTick) {
        if (pluginEnabled) {
            executor.submit(this::dispatchPrayerToggle);
        }
    }

    private void dispatchPrayerToggle() {
        System.out.println("Toggling..");
        Prayers.toggleQuickPrayer(false);
        Time.sleep(43, 89);
        Prayers.toggleQuickPrayer(true);
    }

    @Override
    protected void shutDown() throws Exception {
        pluginEnabled = false;

        super.shutDown();
    }


}
