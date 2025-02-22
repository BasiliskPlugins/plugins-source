package net.autopumper;

import com.google.inject.Inject;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.autopumper.overlay.AutoPumperOverlay;
import net.autopumper.overlay.AutoPumperOverlayHelper;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileItems;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.movement.Reachable;
import org.pf4j.Extension;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static net.runelite.api.AnimationID.IDLE;

@Extension
@PluginDescriptor(
        name = "Auto Pumper",
        description = "Automates Blast Furnace Pump",
        enabledByDefault = false
)
@Slf4j
public class AutoPumper extends Plugin {

    private static final int BLAST_FURNACE_PUMPING = 2432;

    @Inject
    private Client client;
    @Inject
    private AutoPumperConfig autoPumperConfig;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private AutoPumperOverlay autoPumperOverlay;

    private Instant lastAnimating = Instant.now();
    private int lastAnimation = 0;

    private Actor lastInteract;

    private boolean isEnabled;

    private int xpCounter;
    private int previousXp;

    private boolean inProgress;
    private AutoPumperState state;


    private final ExecutorService executor = Executors.newFixedThreadPool(1);

    @Provides
    AutoPumperConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(AutoPumperConfig.class);
    }

    @Override
    protected void startUp() throws Exception {
        isEnabled = true;

        if (client.getGameState() == GameState.LOGGED_IN) {
            AutoPumperOverlayHelper.timeBegan = System.currentTimeMillis();
            if (overlayManager != null) {
                overlayManager.add(autoPumperOverlay);
            }

            super.startUp();

            Player local = client.getLocalPlayer();
            if (isIdling(local)) {
                if (autoPumperConfig.soloPump()) {
                    executor.submit(() -> {
                        inProgress = true;
                        refuel(local);
                        Time.sleep(32, 98);

                        operatePump(local);

                        inProgress = false;
                    });
                } else {
                    inProgress = true;

                    executor.submit(() -> operatePump(local));

                    inProgress = false;
                }
            }
        }
    }

    @Subscribe
    public void onAnimationChanged(AnimationChanged event) {
        if (client.getGameState() != GameState.LOGGED_IN) {
            return;
        }

        Player localPlayer = client.getLocalPlayer();
        if (localPlayer != event.getActor()) {
            return;
        }

        int animation = localPlayer.getAnimation();
        switch (animation) {
            case IDLE:
                lastAnimating = Instant.now();
                break;

            case BLAST_FURNACE_PUMPING:
                lastAnimation = animation;
                lastAnimating = Instant.now();
                break;

            default:
                lastAnimation = IDLE;
                lastAnimating = null;
        }
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (!isEnabled) {
            return;
        }

        final Player local = client.getLocalPlayer();
        if (client.getGameState() != GameState.LOGGED_IN || local == null) {
            resetTimers();

            return;
        }

        if (!inProgress) {

            if (state == AutoPumperState.PUMPING) {
                if (previousXp == client.getSkillExperience(Skill.STRENGTH)) {
                    xpCounter++;

                    if (xpCounter >= 5) {
                        state = AutoPumperState.FILLING_COKE;

                        xpCounter = 0;
                    }
                }

                previousXp = client.getSkillExperience(Skill.STRENGTH);
            }

            if (state == AutoPumperState.FILLING_COKE && autoPumperConfig.soloPump()) {
                executor.submit(() -> {
                    inProgress = true;

                    refuel(local);
                    Time.sleep(32, 98);

                    operatePump(local);

                    inProgress = false;
                });
            }

            if (!autoPumperConfig.soloPump()) {
                inProgress = true;

                executor.submit(() -> operatePump(local));

                inProgress = false;
            }
        }
    }

    private boolean isIdling(Player local) {
        if (lastAnimation == IDLE) {
            return false;
        }

        final int animation = local.getAnimation();
        if (animation == IDLE) {
            if (lastAnimating != null && Instant.now().compareTo(lastAnimating) >= 0) {
                lastAnimation = IDLE;
                lastAnimating = null;

                lastInteract = null;

                return true;
            }
        } else {
            lastAnimating = Instant.now();
        }

        return false;
    }

    private void resetTimers() {
        final Player local = client.getLocalPlayer();
        lastAnimating = null;
        if (client.getGameState() == GameState.LOGIN_SCREEN || local == null || local.getAnimation() != lastAnimation) {
            lastAnimation = IDLE;
        }

        if (client.getGameState() == GameState.LOGIN_SCREEN || local == null || local.getInteracting() != lastInteract) {
            lastInteract = null;
        }
    }

    private void refuel(Player local) {
        if (Inventory.getCount("Spade") < 10) {
            collectSpades(local);
            fillSpades(local);

            return;
        } else {
            fillSpades(local);
        }

        if (Inventory.getCount("Spadeful of coke") == 10) {
            refuelStove();
        }
    }

    private void refuelStove() {
        AutoPumperOverlayHelper.currentState = "Refuelling.";

        while (Inventory.getCount("Spade") != 10 && isEnabled) {
            TileObject stove = TileObjects.getNearest("Stove");
            if (stove == null) {
                return;
            }

            boolean standingNextToStove = Reachable.getInteractable(stove).contains(Players.getLocal().getWorldLocation());

            stove.interact("Refuel");
            if (!standingNextToStove) {
                waitUntilArrivedAt(stove);
            }

            Time.sleep(21, 112);
        }
    }

    private void collectSpades(Player local) {
        AutoPumperOverlayHelper.currentState = "Collecting 10 spades..";

        while (Inventory.getCount("Spade") != 10) {
            TileItem spade = TileItems.getNearest("Spade");
            if (spade == null) {
                continue;
            }

            if (!Reachable.isInteractable(spade.getTile())) {
                Movement.walkTo(spade.getTile().getWorldLocation());

                Time.sleepUntil(() -> Reachable.isInteractable(spade.getTile()), local::isMoving, 100, 9000);
            }

            spade.pickup();
            waitUntilArrivedAt(spade);

            if (Inventory.getCount("Spade") == 10) {
                return;
            }

            Time.sleep(60000);
        }
    }

    private void fillSpades(Player local) {
        AutoPumperOverlayHelper.currentState = "Filling spades..";

        TileObject coke = TileObjects.getNearest("Coke");
        if (coke == null) {
            return;
        }

        while (Inventory.getCount("Spade") != 0) {
            coke.interact("Collect");
            waitUntilArrivedAt(coke);

            Time.sleep(32, 89);
        }
    }

    private void operatePump(Player player) {
        TileObject pump = TileObjects.getNearest(9090);
        if (pump == null) {
            return;
        }

        boolean standingNextToPump = Reachable.getInteractable(pump).contains(Players.getLocal().getWorldLocation());
        if (!standingNextToPump) {
            locatePump();

            return;
        }

        int prevXp = client.getSkillExperience(Skill.STRENGTH);

        pump.interact(0);

        Time.sleepUntil(() -> player.getAnimation() == BLAST_FURNACE_PUMPING, () -> player.getAnimation() == IDLE, 100, 7000);
        Time.sleepUntil(() -> client.getSkillExperience(Skill.STRENGTH) > prevXp, 5000);

        previousXp = prevXp;

        AutoPumperOverlayHelper.currentState = "Pumping..";
        state = AutoPumperState.PUMPING;
    }

    private void waitUntilArrivedAt(TileObject tileObject) {
        List<WorldPoint> interactableTiles = Reachable.getInteractable(tileObject);
        Time.sleepUntil(() -> interactableTiles.contains(Players.getLocal().getWorldLocation()), () -> Players.getLocal().isMoving(), 100, 7000);
    }

    private void waitUntilArrivedAt(TileItem tileItem) {
        List<WorldPoint> interactableTiles = Reachable.getInteractable(tileItem);
        Time.sleepUntil(() -> interactableTiles.contains(Players.getLocal().getWorldLocation()), () -> Players.getLocal().isMoving(), 100, 7000);
    }

    private void locatePump() {
        AutoPumperOverlayHelper.currentState = "Locating pump..";

        Time.sleepTick();

        TileObject pump = TileObjects.getNearest("Pump");
        if (pump == null) {
            return;
        }

        int prevXp = client.getSkillExperience(Skill.STRENGTH);

        pump.interact("Operate");
        waitUntilArrivedAt(pump);
        Time.sleepUntil(() -> client.getSkillExperience(Skill.STRENGTH) > prevXp, 5000);

        previousXp = prevXp;

        AutoPumperOverlayHelper.currentState = "Pumping..";
        state = AutoPumperState.PUMPING;
    }

    @Override
    protected void shutDown() throws Exception {
        isEnabled = false;

        super.shutDown();
    }
}
