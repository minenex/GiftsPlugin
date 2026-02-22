package org.giftsplugin.listener;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.block.data.Rotatable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.profile.PlayerProfile;
import org.giftsplugin.gift.Gift;
import org.giftsplugin.gift.GiftRegistry;
import org.giftsplugin.gift.GiftType;
import org.giftsplugin.service.MessageService;
import org.giftsplugin.service.SoundService;

public final class GiftPlaceListener implements Listener {

    private final GiftRegistry registry;
    private final MessageService messages;
    private final SoundService sounds;
    private final NamespacedKey giftTypeKey;

    public GiftPlaceListener(GiftRegistry registry, MessageService messages,
                             SoundService sounds, NamespacedKey giftTypeKey) {
        this.registry = registry;
        this.messages = messages;
        this.sounds = sounds;
        this.giftTypeKey = giftTypeKey;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlace(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        GiftType type = extractGiftType(item);
        if (type == null) return;

        event.setCancelled(true);

        Block clicked = event.getClickedBlock();
        if (clicked == null) return;

        Block target = clicked.getRelative(event.getBlockFace());
        if (!target.getType().isAir()) return;

        placeGiftBlock(target, item, player);

        Gift gift = registry.register(Gift.builder()
                .type(type)
                .location(target.getLocation())
                .createdNow()
                .creatorId(player.getUniqueId())
                .creatorName(player.getName()));

        if (player.getGameMode() != GameMode.CREATIVE) {
            item.setAmount(item.getAmount() - 1);
        }

        sounds.playPlace(player);
        messages.send(player, "gift-placed", "{id}", String.valueOf(gift.id()));
    }

    private GiftType extractGiftType(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return null;
        if (!item.hasItemMeta()) return null;

        String value = item.getItemMeta().getPersistentDataContainer()
                .get(giftTypeKey, PersistentDataType.STRING);

        if (value == null) return null;

        try {
            return GiftType.valueOf(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private void placeGiftBlock(Block block, ItemStack source, Player player) {
        block.setType(Material.PLAYER_HEAD);

        BlockFace facing = getPlayerFacing(player);
        if (block.getBlockData() instanceof Rotatable rotatable) {
            rotatable.setRotation(facing);
            block.setBlockData(rotatable);
        }

        if (!(block.getState() instanceof Skull skull)) return;
        if (!(source.getItemMeta() instanceof SkullMeta sourceMeta)) return;

        PlayerProfile profile = sourceMeta.getOwnerProfile();
        if (profile != null) {
            skull.setOwnerProfile(profile);
            skull.update();
        }
    }

    private BlockFace getPlayerFacing(Player player) {
        float yaw = player.getLocation().getYaw();
        yaw = (yaw % 360 + 360) % 360;

        if (yaw >= 348.75 || yaw < 11.25) return BlockFace.SOUTH;
        if (yaw < 33.75) return BlockFace.SOUTH_SOUTH_WEST;
        if (yaw < 56.25) return BlockFace.SOUTH_WEST;
        if (yaw < 78.75) return BlockFace.WEST_SOUTH_WEST;
        if (yaw < 101.25) return BlockFace.WEST;
        if (yaw < 123.75) return BlockFace.WEST_NORTH_WEST;
        if (yaw < 146.25) return BlockFace.NORTH_WEST;
        if (yaw < 168.75) return BlockFace.NORTH_NORTH_WEST;
        if (yaw < 191.25) return BlockFace.NORTH;
        if (yaw < 213.75) return BlockFace.NORTH_NORTH_EAST;
        if (yaw < 236.25) return BlockFace.NORTH_EAST;
        if (yaw < 258.75) return BlockFace.EAST_NORTH_EAST;
        if (yaw < 281.25) return BlockFace.EAST;
        if (yaw < 303.75) return BlockFace.EAST_SOUTH_EAST;
        if (yaw < 326.25) return BlockFace.SOUTH_EAST;
        return BlockFace.SOUTH_SOUTH_EAST;
    }
}
