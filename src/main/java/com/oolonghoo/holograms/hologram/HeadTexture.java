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
        
        if (upperInput.startsWith("#HEAD:URL:") || upperInput.startsWith("#SMALLHEAD:URL:")) {
            String base64 = extractValue(input, "URL:");
            if (!base64.isEmpty()) {
                return new HeadTexture(Type.BASE64, base64);
            }
        } else if (upperInput.startsWith("#HEAD:PLAYER:") || upperInput.startsWith("#SMALLHEAD:PLAYER:")) {
            String playerName = extractValue(input, "PLAYER:");
            if (!playerName.isEmpty()) {
                return new HeadTexture(Type.PLAYER, playerName);
            }
        } else if (upperInput.startsWith("#HEAD:HDB:") || upperInput.startsWith("#SMALLHEAD:HDB:")) {
            String hdbId = extractValue(input, "HDB:");
            if (!hdbId.isEmpty()) {
                return new HeadTexture(Type.HDB, hdbId);
            }
        } else if (upperInput.startsWith("#HEAD:") || upperInput.startsWith("#SMALLHEAD:")) {
            String data = extractRawData(input);
            if (!data.isEmpty()) {
                if (isBase64Texture(data)) {
                    return new HeadTexture(Type.BASE64, data);
                } else {
                    return new HeadTexture(Type.PLAYER, data);
                }
            }
        }

        return null;
    }

    private static String extractValue(String input, String prefix) {
        String upperInput = input.toUpperCase(Locale.ROOT);
        int prefixIndex = upperInput.indexOf(prefix);
        if (prefixIndex == -1) {
            return "";
        }
        return input.substring(prefixIndex + prefix.length());
    }

    private static String extractRawData(String input) {
        String upperInput = input.toUpperCase(Locale.ROOT);
        if (upperInput.startsWith("#HEAD:")) {
            return input.substring(6);
        } else if (upperInput.startsWith("#SMALLHEAD:")) {
            return input.substring(11);
        }
        return "";
    }

    private static boolean isBase64Texture(String data) {
        if (data == null || data.isEmpty()) {
            return false;
        }
        return data.length() > 50;
    }

    public static boolean isHeadTexture(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        String upperInput = input.toUpperCase(Locale.ROOT);
        return upperInput.startsWith("#HEAD:") || upperInput.startsWith("#SMALLHEAD:");
    }

    @Override
    public String toString() {
        return "HeadTexture{" +
                "type=" + type +
                ", value='" + value + '\'' +
                '}';
    }
}
