package org.giftsplugin.menu;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class GiftMenuHolder implements InventoryHolder {

    private final GiftMenu menu;

    public GiftMenuHolder(GiftMenu menu) {
        this.menu = menu;
    }

    public GiftMenu menu() {
        return menu;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
