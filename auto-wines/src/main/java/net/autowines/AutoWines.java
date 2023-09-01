package net.autowines;

import com.google.inject.Inject;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.autowines.overlay.AutoWinesOverlay;
import net.autowines.overlay.AutoWinesOverlayHelper;
import net.runelite.api.*;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.movement.pathfinder.model.BankLocation;
import net.unethicalite.api.plugins.LoopedPlugin;
import net.unethicalite.api.widgets.Dialog;
import net.unethicalite.api.widgets.Widgets;
import org.pf4j.Extension;

@Extension
@PluginDescriptor(
        name = "Auto Wines",
        description = "Automates Wine Fermentation",
        enabledByDefault = false
)
@Slf4j
public class AutoWines extends LoopedPlugin {

    @Inject
    private Client client;
    @Inject
    private Notifier notifier;

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private AutoWinesOverlay autoWinesOverlay;

    private int winesMade = 0;

    @Provides
    private AutoWinesConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(AutoWinesConfig.class);
    }

    @Override
    protected void startUp() throws Exception {
        if (client.getGameState() == GameState.LOGGED_IN) {
            AutoWinesOverlayHelper.expstarted = client.getSkillExperience(Skill.COOKING);
            AutoWinesOverlayHelper.startinglevel = client.getRealSkillLevel(Skill.COOKING);
            AutoWinesOverlayHelper.timeBegan = System.currentTimeMillis();
            if (overlayManager != null) {
                overlayManager.add(autoWinesOverlay);
            }
        }
    }

    @Override
    protected void shutDown() throws Exception {
        overlayManager.remove(autoWinesOverlay);
    }

    @Override
    protected int loop() {
        AutoWinesOverlayHelper.currentState = "Locating bank..";
        findBank();

        AutoWinesOverlayHelper.currentState = "Withdrawing..";
        withdrawIngredients();

        AutoWinesOverlayHelper.currentState = "Fermenting..";
        fermentWine();

        AutoWinesOverlayHelper.currentState = "Banking wines..";
        bankWines();

        winesMade += 14;
        AutoWinesOverlayHelper.wineAmount = winesMade;

        return 1000;
    }

    private void bankWines() {
        findBank();

        Bank.depositInventory();
    }

    private void fermentWine() {
        Item grape = Inventory.getFirst("Grapes");
        Item jugOfWater = Inventory.getFirst("Jug of water");

        grape.useOn(jugOfWater);

        Time.sleepUntil(Dialog::isOpen, () -> !Dialog.isOpen(), 1000, 7000);

        while (Dialog.isOpen()) {
            if (Widgets.isVisible(client.getWidget(270, 14))) {
                Dialog.continueSpace();

                Time.sleep(2000, 3000);
            }
        }

        Time.sleepUntil(this::hasFinishedUnfermentedWines, () -> Players.getLocal().isAnimating(), 100, 4000);
    }

    private void withdrawIngredients() {
        if (Bank.getFreeSlots() == 0) {
            notifier.notify("Free up some space before starting! Stopping script..");

            this.stop();
        }
        if (!hasRequiredItems()) {
            notifier.notify("You don't have any grapes or jugs of water! Stopping script..");

            this.stop();
        }

        if (!Inventory.isEmpty()) {
            Bank.depositInventory();

            Time.sleep(100, 230);
        }

        Bank.withdraw("Grapes", 14, Bank.WithdrawMode.ITEM);
        Bank.withdraw("Jug of water", 14, Bank.WithdrawMode.ITEM);

        Time.sleep(134, 323);

        if (hasGrapes() && hasJugsOfWater()) {
            Bank.close();

            Time.sleep(76, 200);
        }

        Time.sleepUntil(() -> !Bank.isOpen(), 1000);
    }

    private void findBank() {
        while (BankLocation.getNearest().getArea().distanceTo(Players.getLocal().getWorldLocation()) > 3) {
            Movement.walkTo(BankLocation.getNearest().getArea());

            Time.sleep(1200);
        }

        if (BankLocation.getNearest().getArea().distanceTo(Players.getLocal().getWorldLocation()) <= 3) {
            while (!Bank.isOpen()) {
                if (NPCs.getNearest("Banker") != null) {
                    NPCs.getNearest("Banker").interact("Bank");

                    Time.sleep(120, 234);
                } else if (TileObjects.getNearest("Bank booth") != null) {
                    TileObjects.getNearest("Bank booth").interact("Bank");

                    Time.sleep(120, 234);
                }
            }
        }

        Time.sleepUntil(Bank::isOpen, () -> !Bank.isOpen(), 100, 7000);
    }

    private boolean hasRequiredItems() {
        return Bank.contains("Grapes") && Bank.contains("Jug of water");
    }

    private boolean hasGrapes() {
        return Inventory.contains("Grapes") && Inventory.getCount("Grapes") == 14;
    }

    private boolean hasJugsOfWater() {
        return Inventory.contains("Jug of water") && Inventory.getCount("Grapes") == 14;
    }

    private boolean hasFinishedUnfermentedWines() {
        return Inventory.getCount("Unfermented wine") == 14;
    }
}
