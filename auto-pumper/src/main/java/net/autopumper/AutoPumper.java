package net.autopumper;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileObjects;
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

    private Instant lastAnimating = Instant.now();
    private int lastAnimation = 0;

    private Instant lastInteracting;
    private Actor lastInteract;

    private final ExecutorService executor = Executors.newFixedThreadPool(1);

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
        final Player local = client.getLocalPlayer();
        if (client.getGameState() != GameState.LOGGED_IN || local == null) {
            resetTimers();

            return;
        }

        if (isPumping(local)) {
            return;
        }

        if (isIdling(local)) {
            executor.submit(() -> operatePump(local));
        }
    }

    private boolean isPumping(Player player) {
        return player.getAnimation() == BLAST_FURNACE_PUMPING;
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
                lastInteracting = null;

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

        lastInteracting = null;
        if (client.getGameState() == GameState.LOGIN_SCREEN || local == null || local.getInteracting() != lastInteract) {
            lastInteract = null;
        }
    }

    private void operatePump(Player player) {
        TileObject pump = TileObjects.getNearest(b -> b.hasAction("Operate"));
        if (pump == null) {
            return;
        }

        boolean standingNextToPump = Reachable.getInteractable(pump).contains(Players.getLocal().getWorldLocation());
        if (!standingNextToPump) {
            locatePump(pump);

            return;
        }

        pump.interact("Operate");

        Time.sleepUntil(() -> player.getAnimation() == BLAST_FURNACE_PUMPING, () -> player.getAnimation() == IDLE, 100, 1000);
    }

    private void locatePump(TileObject pump) {
        pump.interact("Operate");

        waitUntiArrivedAtPump(pump);
    }

    private void waitUntiArrivedAtPump(TileObject pump) {
        List<WorldPoint> interactableTiles = Reachable.getInteractable(pump);

        Time.sleepUntil(() -> interactableTiles.contains(Players.getLocal().getWorldLocation()), () -> Players.getLocal().isMoving(), 100, 10000);
    }
}
