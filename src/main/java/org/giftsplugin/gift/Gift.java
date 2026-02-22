package org.giftsplugin.gift;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class Gift {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final int id;
    private final GiftType type;
    private final String world;
    private final int x;
    private final int y;
    private final int z;
    private final long createdAt;
    private final UUID creatorId;
    private final String creatorName;

    private UUID finderId;
    private String finderName;

    private Gift(Builder builder) {
        this.id = builder.id;
        this.type = builder.type;
        this.world = builder.world;
        this.x = builder.x;
        this.y = builder.y;
        this.z = builder.z;
        this.createdAt = builder.createdAt;
        this.creatorId = builder.creatorId;
        this.creatorName = builder.creatorName;
        this.finderId = builder.finderId;
        this.finderName = builder.finderName;
    }

    public int id() {
        return id;
    }

    public GiftType type() {
        return type;
    }

    public String world() {
        return world;
    }

    public Location location() {
        World bukkitWorld = Bukkit.getWorld(world);
        return bukkitWorld != null ? new Location(bukkitWorld, x, y, z) : null;
    }

    public String coordinates() {
        return x + ", " + y + ", " + z;
    }

    public boolean matchesLocation(Location loc) {
        return loc.getWorld().getName().equals(world)
                && loc.getBlockX() == x
                && loc.getBlockY() == y
                && loc.getBlockZ() == z;
    }

    public String createdAtFormatted() {
        LocalDateTime time = LocalDateTime.ofInstant(Instant.ofEpochMilli(createdAt), ZoneId.systemDefault());
        return time.format(TIME_FORMAT);
    }

    public long createdAt() {
        return createdAt;
    }

    public UUID creatorId() {
        return creatorId;
    }

    public String creatorName() {
        return creatorName;
    }

    public boolean isFound() {
        return finderId != null;
    }

    public UUID finderId() {
        return finderId;
    }

    public String finderName() {
        return finderName;
    }

    public void markFound(UUID playerId, String playerName) {
        this.finderId = playerId;
        this.finderName = playerName;
    }

    public Map<String, Object> serialize() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", id);
        data.put("type", type.name());
        data.put("world", world);
        data.put("x", x);
        data.put("y", y);
        data.put("z", z);
        data.put("created-at", createdAt);
        data.put("creator-id", creatorId.toString());
        data.put("creator-name", creatorName);
        data.put("found", isFound());
        if (isFound()) {
            data.put("finder-id", finderId.toString());
            data.put("finder-name", finderName);
        }
        return data;
    }

    public static Gift deserialize(Map<String, Object> data) {
        Builder builder = new Builder()
                .id(((Number) data.get("id")).intValue())
                .type(GiftType.valueOf((String) data.get("type")))
                .world((String) data.get("world"))
                .x(((Number) data.get("x")).intValue())
                .y(((Number) data.get("y")).intValue())
                .z(((Number) data.get("z")).intValue())
                .createdAt(((Number) data.get("created-at")).longValue())
                .creatorId(UUID.fromString((String) data.get("creator-id")))
                .creatorName((String) data.get("creator-name"));

        boolean found = data.containsKey("found") && Boolean.TRUE.equals(data.get("found"));
        if (found && data.containsKey("finder-id") && data.containsKey("finder-name")) {
            builder.finderId(UUID.fromString((String) data.get("finder-id")))
                    .finderName((String) data.get("finder-name"));
        }

        return builder.build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private int id;
        private GiftType type;
        private String world;
        private int x, y, z;
        private long createdAt;
        private UUID creatorId;
        private String creatorName;
        private UUID finderId;
        private String finderName;

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder type(GiftType type) {
            this.type = type;
            return this;
        }

        public Builder location(Location loc) {
            this.world = loc.getWorld().getName();
            this.x = loc.getBlockX();
            this.y = loc.getBlockY();
            this.z = loc.getBlockZ();
            return this;
        }

        public Builder world(String world) {
            this.world = world;
            return this;
        }

        public Builder x(int x) {
            this.x = x;
            return this;
        }

        public Builder y(int y) {
            this.y = y;
            return this;
        }

        public Builder z(int z) {
            this.z = z;
            return this;
        }

        public Builder createdAt(long timestamp) {
            this.createdAt = timestamp;
            return this;
        }

        public Builder createdNow() {
            this.createdAt = System.currentTimeMillis();
            return this;
        }

        public Builder creatorId(UUID id) {
            this.creatorId = id;
            return this;
        }

        public Builder creatorName(String name) {
            this.creatorName = name;
            return this;
        }

        public Builder finderId(UUID id) {
            this.finderId = id;
            return this;
        }

        public Builder finderName(String name) {
            this.finderName = name;
            return this;
        }

        public Gift build() {
            return new Gift(this);
        }
    }
}
