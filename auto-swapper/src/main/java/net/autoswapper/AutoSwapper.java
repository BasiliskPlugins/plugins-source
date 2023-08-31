package net.autoswapper;

import com.google.common.base.Splitter;
import com.google.inject.Inject;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.Prayer;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.HotkeyListener;
import net.unethicalite.api.items.Equipment;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.widgets.Prayers;
import org.pf4j.Extension;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

@Extension
@PluginDescriptor(
        name = "Auto Swapper",
        description = "Auto swaps gear of your choice!",
        enabledByDefault = false
)
@Slf4j
public class AutoSwapper extends Plugin {
    private static final Splitter NEWLINE_SPLITTER = Splitter
            .on("\n")
            .omitEmptyStrings()
            .trimResults();

    @Inject
    private Client client;
    @Inject
    private ClientThread clientThread;
    @Inject
    private KeyManager keyManager;
    @Inject
    private AutoSwapperConfig config;

    @Provides
    AutoSwapperConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(AutoSwapperConfig.class);
    }

    @Override
    protected void startUp() throws AWTException {
        if (client.getGameState() == GameState.LOGGED_IN) {
            keyManager.registerKeyListener(one);
            keyManager.registerKeyListener(two);
            keyManager.registerKeyListener(three);
            keyManager.registerKeyListener(four);
            keyManager.registerKeyListener(five);
            keyManager.registerKeyListener(six);
            keyManager.registerKeyListener(seven);
            keyManager.registerKeyListener(eight);
            keyManager.registerKeyListener(nine);
            keyManager.registerKeyListener(ten);
            keyManager.registerKeyListener(eleven);
            keyManager.registerKeyListener(twelve);
            keyManager.registerKeyListener(thirteen);
            keyManager.registerKeyListener(fourteen);
            keyManager.registerKeyListener(fifteen);
            keyManager.registerKeyListener(sixteen);
            keyManager.registerKeyListener(seventeen);
            keyManager.registerKeyListener(eighteen);
            keyManager.registerKeyListener(nineteen);
            keyManager.registerKeyListener(twenty);
        }
    }

    @Override
    protected void shutDown() {
        keyManager.unregisterKeyListener(one);
        keyManager.unregisterKeyListener(two);
        keyManager.unregisterKeyListener(three);
        keyManager.unregisterKeyListener(four);
        keyManager.unregisterKeyListener(five);
        keyManager.unregisterKeyListener(six);
        keyManager.unregisterKeyListener(seven);
        keyManager.unregisterKeyListener(eight);
        keyManager.unregisterKeyListener(nine);
        keyManager.unregisterKeyListener(ten);
        keyManager.unregisterKeyListener(eleven);
        keyManager.unregisterKeyListener(twelve);
        keyManager.unregisterKeyListener(thirteen);
        keyManager.unregisterKeyListener(fourteen);
        keyManager.unregisterKeyListener(fifteen);
        keyManager.unregisterKeyListener(sixteen);
        keyManager.unregisterKeyListener(seventeen);
        keyManager.unregisterKeyListener(eighteen);
        keyManager.unregisterKeyListener(nineteen);
        keyManager.unregisterKeyListener(twenty);
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        if (event.getGameState() != GameState.LOGGED_IN) {
            keyManager.unregisterKeyListener(one);
            keyManager.unregisterKeyListener(two);
            keyManager.unregisterKeyListener(three);
            keyManager.unregisterKeyListener(four);
            keyManager.unregisterKeyListener(five);
            keyManager.unregisterKeyListener(six);
            keyManager.unregisterKeyListener(seven);
            keyManager.unregisterKeyListener(eight);
            keyManager.unregisterKeyListener(nine);
            keyManager.unregisterKeyListener(ten);
            keyManager.unregisterKeyListener(eleven);
            keyManager.unregisterKeyListener(twelve);
            keyManager.unregisterKeyListener(thirteen);
            keyManager.unregisterKeyListener(fourteen);
            keyManager.unregisterKeyListener(fifteen);
            keyManager.unregisterKeyListener(sixteen);
            keyManager.unregisterKeyListener(seventeen);
            keyManager.unregisterKeyListener(eighteen);
            keyManager.unregisterKeyListener(nineteen);
            keyManager.unregisterKeyListener(twenty);
            return;
        }
        keyManager.registerKeyListener(one);
        keyManager.registerKeyListener(two);
        keyManager.registerKeyListener(three);
        keyManager.registerKeyListener(four);
        keyManager.registerKeyListener(five);
        keyManager.registerKeyListener(six);
        keyManager.registerKeyListener(seven);
        keyManager.registerKeyListener(eight);
        keyManager.registerKeyListener(nine);
        keyManager.registerKeyListener(ten);
        keyManager.registerKeyListener(eleven);
        keyManager.registerKeyListener(twelve);
        keyManager.registerKeyListener(thirteen);
        keyManager.registerKeyListener(fourteen);
        keyManager.registerKeyListener(fifteen);
        keyManager.registerKeyListener(sixteen);
        keyManager.registerKeyListener(seventeen);
        keyManager.registerKeyListener(eighteen);
        keyManager.registerKeyListener(nineteen);
        keyManager.registerKeyListener(twenty);
    }

    private final HotkeyListener one = new HotkeyListener(() -> config.customOne()) {
        @Override
        public void hotkeyPressed() {
            clientThread.invokeLater(() -> decode(config.customSwapOne()));
        }
    };

    private final HotkeyListener two = new HotkeyListener(() -> config.customTwo()) {
        @Override
        public void hotkeyPressed() {
            clientThread.invokeLater(() -> decode(config.customSwapTwo()));
        }
    };

    private final HotkeyListener three = new HotkeyListener(() -> config.customThree()) {
        @Override
        public void hotkeyPressed() {
            clientThread.invokeLater(() -> decode(config.customSwapThree()));
        }
    };

    private final HotkeyListener four = new HotkeyListener(() -> config.customFour()) {
        @Override
        public void hotkeyPressed() {
            clientThread.invokeLater(() -> decode(config.customSwapFour()));
        }
    };

    private final HotkeyListener five = new HotkeyListener(() -> config.customFive()) {
        @Override
        public void hotkeyPressed() {
            clientThread.invokeLater(() -> decode(config.customSwapFive()));
        }
    };

    private final HotkeyListener six = new HotkeyListener(() -> config.customSix()) {
        @Override
        public void hotkeyPressed() {
            clientThread.invokeLater(() -> decode(config.customSwapSix()));
        }
    };

    private final HotkeyListener seven = new HotkeyListener(() -> config.customSeven()) {
        @Override
        public void hotkeyPressed() {
            clientThread.invokeLater(() -> decode(config.customSwapSeven()));
        }
    };

    private final HotkeyListener eight = new HotkeyListener(() -> config.customEight()) {
        @Override
        public void hotkeyPressed() {
            clientThread.invokeLater(() -> decode(config.customSwapEight()));
        }
    };

    private final HotkeyListener nine = new HotkeyListener(() -> config.customNine()) {
        @Override
        public void hotkeyPressed() {
            clientThread.invokeLater(() -> decode(config.customSwapNine()));
        }
    };

    private final HotkeyListener ten = new HotkeyListener(() -> config.customTen()) {
        @Override
        public void hotkeyPressed() {
            clientThread.invokeLater(() -> decode(config.customSwapTen()));
        }
    };

    private final HotkeyListener eleven = new HotkeyListener(() -> config.customEleven()) {
        @Override
        public void hotkeyPressed() {
            clientThread.invokeLater(() -> decode(config.customSwapEleven()));
        }
    };

    private final HotkeyListener twelve = new HotkeyListener(() -> config.customTwelve()) {
        @Override
        public void hotkeyPressed() {
            clientThread.invokeLater(() -> decode(config.customSwapTwelve()));
        }
    };

    private final HotkeyListener thirteen = new HotkeyListener(() -> config.customThirteen()) {
        @Override
        public void hotkeyPressed() {
            clientThread.invokeLater(() -> decode(config.customSwapThirteen()));
        }
    };

    private final HotkeyListener fourteen = new HotkeyListener(() -> config.customFourteen()) {
        @Override
        public void hotkeyPressed() {
            clientThread.invokeLater(() -> decode(config.customSwapFourteen()));
        }
    };

    private final HotkeyListener fifteen = new HotkeyListener(() -> config.customFifteen()) {
        @Override
        public void hotkeyPressed() {
            clientThread.invokeLater(() -> decode(config.customSwapFifteen()));
        }
    };

    private final HotkeyListener sixteen = new HotkeyListener(() -> config.customSixteen()) {
        @Override
        public void hotkeyPressed() {
            clientThread.invokeLater(() -> decode(config.customSwapSixteen()));
        }
    };

    private final HotkeyListener seventeen = new HotkeyListener(() -> config.customSeventeen()) {
        @Override
        public void hotkeyPressed() {
            clientThread.invokeLater(() -> decode(config.customSwapSeventeen()));
        }
    };

    private final HotkeyListener eighteen = new HotkeyListener(() -> config.customEighteen()) {
        @Override
        public void hotkeyPressed() {
            clientThread.invokeLater(() -> decode(config.customSwapEighteen()));
        }
    };

    private final HotkeyListener nineteen = new HotkeyListener(() -> config.customNineteen()) {
        @Override
        public void hotkeyPressed() {
            clientThread.invokeLater(() -> decode(config.customSwapNineteen()));
        }
    };

    private final HotkeyListener twenty = new HotkeyListener(() -> config.customTwenty()) {
        @Override
        public void hotkeyPressed() {
            clientThread.invokeLater(() -> decode(config.customSwapTwenty()));
        }
    };

    private void decode(String string) {
        final Map<String, String> map = new LinkedHashMap<>();
        final Iterable<String> tmp = NEWLINE_SPLITTER.split(string);

        for (String s : tmp) {
            if (s.startsWith("//")) {
                continue;
            }
            String[] split = s.split(":");
            try {
                map.put(split[0], split[1]);
            } catch (IndexOutOfBoundsException e) {
                log.error("Decode: Invalid Syntax in decoder.");

                return;
            }
        }

        performSwitches(map);
    }

    private void performSwitches(Map<String, String> map) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String param = entry.getKey();
            String command = entry.getValue().toLowerCase();

            switch (command) {
                case "equip": {
                    clientThread.invoke(() -> {
                        final Item item = Inventory.getFirst(param);

                        if (item == null) {
                            log.debug("Equip: Can't find valid bounds for param {}.", param);

                            return;
                        }

                        item.interact(x -> x != null && (x.toLowerCase().contains("wear")
                                || x.toLowerCase().contains("wield")
                                || x.toLowerCase().contains("equip")));
                    });
                }

                break;

                case "remove": {
                    clientThread.invoke(() -> {
                        final Item item = Equipment.getFirst(param);

                        if (item == null) {
                            log.debug("Remove: Can't find valid bounds for param {}.", param);

                            return;
                        }

                        item.interact(x -> x != null && x.toLowerCase().contains("remove"));
                    });
                }

                break;

                case "prayer": {
                    final Prayer p = Prayer.valueOf(param.toUpperCase().replace(" ", "_"));

                    Prayers.toggle(p);
                }

                break;
            }
        }
    }
}
