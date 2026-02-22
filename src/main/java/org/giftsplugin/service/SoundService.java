package org.giftsplugin.service;

import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.giftsplugin.config.PluginConfig;

public final class SoundService {

    private final PluginConfig config;

    public SoundService(PluginConfig config) {
        this.config = config;
    }

    public void playPlace(Player player) {
        play(player, "place");
    }

    public void playFind(Player player) {
        play(player, "find");
    }

    public void playFindSuper(Player player) {
        play(player, "find-super");
    }

    public void playBreak(Player player) {
        play(player, "break");
    }

    public void playMenuOpen(Player player) {
        play(player, "menu-open");
    }

    public void playMenuClick(Player player) {
        play(player, "menu-click");
    }

    public void playReceive(Player player) {
        play(player, "receive");
    }

    public void playTeleport(Player player) {
        play(player, "teleport");
    }

    public void playError(Player player) {
        play(player, "error");
    }

    private void play(Player player, String key) {
        String path = "sounds." + key;

        if (!config.isSoundEnabled(path)) return;

        Sound sound = config.getSound(path);
        if (sound == null) return;

        float volume = config.getSoundVolume(path);
        float pitch = config.getSoundPitch(path);

        player.playSound(player.getLocation(), sound, volume, pitch);
    }
}
