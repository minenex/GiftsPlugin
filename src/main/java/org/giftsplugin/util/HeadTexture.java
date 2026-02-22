package org.giftsplugin.util;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.URI;
import java.net.URL;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HeadTexture {

    private static final Pattern URL_PATTERN = Pattern.compile("\"url\"\\s*:\\s*\"(https?://[^\"]+)\"");

    private HeadTexture() {
    }

    public static void apply(ItemStack head, String base64) {
        if (base64 == null || base64.isEmpty()) return;

        try {
            String json = new String(Base64.getDecoder().decode(base64));
            Matcher matcher = URL_PATTERN.matcher(json);
            if (!matcher.find()) return;

            URL textureUrl = URI.create(matcher.group(1)).toURL();

            SkullMeta meta = (SkullMeta) head.getItemMeta();
            PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());
            PlayerTextures textures = profile.getTextures();
            textures.setSkin(textureUrl);
            profile.setTextures(textures);
            meta.setOwnerProfile(profile);
            head.setItemMeta(meta);
        } catch (Exception ignored) {
        }
    }
}
