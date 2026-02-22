package org.giftsplugin.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Text {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

    private Text() {
    }

    public static Component color(String text) {
        return SERIALIZER.deserialize(translateHex(text)).decoration(TextDecoration.ITALIC, false);
    }

    public static Component colorWithItalic(String text) {
        return SERIALIZER.deserialize(translateHex(text));
    }

    private static String translateHex(String text) {
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder("&x");
            for (char c : hex.toCharArray()) {
                replacement.append('&').append(c);
            }
            matcher.appendReplacement(result, replacement.toString());
        }

        matcher.appendTail(result);
        return result.toString();
    }
}
