package org.giftsplugin.config;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.giftsplugin.gift.Gift;
import org.giftsplugin.gift.GiftType;
import org.giftsplugin.util.HeadTexture;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class PluginConfig {

    private final Plugin plugin;
    private FileConfiguration config;

    public PluginConfig(Plugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
        migrate();
    }

    private void migrate() {
        int version = config.getInt("config-version", 0);
        if (version < 1) {
            config.set("config-version", 1);
            plugin.saveConfig();
        }
    }

    public String message(String key) {
        return config.getString("messages." + key, "");
    }

    public String prefix() {
        return config.getString("messages.prefix", "");
    }

    public String guiTitle() {
        return config.getString("messages.gui-title", "Gifts");
    }

    public int guiRows() {
        return config.getInt("gui.rows", 6);
    }

    public Material guiFiller() {
        try {
            return Material.valueOf(config.getString("gui.filler-material", "RED_STAINED_GLASS_PANE"));
        } catch (IllegalArgumentException e) {
            return Material.RED_STAINED_GLASS_PANE;
        }
    }

    public String guiFillerName() {
        return config.getString("gui.filler-name", " ");
    }

    public ItemStack createGiftItem(GiftType type) {
        String path = "gift-item." + type.key();

        Material material = Material.valueOf(config.getString(path + ".material", "PLAYER_HEAD"));
        ItemStack item = new ItemStack(material);

        if (material == Material.PLAYER_HEAD) {
            HeadTexture.apply(item, config.getString(path + ".texture"));
        }

        item.editMeta(meta -> {
            meta.displayName(Text.color(config.getString(path + ".name", "Gift")));
            meta.lore(config.getStringList(path + ".lore").stream()
                    .map(Text::color)
                    .toList());
        });

        return item;
    }

    public ItemStack createGiftDisplayItem(Gift gift) {
        ItemStack item = createGiftItem(gift.type());

        item.editMeta(meta -> {
            String nameKey = gift.type() == GiftType.SUPER
                    ? "gui.gift-display.super-name"
                    : "gui.gift-display.normal-name";

            meta.displayName(Text.color(
                    config.getString(nameKey, "Gift #{id}")
                            .replace("{id}", String.valueOf(gift.id()))
            ));

            String foundStatus = gift.isFound()
                    ? config.getString("gui.gift-display.found-status", "Found by {found_by}")
                            .replace("{found_by}", gift.finderName())
                    : config.getString("gui.gift-display.not-found-status", "Not found");

            List<String> loreTemplate = config.getStringList("gui.gift-display.lore");
            meta.lore(loreTemplate.stream()
                    .map(line -> line
                            .replace("{id}", String.valueOf(gift.id()))
                            .replace("{time}", gift.createdAtFormatted())
                            .replace("{coords}", gift.coordinates())
                            .replace("{world}", gift.world())
                            .replace("{placed_by}", gift.creatorName())
                            .replace("{found_status}", foundStatus)
                            .replace("{found_by}", gift.isFound() ? gift.finderName() : ""))
                    .map(Text::color)
                    .toList());
        });

        return item;
    }

    public List<ItemStack> rewardItems(GiftType type) {
        List<ItemStack> items = new ArrayList<>();
        String path = "rewards." + type.key() + ".items";

        List<?> list = config.getList(path);
        if (list == null) return items;

        for (Object obj : list) {
            if (obj instanceof ConfigurationSection section) {
                items.add(new ItemStack(
                        Material.valueOf(section.getString("material", "DIAMOND")),
                        section.getInt("amount", 1)
                ));
            } else if (obj instanceof Map<?, ?> map) {
                Object amount = map.get("amount");
                items.add(new ItemStack(
                        Material.valueOf((String) map.get("material")),
                        amount != null ? ((Number) amount).intValue() : 1
                ));
            }
        }

        return items;
    }

    public List<PotionEffect> rewardEffects(GiftType type) {
        List<PotionEffect> effects = new ArrayList<>();
        String path = "rewards." + type.key() + ".effects";

        List<?> list = config.getList(path);
        if (list == null) return effects;

        for (Object obj : list) {
            if (obj instanceof Map<?, ?> map) {
                PotionEffectType effectType = PotionEffectType.getByName((String) map.get("type"));
                if (effectType == null) continue;

                Object dur = map.get("duration");
                Object amp = map.get("amplifier");

                effects.add(new PotionEffect(
                        effectType,
                        dur != null ? ((Number) dur).intValue() : 200,
                        amp != null ? ((Number) amp).intValue() : 0
                ));
            }
        }

        return effects;
    }

    public FireworkEffect fireworkEffect() {
        List<Color> colors = config.getStringList("firework.colors").stream()
                .map(this::parseColor)
                .filter(c -> c != null)
                .toList();

        List<Color> fade = config.getStringList("firework.fade-colors").stream()
                .map(this::parseColor)
                .filter(c -> c != null)
                .toList();

        FireworkEffect.Type type;
        try {
            type = FireworkEffect.Type.valueOf(config.getString("firework.type", "BALL_LARGE"));
        } catch (IllegalArgumentException e) {
            type = FireworkEffect.Type.BALL_LARGE;
        }

        return FireworkEffect.builder()
                .withColor(colors.isEmpty() ? List.of(Color.RED) : colors)
                .withFade(fade)
                .with(type)
                .flicker(config.getBoolean("firework.flicker", true))
                .trail(config.getBoolean("firework.trail", true))
                .build();
    }

    public int fireworkPower() {
        return config.getInt("firework.power", 1);
    }

    private Color parseColor(String name) {
        return switch (name.toUpperCase()) {
            case "RED" -> Color.RED;
            case "YELLOW" -> Color.YELLOW;
            case "ORANGE" -> Color.ORANGE;
            case "WHITE" -> Color.WHITE;
            case "BLUE" -> Color.BLUE;
            case "GREEN" -> Color.GREEN;
            case "PURPLE" -> Color.PURPLE;
            case "AQUA" -> Color.AQUA;
            case "LIME" -> Color.LIME;
            case "FUCHSIA" -> Color.FUCHSIA;
            case "SILVER" -> Color.SILVER;
            case "GRAY" -> Color.GRAY;
            case "MAROON" -> Color.MAROON;
            case "OLIVE" -> Color.OLIVE;
            case "NAVY" -> Color.NAVY;
            case "TEAL" -> Color.TEAL;
            case "BLACK" -> Color.BLACK;
            default -> null;
        };
    }

    public boolean isSoundEnabled(String path) {
        return config.getBoolean(path + ".enabled", true);
    }

    public Sound getSound(String path) {
        String name = config.getString(path + ".sound", "");
        try {
            return Sound.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public float getSoundVolume(String path) {
        return (float) config.getDouble(path + ".volume", 1.0);
    }

    public float getSoundPitch(String path) {
        return (float) config.getDouble(path + ".pitch", 1.0);
    }
}
