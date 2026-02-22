package org.giftsplugin.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.giftsplugin.gift.GiftRegistry;
import org.giftsplugin.service.MessageService;
import org.giftsplugin.service.SoundService;

public final class GiftBreakListener implements Listener {

    private final GiftRegistry registry;
    private final MessageService messages;
    private final SoundService sounds;

    public GiftBreakListener(GiftRegistry registry, MessageService messages, SoundService sounds) {
        this.registry = registry;
        this.messages = messages;
        this.sounds = sounds;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.PLAYER_HEAD) return;

        registry.findAtLocation(block.getLocation()).ifPresent(gift -> {
            event.setDropItems(false);
            registry.remove(gift.id());
            sounds.playBreak(event.getPlayer());
            messages.send(event.getPlayer(), "gift-removed", "{id}", String.valueOf(gift.id()));
        });
    }
}
