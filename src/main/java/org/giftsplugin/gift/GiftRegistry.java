package org.giftsplugin.gift;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public final class GiftRegistry {

    private final Plugin plugin;
    private final File dataFile;
    private final Map<Integer, Gift> gifts = new ConcurrentHashMap<>();
    private final AtomicInteger idCounter = new AtomicInteger(1);

    public GiftRegistry(Plugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "gifts.yml");
    }

    public void load() {
        gifts.clear();

        if (!dataFile.exists()) {
            idCounter.set(1);
            return;
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(dataFile);
        int maxId = 0;

        for (String key : yaml.getKeys(false)) {
            if (key.equals("next-id")) continue;

            try {
                var section = yaml.getConfigurationSection(key);
                if (section == null) continue;

                Gift gift = Gift.deserialize(section.getValues(false));
                gifts.put(gift.id(), gift);
                maxId = Math.max(maxId, gift.id());
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to load gift: " + key, e);
            }
        }

        idCounter.set(yaml.getInt("next-id", maxId + 1));
    }

    public void save() {
        File parent = dataFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        YamlConfiguration yaml = new YamlConfiguration();

        for (Gift gift : gifts.values()) {
            yaml.createSection(String.valueOf(gift.id()), gift.serialize());
        }

        yaml.set("next-id", idCounter.get());

        try {
            yaml.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save gifts", e);
        }
    }

    public Gift register(Gift.Builder builder) {
        int id = idCounter.getAndIncrement();
        Gift gift = builder.id(id).build();
        gifts.put(id, gift);
        save();
        return gift;
    }

    public Optional<Gift> findById(int id) {
        return Optional.ofNullable(gifts.get(id));
    }

    public Optional<Gift> findAtLocation(Location location) {
        return gifts.values().stream()
                .filter(gift -> gift.matchesLocation(location))
                .findFirst();
    }

    public Collection<Gift> all() {
        return gifts.values();
    }

    public void update(Gift gift) {
        save();
    }

    public void remove(int id) {
        gifts.remove(id);
        save();
    }

    public int count() {
        return gifts.size();
    }
}
