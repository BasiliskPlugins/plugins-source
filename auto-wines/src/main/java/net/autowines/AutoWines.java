package net.autowines;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.autowines.loadout.ActionDelayHelper;
import net.autowines.overlay.AutoWinesOverlay;
import net.autowines.overlay.AutoWinesOverlayHelper;
import net.runelite.api.*;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.input.Keyboard;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.plugins.Script;
import net.unethicalite.api.widgets.Widgets;
import org.pf4j.Extension;

import java.util.Map;

@Slf4j
@PluginDescriptor(
        name = "Auto Wines",
        description = "Automates wine making",
        enabledByDefault = false
)
@Extension
public class AutoWines extends Script {
    @Inject
    private Client client;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private AutoWinesOverlay autoWinesOverlay;
    private int winesMade;

    @Override
    public void onStart(String... strings) {
        WineTracker.onItemContainerChanged(null);
    }

    @Override
    protected void startUp() {
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
    protected int loop() {
        if (!fermentWine()) {
            return withdrawIngredients();
        }

        return ActionDelayHelper.shortReturn();
    }

    private int withdrawIngredients() {
        AutoWinesOverlayHelper.currentState = "Banking..";

        InventoryLoadout invyLoadout = new InventoryLoadout();
        invyLoadout.addItem(ItemID.GRAPES, 14);
        invyLoadout.addItem(ItemID.JUG_OF_WATER, 14);
        if (invyLoadout.fulfilled() && Bank.isOpen()) {
            Bank.close();
            return -1;
        }

        invyLoadout.fulfill();

        return ActionDelayHelper.shortReturn();
    }

    private Widget getMultiSkillMenu() {
        return Widgets.get(WidgetInfo.MULTI_SKILL_MENU);
    }

    private boolean isMultiSkillMenuOpen() {
        Widget w = getMultiSkillMenu();

        return w != null && w.isVisible();
    }

    private boolean fermentWine() {
        AutoWinesOverlayHelper.currentState = "Making wines..";

        Item grapes = Inventory.getFirst(ItemID.GRAPES);
        Item jugs = Inventory.getFirst(ItemID.JUG_OF_WATER);
        if (grapes == null || jugs == null) {
            return false;
        }
        if (isMultiSkillMenuOpen()) {
            Keyboard.type(" ");

            return !Time.sleepUntil(() ->
                    !Inventory.contains(ItemID.GRAPES, ItemID.JUG_OF_WATER),
                    () -> Players.getLocal().isAnimating(),
                    100,
                    4000);
        }

        grapes.useOn(jugs);

        Time.sleepUntil(this::isMultiSkillMenuOpen, 100, 3000);

        return true;
    }

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event) {
        for (Map.Entry<Integer, Integer> i : WineTracker.onItemContainerChanged(event).entrySet()) {
            int id = i.getKey();

            if (id == ItemID.GRAPES && !Bank.isOpen()) {
                winesMade++;

                AutoWinesOverlayHelper.wineAmount = winesMade;
            }
        }
    }

    @Override
    protected void shutDown() throws Exception {
        overlayManager.remove(autoWinesOverlay);

        winesMade = 0;
        AutoWinesOverlayHelper.wineAmount = 0;

        super.shutDown();
    }
}
