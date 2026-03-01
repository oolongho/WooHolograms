package com.oolonghoo.holograms.hologram;

import java.util.Locale;
import java.util.UUID;

public class HeadTexture {

    public enum Type {
        BASE64,
        PLAYER,
        HDB
    }

    private final Type type;
    private final String value;
    private UUID uuid;

    private HeadTexture(Type type, String value) {
        this.type = type;
        this.value = value;
    }

    public Type getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getTextureValue() {
        return value;
    }

    public static HeadTexture parse(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }

        String upperInput = input.toUpperCase(Locale.ROOT);
        
        if (upperInput.startsWith("#HEAD:URL:")) {
            String base64 = input.substring("#HEAD:URL:".length());
            if (!base64.isEmpty()) {
                return new HeadTexture(Type.BASE64, base64);
            }
        } else if (upperInput.startsWith("#HEAD:PLAYER:")) {
            String playerName = input.substring("#HEAD:PLAYER:".length());
            if (!playerName.isEmpty()) {
                return new HeadTexture(Type.PLAYER, playerName);
            }
        } else if (upperInput.startsWith("#HEAD:HDB:")) {
            String hdbId = input.substring("#HEAD:HDB:".length());
            if (!hdbId.isEmpty()) {
                return new HeadTexture(Type.HDB, hdbId);
            }
        }

        return null;
    }

    public static boolean isHeadTexture(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        String upperInput = input.toUpperCase(Locale.ROOT);
        return upperInput.startsWith("#HEAD:URL:") 
            || upperInput.startsWith("#HEAD:PLAYER:") 
            || upperInput.startsWith("#HEAD:HDB:");
    }

    @Override
    public String toString() {
        return "HeadTexture{" +
                "type=" + type +
                ", value='" + value + '\'' +
                '}';
    }
}
