package org.giftsplugin.reward;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.giftsplugin.config.PluginConfig;
import org.giftsplugin.gift.GiftType;

public final class RewardHandler {

    private final PluginConfig config;

    public RewardHandler(PluginConfig config) {
        this.config = config;
    }

    public void give(Player player, GiftType type) {
        giveItems(player, type);
        applyEffects(player, type);
    }

    private void giveItems(Player player, GiftType type) {
        for (ItemStack item : config.rewardItems(type)) {
            if (player.getInventory().firstEmpty() == -1) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            } else {
                player.getInventory().addItem(item);
            }
        }
    }

    private void applyEffects(Player player, GiftType type) {
        for (PotionEffect effect : config.rewardEffects(type)) {
            player.addPotionEffect(effect);
        }
    }
}
