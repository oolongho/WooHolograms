package com.oolonghoo.holograms.nms.versions;

import org.bukkit.Bukkit;

final class EntityPacketHelperFactory {

    private static final EntityPacketHelper INSTANCE = create();

    static EntityPacketHelper getInstance() {
        return INSTANCE;
    }

    private static EntityPacketHelper create() {
        String version = Bukkit.getServer().getBukkitVersion();
        int dashIdx = version.indexOf('-');
        if (dashIdx > 0) version = version.substring(0, dashIdx);
        String[] parts = version.split("\\.");

        int major = 0, minor = 0, patch = 0;
        try {
            if (parts.length >= 1) major = Integer.parseInt(parts[0]);
            if (parts.length >= 2) minor = Integer.parseInt(parts[1]);
            if (parts.length >= 3) patch = Integer.parseInt(parts[2]);
        } catch (NumberFormatException ignored) {}

        // Legacy: MC 1.21.0/1（旧版 ClientboundAddEntityPacket 构造函数签名）
        // Modern: MC 1.21.2+ 及 Spigot 26.1+（新版构造函数含 headYaw 参数）
        boolean legacy = (major == 1 && minor == 21 && patch <= 1);

        String adapterType = legacy ? "legacy" : "modern";
        Bukkit.getLogger().info("[WooHolograms] NMS adapter: " + adapterType + " (server version: " + version + ")");

        if (legacy) {
            return new LegacyEntityPacketHelper();
        }
        return new ModernEntityPacketHelper();
    }
}
