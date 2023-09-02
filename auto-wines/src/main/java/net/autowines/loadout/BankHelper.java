package net.autowines.loadout;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Item;
import net.runelite.api.NPC;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.unethicalite.api.commons.Rand;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.game.Worlds;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.movement.Reachable;
import net.unethicalite.api.movement.pathfinder.model.BankLocation;
import net.unethicalite.api.widgets.Dialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Slf4j
public class BankHelper {
    public static BankLocation[] f2pBanks = {
            BankLocation.AL_KHARID_BANK,
            BankLocation.DRAYNOR_BANK,
            BankLocation.LUMBRIDGE_BANK,
            BankLocation.EDGEVILLE_BANK,
            BankLocation.FALADOR_EAST_BANK,
            BankLocation.FALADOR_WEST_BANK,
            BankLocation.VARROCK_WEST_BANK,
            BankLocation.VARROCK_EAST_BANK,
            BankLocation.GRAND_EXCHANGE_BANK,
            BankLocation.DUEL_ARENA_BANK,
            BankLocation.FEROX_ENCLAVE_BANK
    };

    private static int walkingRandomPoint = 6;

    public static int withdrawItemFromBank(String name, int quantity) {
        if (!Bank.isOpen()) {
            return clickLocalBank();
        }

        if (Inventory.getCount(name) < quantity) {
            int toWithdrawQty = quantity - Inventory.getCount(name);
            bankWithdraw(name, toWithdrawQty, Bank.WithdrawMode.ITEM);
        }

        return ActionDelayHelper.returnTick();
    }

    public static int withdrawItemFromBank(int id, int quantity) {
        if (!Bank.isOpen()) {
            return clickLocalBank();
        }

        if (Inventory.getCount(id) < quantity) {
            int toWithdrawQty = quantity - Inventory.getCount(id);
            bankWithdraw(id, toWithdrawQty, Bank.WithdrawMode.ITEM);
        }

        return ActionDelayHelper.returnTick();
    }

    public static int clickLocalBank() {
        if (Worlds.getCurrentWorld().isMembers()) {
            return clickBank(BankLocation.getNearest());
        }

        BankLocation nearest = Arrays.stream(f2pBanks)
                .min(Comparator.comparingInt(x -> x.getArea().distanceTo2D(Players.getLocal().getWorldLocation())))
                .orElse(null);

        return clickBank(nearest);
    }

    public static int clickBank(BankLocation location) {
        if (!shouldWalk()) {
            return ActionDelayHelper.shortReturn();
        }

        TileObject booth = TileObjects.getNearest(b -> b.hasAction("Collect") && (b.hasAction("Bank") || b.hasAction("Use")) && location.getArea().distanceTo(b) < 5);

        if (booth != null) {
            if (booth.distanceTo(Players.getLocal()) >= 20 || !Reachable.isInteractable(booth)) {
                log.info("Walking towards booth");
                if (shouldWalk()) {
                    walkTo(booth.getWorldArea());
                }

                return ActionDelayHelper.returnTick();
            }
            log.info("Clicking Booth");
            String action = (booth.hasAction("Bank") ? "Bank" : "Use");
            if (shouldWalk()) {
                booth.interact(action);
                Time.sleepUntil(() -> Bank.isOpen(), () -> Players.getLocal().isMoving(), 100, 3000);
            }

            return ActionDelayHelper.returnTick();
        }
        NPC banker = NPCs.getNearest(b -> b.hasAction("Collect") && b.hasAction("Bank") && location.getArea().distanceTo(b) < 5);
        if (banker != null) {
            if (banker.distanceTo(Players.getLocal()) < 20 && Reachable.isInteractable(banker)) {
                log.info("Clicking Banker");
                banker.interact("Bank");
                Time.sleepUntil(() -> Bank.isOpen(), () -> Players.getLocal().isMoving(), 100, 3000);

                return ActionDelayHelper.shortReturn();
            }

            log.info("Walking towards banker");
            if (shouldWalk()) {
                walkTo(banker.getWorldArea());
            }

            return ActionDelayHelper.returnTick();
        }
        log.info("Walking towards bank");
        if (shouldWalk()) {
            walkTo(location.getArea());
        }

        return ActionDelayHelper.returnTick();
    }

    public static void depositAllExcept(int... ids) {
        List<Integer> itemsDeposited = new ArrayList<>();
        for (Item i : Inventory.getAll(i -> i != null && i.getName() != null && i.getId() > 0 && !i.getName().equalsIgnoreCase("null") && !Arrays.stream(ids).anyMatch(exceptId -> exceptId == i.getId()))) {
            if (ActionDelayHelper.stopScript) {
                break;
            }
            if (i == null || itemsDeposited.stream().anyMatch(i2 -> i2.intValue() == i.getId())) continue;
            itemsDeposited.add(i.getId());
            log.info("deposit some other items: " + i.getName());
            Bank.depositAll(i.getId());
            ActionDelayHelper.shortSleep();
        }
    }

    public static void depositAllExcept(String... names) {
        List<Integer> itemsDeposited = new ArrayList<>();
        for (Item i : Inventory.getAll(i -> i != null && i.getName() != null && i.getId() > 0 && !i.getName().equalsIgnoreCase("null") && !Arrays.stream(names).anyMatch(exceptName -> exceptName.equals(i.getName())))) {
            if (ActionDelayHelper.stopScript) {
                break;
            }
            if (i == null || itemsDeposited.stream().anyMatch(i2 -> i2.intValue() == i.getId())) continue;
            itemsDeposited.add(i.getId());
            log.info("deposit some other items: " + i.getName());
            Bank.depositAll(i.getId());
            ActionDelayHelper.shortSleep();
        }
    }

    private static String getAction(Item item, int amount, Boolean withdraw) {
        String action = withdraw ? "Withdraw" : "Deposit";
        if (amount == 1) {
            action = action + "-1";
        } else if (amount == 5) {
            action = action + "-5";
        } else if (amount == 10) {
            action = action + "-10";
        } else if (withdraw && amount >= item.getQuantity()) {
            action = action + "-All";
        } else if (!withdraw && amount >= Bank.Inventory.getCount(true, item.getId())) {
            action = action + "-All";
        } else if (item.hasAction(action + "-" + amount)) {
            action = action + "-" + amount;
        } else {
            action = action + "-X";
        }

        return action;
    }

    public static void bankWithdraw(int id, int qty, Bank.WithdrawMode withdrawMode) {
        Item item = Bank.getFirst((x) -> x.getId() == id && !x.isPlaceholder());
        if (item != null) {
            String action = getAction(item, qty, true);
            int actionIndex = item.getActionIndex(action);
            if (withdrawMode == Bank.WithdrawMode.NOTED && !Bank.isNotedWithdrawMode()) {
                Bank.setWithdrawMode(true);
                Time.sleepUntil(Bank::isNotedWithdrawMode, 1200);
            }

            if (withdrawMode == Bank.WithdrawMode.ITEM && Bank.isNotedWithdrawMode()) {
                Bank.setWithdrawMode(false);
                Time.sleepUntil(() -> {
                    return !Bank.isNotedWithdrawMode();
                }, 1200);
            }

            item.interact(actionIndex + 1);
            if (action.equals("Withdraw-X")) {
                Dialog.enterAmount(qty);
                Time.sleepTick();
            }

        }
    }

    public static void bankWithdraw(String name, int qty, Bank.WithdrawMode withdrawMode) {
        Item item = Bank.getFirst(x -> x.getName().equalsIgnoreCase(name) && !x.isPlaceholder());
        if (item != null) {
            String action = getAction(item, qty, true);
            int actionIndex = item.getActionIndex(action);
            if (withdrawMode == Bank.WithdrawMode.NOTED && !Bank.isNotedWithdrawMode()) {
                Bank.setWithdrawMode(true);
                Time.sleepUntil(Bank::isNotedWithdrawMode, 1200);
            }

            if (withdrawMode == Bank.WithdrawMode.ITEM && Bank.isNotedWithdrawMode()) {
                Bank.setWithdrawMode(false);
                Time.sleepUntil(() -> {
                    return !Bank.isNotedWithdrawMode();
                }, 1200);
            }

            item.interact(actionIndex + 1);
            if (action.equals("Withdraw-X")) {
                Dialog.enterAmount(qty);
                Time.sleepTick();
            }

        }
    }

    public static void depositItem(int itemID, int quantity) {
        if (Bank.Inventory.getCount(true, itemID) <= 0) {
            log.debug("Missing itemID from invy in queue, skipping: " + itemID);
            return;
        }

        //Withdraw by directly interacting for any clickable-action quantities and return immediately
        Item invyItem = Bank.Inventory.getFirst(itemID);
        int foundActionIndex = -1;
        int currentIndex = 0;
        for (String action : invyItem.getActions()) {
            if (currentIndex <= 0) {
                currentIndex++;
                continue;
            }
            if (action.contains("Deposit-")) {
                if (action.equals("Deposit-X")) {
                    currentIndex++;
                    continue;
                }
                if (action.equals("Deposit-All")) {
                    if (quantity != 1 && quantity != 5 && quantity != 10) {
                        log.debug("Invy count of: " + invyItem.getName() + " not 1, 5, or 10, so withdraw-x");
                        foundActionIndex = currentIndex;
                        break;
                    }
                    currentIndex++;
                    continue;
                }
                int availableActionQty = Integer.parseInt(action.replace("Deposit-", ""));
                if (availableActionQty == quantity) {
                    log.debug("found action index with quantity: " + availableActionQty + " equal to deposit desired qty: " + quantity);
                    foundActionIndex = currentIndex;
                    break;
                }
            }
            currentIndex++;
        }
        if (foundActionIndex > -1) {
            log.info("Interacting action index " + foundActionIndex + " correlating to action: " + invyItem.getActions()[foundActionIndex] + " on item: " + invyItem.getName());
            invyItem.interact(foundActionIndex);
            return;
        }

        //Call API method to handle withdraw-x quantities and wait for finish
        log.debug("Deposit-x of item/qty: " + invyItem.getName() + "/" + quantity);
        Bank.deposit(itemID, quantity);
    }

    private static boolean shouldWalk() {
        WorldPoint walkTileCurrent = Movement.getDestination();
        if (walkTileCurrent == null) {
            return true;
        }
        if (walkTileCurrent.distanceTo(Players.getLocal().getWorldLocation()) <= walkingRandomPoint) {
            return true;
        }

        return !Movement.isWalking();
    }

    private static int walkTo(WorldArea a) {
        if (!shouldWalk()) {
            return ActionDelayHelper.shortReturn();
        }
        ActionDelayHelper.fastSleep();
        Movement.walkTo(a.getRandom());
        walkingRandomPoint = Rand.nextInt(0, 6);
        Time.sleepTick();

        return ActionDelayHelper.shortReturn();
    }
}

