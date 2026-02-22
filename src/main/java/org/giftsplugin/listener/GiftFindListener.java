package org.giftsplugin.listener;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.giftsplugin.gift.Gift;
import org.giftsplugin.gift.GiftRegistry;
import org.giftsplugin.gift.GiftType;
import org.giftsplugin.reward.RewardHandler;
import org.giftsplugin.service.FireworkService;
import org.giftsplugin.service.MessageService;
import org.giftsplugin.service.SoundService;

public final class GiftFindListener implements Listener {

    private final Plugin plugin;
    private final GiftRegistry registry;
    private final MessageService messages;
    private final SoundService sounds;
    private final RewardHandler rewards;
    private final FireworkService fireworks;

    public GiftFindListener(Plugin plugin, GiftRegistry registry, MessageService messages,
                            SoundService sounds, RewardHandler rewards, FireworkService fireworks) {
        this.plugin = plugin;
        this.registry = registry;
        this.messages = messages;
        this.sounds = sounds;
        this.rewards = rewards;
        this.fireworks = fireworks;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onFind(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.PLAYER_HEAD) return;

        registry.findAtLocation(block.getLocation()).ifPresent(gift -> {
            event.setCancelled(true);
            processFind(event.getPlayer(), gift, block);
        });
    }

    private void processFind(Player player, Gift gift, Block block) {
        if (gift.isFound()) {
            sounds.playError(player);
            messages.send(player, "already-found");
            return;
        }

        gift.markFound(player.getUniqueId(), player.getName());
        registry.update(gift);

        Location center = block.getLocation().add(0.5, 0.5, 0.5);
        fireworks.launch(center);

        new BukkitRunnable() {
            @Override
            public void run() {
                block.setType(Material.AIR);
            }
        }.runTaskLater(plugin, 5L);

        rewards.give(player, gift.type());

        if (gift.type() == GiftType.SUPER) {
            sounds.playFindSuper(player);
            messages.send(player, "super-gift-found");
        } else {
            sounds.playFind(player);
            messages.send(player, "gift-found");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFireworkDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Firework firework)) return;

        if (firework.hasMetadata(FireworkService.SAFE_FIREWORK_TAG)) {
            event.setCancelled(true);
        }
    }
}
