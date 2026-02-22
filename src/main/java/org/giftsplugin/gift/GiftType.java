package org.giftsplugin.gift;

public enum GiftType {
    
    NORMAL("normal"),
    SUPER("super");

    private final String key;

    GiftType(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }
}
