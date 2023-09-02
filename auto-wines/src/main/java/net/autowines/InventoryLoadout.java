package net.autowines;

import lombok.extern.slf4j.Slf4j;
import net.autowines.loadout.BankHelper;
import net.autowines.loadout.LoadoutItem;
import net.runelite.api.Item;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.items.Bank;

import java.util.*;

@Slf4j
public class InventoryLoadout {
    private List<LoadoutItem> items;
    public static Queue<LoadoutItem> itemQueue = new LinkedList<>();

    public InventoryLoadout(LoadoutItem... items) {
        this.items = new ArrayList<>(Arrays.asList(items));
    }

    public void addItem(LoadoutItem... loadoutItem) {
        items.addAll(Arrays.asList(loadoutItem));
    }
    public void addItem(int id, int qty) {
        items.add(new LoadoutItem(id,qty));
    }

    public boolean fulfilled() {
        // check extra items
        List<LoadoutItem> extraEquipmentItems = getExtraItems();
        if (!extraEquipmentItems.isEmpty()) {
            return false;
        }

        // check missing items
        for (LoadoutItem item : items) {
            Item invyItem = Bank.Inventory.getFirst(item.getId());

            if (invyItem == null
                    || invyItem.getId() == -1
                    || invyItem.getName() == null
                    || invyItem.getName().equalsIgnoreCase("null")) {
                return false;
            }

            if (invyItem.isNoted() != item.isNoted()) {
                return false;
            }

            int currentQuantity = Bank.Inventory.getCount(true, item.getId());
            int neededQuantity = item.getItemQty() - currentQuantity;
            if (neededQuantity > 0) {
                return false;
            }
        }

        itemQueue.clear();

        return true;
    }
    public boolean fulfill() {
        log.debug("invy fulfill");
        if (!Bank.isOpen()) {
            Time.sleep(BankHelper.clickLocalBank());

            return true;
        }
        List<LoadoutItem> extraItems = getExtraItems();
        if (!extraItems.isEmpty()) {
            log.debug("Found extra items, depositing all extra inventory");
            for (LoadoutItem l : extraItems) {
                BankHelper.depositItem(l.getId(), l.getItemQty());
                Time.sleep(120, 300);
            }

            itemQueue.clear();

            return true;
        }

        boolean neededSomething = false;
        for (LoadoutItem item : items) {
            int currentQuantity = Bank.Inventory.getCount(item.getId());
            int neededQuantity = item.getItemQty() - currentQuantity;
            if (neededQuantity > 0) {
                if (Bank.getCount(true, item.getId()) <= 0 && currentQuantity <= 0) {
                    log.debug("Not have any of item in bank or inventory: " +item.getId());

                    return false;
                }
                neededSomething = true;
                log.debug("add invy item to queue: "+item.getId() + ", "+item.getItemQty());
                BankHelper.bankWithdraw(item.getId(), neededQuantity, (item.isNoted() ? Bank.WithdrawMode.NOTED : Bank.WithdrawMode.ITEM));
            }
        }
        if (!neededSomething) {
            log.debug("InventoryLoadout.fulfill() has nothing missing and nothing extra from loadout, fulfilled inventory!");

            return true;
        }
        log.debug("invy fulfill return after needing something to withdraw");
        Time.sleepTick();

        return true;
    }
    public List<LoadoutItem> getExtraItems() {
        log.debug("getExtraItems Inventory");
        List<LoadoutItem> extraItems = new ArrayList<>();

        // Create a map to hold the total quantity of each item ID in the inventory
        Map<Integer, Integer> inventoryCounts = new HashMap<>();
        for (Item current : Bank.Inventory.getAll()) {
            inventoryCounts.merge(current.getId(), current.getQuantity(), Integer::sum);
        }

        for (Map.Entry<Integer, Integer> entry : inventoryCounts.entrySet()) {
            boolean isIdFoundInDesired = false;
            int desiredQty = 0;
            for (LoadoutItem desired : items) {
                int compareId = (desired.isNoted() ? desired.getNotedId() : desired.getId());
                if (entry.getKey() == compareId) {
                    desiredQty = desired.getItemQty();
                    isIdFoundInDesired = true;
                    break;
                }
            }
            if (!isIdFoundInDesired || entry.getValue() > desiredQty) {
                int extraQty = entry.getValue() - (isIdFoundInDesired ? desiredQty : 0);
                log.debug("Found extra item: "+entry.getKey()+ " in extra qty: "+ extraQty);
                extraItems.add(new LoadoutItem(entry.getKey(), extraQty));
            }
        }

        return extraItems;
    }

}

