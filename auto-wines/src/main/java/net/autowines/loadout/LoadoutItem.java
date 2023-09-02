package net.autowines.loadout;

public class LoadoutItem {
    private final int id;
    private final int itemQty;
    private final boolean noted;
    private final int notedId;

    public LoadoutItem(int itemName, int itemQty) {
        this.id = itemName;
        this.itemQty = itemQty;
        this.noted = false;
        this.notedId = 0;
    }

    public LoadoutItem(int itemName, int itemQty, boolean noted, int notedId) {
        this.id = itemName;
        this.itemQty = itemQty;
        this.noted = noted;
        this.notedId = 0;
    }

    public int getId() {
        return id;
    }

    public int getItemQty() {
        return itemQty;
    }

    public boolean isNoted() {
        return noted;
    }
    public int getNotedId() {
        return notedId;
    }

}
