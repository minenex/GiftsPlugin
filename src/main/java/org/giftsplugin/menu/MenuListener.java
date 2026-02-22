package org.giftsplugin.menu;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.giftsplugin.gift.Gift;
import org.giftsplugin.service.MessageService;
import org.giftsplugin.service.SoundService;

public final class MenuListener implements Listener {

    private final MessageService messages;
    private final SoundService sounds;

    public MenuListener(MessageService messages, SoundService sounds) {
        this.messages = messages;
        this.sounds = sounds;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof GiftMenuHolder holder)) return;

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        Integer giftId = clicked.getItemMeta().getPersistentDataContainer()
                .get(holder.menu().giftIdKey(), PersistentDataType.INTEGER);

        if (giftId == null) return;

        sounds.playMenuClick(player);
        holder.menu().registry().findById(giftId).ifPresent(gift -> teleportToGift(player, gift));
    }

    private void teleportToGift(Player player, Gift gift) {
        if (!player.hasPermission("gifts.teleport")) {
            sounds.playError(player);
            messages.send(player, "no-permission");
            return;
        }

        Location location = gift.location();
        if (location == null) return;

        player.closeInventory();
        location.add(0.5, 1, 0.5);
        player.teleport(location);
        sounds.playTeleport(player);
        messages.send(player, "teleported", "{id}", String.valueOf(gift.id()));
    }
}
