package org.giftsplugin.service;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.giftsplugin.config.PluginConfig;

public final class FireworkService {

    public static final String SAFE_FIREWORK_TAG = "gifts_safe_firework";

    private final Plugin plugin;
    private final PluginConfig config;

    public FireworkService(Plugin plugin, PluginConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void launch(Location center) {
        World world = center.getWorld();
        if (world == null) return;

        for (int i = 0; i < 3; i++) {
            int delay = i * 3;

            new BukkitRunnable() {
                @Override
                public void run() {
                    Location spawn = center.clone().add(
                            randomOffset() * 0.5,
                            0,
                            randomOffset() * 0.5
                    );

                    Firework firework = (Firework) world.spawnEntity(spawn, EntityType.FIREWORK_ROCKET);
                    firework.setMetadata(SAFE_FIREWORK_TAG, new FixedMetadataValue(plugin, true));

                    FireworkMeta meta = firework.getFireworkMeta();
                    meta.addEffect(config.fireworkEffect());
                    meta.setPower(0);
                    firework.setFireworkMeta(meta);

                    firework.detonate();
                }
            }.runTaskLater(plugin, delay);
        }
    }

    private double randomOffset() {
        return (Math.random() - 0.5) * 2;
    }
}
