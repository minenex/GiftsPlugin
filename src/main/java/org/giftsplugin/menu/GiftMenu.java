package org.giftsplugin.menu;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.giftsplugin.config.PluginConfig;
import org.giftsplugin.config.Text;
import org.giftsplugin.gift.Gift;
import org.giftsplugin.gift.GiftRegistry;
import org.giftsplugin.service.SoundService;

public final class GiftMenu {

    private final PluginConfig config;
    private final GiftRegistry registry;
    private final SoundService sounds;
    private final NamespacedKey giftIdKey;

    public GiftMenu(PluginConfig config, GiftRegistry registry, SoundService sounds, NamespacedKey giftIdKey) {
        this.config = config;
        this.registry = registry;
        this.sounds = sounds;
        this.giftIdKey = giftIdKey;
    }

    public void open(Player player) {
        int rows = config.guiRows();
        int size = rows * 9;

        Inventory inventory = Bukkit.createInventory(
                new GiftMenuHolder(this),
                size,
                Text.color(config.guiTitle())
        );

        fillBackground(inventory, size);
        populateGifts(inventory, size);

        player.openInventory(inventory);
        sounds.playMenuOpen(player);
    }

    private void fillBackground(Inventory inventory, int size) {
        ItemStack filler = new ItemStack(config.guiFiller());
        filler.editMeta(meta -> meta.displayName(Text.color(config.guiFillerName())));

        for (int i = 0; i < size; i++) {
            inventory.setItem(i, filler.clone());
        }
    }

    private void populateGifts(Inventory inventory, int size) {
        int slot = 0;

        for (Gift gift : registry.all()) {
            if (slot >= size) break;

            ItemStack item = config.createGiftDisplayItem(gift);
            item.editMeta(meta -> meta.getPersistentDataContainer()
                    .set(giftIdKey, PersistentDataType.INTEGER, gift.id()));

            inventory.setItem(slot++, item);
        }
    }

    public NamespacedKey giftIdKey() {
        return giftIdKey;
    }

    public GiftRegistry registry() {
        return registry;
    }
}
