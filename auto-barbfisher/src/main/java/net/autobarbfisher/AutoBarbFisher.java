package net.autobarbfisher;

import com.google.inject.Inject;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.autobarbfisher.overlay.AutoBarbFisherOverlay;
import net.autobarbfisher.overlay.AutoBarbFisherOverlayHelper;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Reachable;
import org.pf4j.Extension;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BooleanSupplier;

@Extension
@PluginDescriptor(
        name = "Auto Barb Fisher",
        description = "Automates 3T Barbarian Fishing",
        enabledByDefault = false
)
@Slf4j
public class AutoBarbFisher extends Plugin {

    @Inject
    private Client client;
    @Inject
    private Notifier notifier;
    @Inject
    private OverlayManager overlayManager;

    @Inject
    private AutoBarbFisherConfig config;
    @Inject
    private AutoBarbFisherOverlay autoBarbFisherOverlay;

    private boolean inProgress;

    private final ExecutorService executor = Executors.newFixedThreadPool(1);

    FishingState state = FishingState.Idle;

    @Provides
    private AutoBarbFisherConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(AutoBarbFisherConfig.class);
    }

    @Override
    protected void startUp() {
        if (client.getGameState() == GameState.LOGGED_IN) {
            AutoBarbFisherOverlayHelper.expstarted = client.getSkillExperience(Skill.FISHING);
            AutoBarbFisherOverlayHelper.startinglevel = client.getRealSkillLevel(Skill.FISHING);
            AutoBarbFisherOverlayHelper.timeBegan = System.currentTimeMillis();
            if (overlayManager != null) {
                overlayManager.add(autoBarbFisherOverlay);
            }
        }
    }

    @Subscribe
    public void onGameTick(GameTick gameTick) {
        if (!inProgress) {
            switch (state) {
                case UseGuam:
                    AutoBarbFisherOverlayHelper.currentState = "Fishing..";
                    executor.submit(this::skipTick);
                    break;
                case UseTarAndDrop:
                    AutoBarbFisherOverlayHelper.currentState = "Fishing..";
                    executor.submit(this::useTarAndDropFish);
                    break;
                case ClickFishingSpot:
                case Idle:
                    AutoBarbFisherOverlayHelper.currentState = "Fishing..";
                    executor.submit(this::clickFishingSpot);
                    break;
                case LocatingFishingSpot:
                    AutoBarbFisherOverlayHelper.currentState = "Locating fishing spot..";
                    break;
                default:
                    notifier.notify("ThreeTickBarb stopped unexpectedly!");
                    break;
            }
        }
    }

    private void skipTick() {
        inProgress = true;

        state = FishingState.UseTarAndDrop;

        inProgress = false;
    }

    private void useTarAndDropFish() {
        inProgress = true;
        Item guamLeaf = Inventory.getFirst("Guam leaf");
        if (guamLeaf == null) {
            return;
        }

        Item swampTar = Inventory.getFirst("Swamp tar");
        if (swampTar == null) {
            return;
        }

        boolean randomizeDrop = Inventory.getCount("Leaping trout") == random(1, 5);
        if (randomizeDrop || Inventory.isFull()) {
            AutoBarbFisherOverlayHelper.currentState = "Dropping fish..";

            dropFish("Leaping trout");
            dropFish("Leaping salmon");
            dropFish("Leaping sturgeon");
        }

        guamLeaf.useOn(swampTar);
        state = FishingState.ClickFishingSpot;

        inProgress = false;
    }

    private void dropFish(String fishName) {
        List<Item> fishInInventory = Inventory.getAll(fishName);
        if (fishInInventory.isEmpty()) {
            return;
        }

        Random random = new Random();
        int minDrop = 1;
        int maxDrop = fishInInventory.size();
        int numToDrop = random.nextInt(maxDrop - minDrop + 1) + minDrop;

        for (int i = 0; i < numToDrop; i++) {
            int randomIndex = random.nextInt(fishInInventory.size());
            Item itemToDrop = fishInInventory.get(randomIndex);
            itemToDrop.drop();
        }
    }

    private void clickFishingSpot() {
        inProgress = true;

        NPC fishingSpot = NPCs.getNearest("Fishing spot");
        if (fishingSpot == null) {
            return;
        }

        boolean standingNextToFishingSpot = Reachable.getInteractable(fishingSpot).contains(Players.getLocal().getWorldLocation());
        if (!standingNextToFishingSpot) {
            AutoBarbFisherOverlayHelper.currentState = "Locating fishing spot..";
            state = FishingState.LocatingFishingSpot;

            locateFishingSpot(fishingSpot);

            return;
        }

        fishingSpot.interact("Use-rod");
        state = FishingState.UseGuam;

        inProgress = false;
    }

    private void locateFishingSpot(NPC fishingSpot) {
        inProgress = true;

        fishingSpot.interact("Use-rod");
        waitUntilArrivedAtFishingSpot(fishingSpot);

        state = FishingState.UseGuam;

        inProgress = false;
    }

    private void waitUntilArrivedAtFishingSpot(NPC fishingSpot) {
        List<WorldPoint> interactableTiles = Reachable.getInteractable(fishingSpot);
        Time.sleepUntil(() -> interactableTiles.contains(Players.getLocal().getWorldLocation()), () -> Players.getLocal().isMoving(), 100, 4000);
    }

    private int random(final int min, final int max) {
        final int n = Math.abs(max - min);
        return Math.min(min, max) + (n == 0 ? 0 : new java.util.Random().nextInt(n));
    }

    @Override
    protected void shutDown() {
        state = FishingState.Idle;
        overlayManager.remove(autoBarbFisherOverlay);
    }
}