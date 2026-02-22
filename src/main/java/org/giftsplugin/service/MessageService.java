package org.giftsplugin.service;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.giftsplugin.config.PluginConfig;
import org.giftsplugin.config.Text;

public final class MessageService {

    private final PluginConfig config;

    public MessageService(PluginConfig config) {
        this.config = config;
    }

    public void sendTo(CommandSender sender, String key) {
        sender.sendMessage(Text.color(config.prefix() + config.message(key)));
    }

    public void send(Player player, String key) {
        player.sendMessage(format(key));
    }

    public void send(Player player, String key, String placeholder, String value) {
        String raw = config.prefix() + config.message(key);
        player.sendMessage(Text.color(raw.replace(placeholder, value)));
    }

    public void sendRaw(Player player, String key) {
        player.sendMessage(Text.color(config.message(key)));
    }

    public Component format(String key) {
        return Text.color(config.prefix() + config.message(key));
    }
}
