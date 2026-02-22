package org.giftsplugin.command;

import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.giftsplugin.config.PluginConfig;
import org.giftsplugin.gift.GiftRegistry;
import org.giftsplugin.gift.GiftType;
import org.giftsplugin.menu.GiftMenu;
import org.giftsplugin.service.MessageService;
import org.giftsplugin.service.SoundService;

import java.util.List;

public final class GiftCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBCOMMANDS = List.of("give", "supergift", "info", "reload");

    private final PluginConfig config;
    private final GiftRegistry registry;
    private final MessageService messages;
    private final SoundService sounds;
    private final GiftMenu menu;
    private final NamespacedKey giftTypeKey;

    public GiftCommand(PluginConfig config, GiftRegistry registry, MessageService messages,
                       SoundService sounds, GiftMenu menu, NamespacedKey giftTypeKey) {
        this.config = config;
        this.registry = registry;
        this.messages = messages;
        this.sounds = sounds;
        this.menu = menu;
        this.giftTypeKey = giftTypeKey;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            messages.sendTo(sender, "player-only");
            return true;
        }

        if (!player.hasPermission("gifts.use")) {
            sounds.playError(player);
            messages.send(player, "no-permission");
            return true;
        }

        if (args.length == 0) {
            showHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "give" -> giveGift(player, GiftType.NORMAL);
            case "supergift" -> giveGift(player, GiftType.SUPER);
            case "info" -> menu.open(player);
            case "reload" -> reload(player);
            default -> showHelp(player);
        }

        return true;
    }

    private void giveGift(Player player, GiftType type) {
        if (!player.hasPermission("gifts.admin")) {
            sounds.playError(player);
            messages.send(player, "no-permission");
            return;
        }

        ItemStack item = config.createGiftItem(type);
        item.editMeta(meta -> meta.getPersistentDataContainer()
                .set(giftTypeKey, PersistentDataType.STRING, type.name()));

        addToInventory(player, item);
        sounds.playReceive(player);

        String messageKey = type == GiftType.SUPER ? "super-gift-received" : "gift-received";
        messages.send(player, messageKey);
    }

    private void addToInventory(Player player, ItemStack item) {
        if (player.getInventory().firstEmpty() == -1) {
            player.getWorld().dropItemNaturally(player.getLocation(), item);
        } else {
            player.getInventory().addItem(item);
        }
    }

    private void reload(Player player) {
        if (!player.hasPermission("gifts.admin")) {
            sounds.playError(player);
            messages.send(player, "no-permission");
            return;
        }

        config.load();
        registry.save();
        registry.load();
        messages.send(player, "config-reloaded");
    }

    private void showHelp(Player player) {
        messages.sendRaw(player, "help-header");
        messages.sendRaw(player, "help-give");
        messages.sendRaw(player, "help-supergift");
        messages.sendRaw(player, "help-info");
        messages.sendRaw(player, "help-reload");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            return SUBCOMMANDS.stream()
                    .filter(cmd -> cmd.startsWith(input))
                    .toList();
        }
        return List.of();
    }
}
