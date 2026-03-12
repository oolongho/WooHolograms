package com.oolonghoo.holograms.action;

import com.oolonghoo.holograms.WooHolograms;
import com.oolonghoo.holograms.hologram.Hologram;
import com.oolonghoo.holograms.util.ColorUtil;
import com.oolonghoo.holograms.util.LocationUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 动作类型
 * 参考 DecentHolograms 的 ActionType 实现
 * 定义全息图支持的所有动作类型
 * 
 * @author oolongho
 */
public abstract class ActionType {

    /**
     * 动作类型缓存
     */
    private static final Map<String, ActionType> VALUES = new HashMap<>();

    /**
     * 根据名称获取动作类型
     * 
     * @param name 名称
     * @return 动作类型
     */
    public static ActionType getByName(String name) {
        if (name == null) {
            return null;
        }
        return VALUES.get(name.toUpperCase());
    }

    /**
     * 获取所有动作类型
     * 
     * @return 动作类型集合
     */
    public static Collection<ActionType> getActionTypes() {
        return VALUES.values();
    }

    /*
     * 内置动作类型
     */

    /**
     * 无操作
     */
    public static final ActionType NONE = new ActionType("NONE") {
        @Override
        public boolean execute(Player player, String... args) {
            return true;
        }
        
        {
            register();
        }
    };

    /**
     * 发送消息
     */
    public static final ActionType MESSAGE = new ActionType("MESSAGE") {
        @Override
        public boolean execute(Player player, String... args) {
            if (player == null || args == null || args.length == 0) {
                return true;
            }

            String message = String.join(" ", args);
            message = message.replace("{player}", player.getName());
            player.sendMessage(ColorUtil.colorize(message));
            return true;
        }
        
        {
            register();
        }
    };

    /**
     * 以玩家身份执行命令
     */
    public static final ActionType COMMAND = new ActionType("COMMAND") {
        @Override
        public boolean execute(Player player, String... args) {
            if (player == null || args == null || args.length == 0) {
                return true;
            }

            String command = String.join(" ", args);
            command = command.replace("{player}", player.getName());

            // 检查命令是否在黑名单中
            if (WooHolograms.getInstance().getConfigManager().isCommandBlacklisted(command)) {
                if (WooHolograms.getInstance().getConfigManager().isDebug()) {
                    WooHolograms.getInstance().getLogger().warning(
                            String.format("Blocked blacklisted command for player %s: %s", player.getName(), command));
                }
                player.sendMessage(ColorUtil.colorize("&c该命令被禁止执行！"));
                return false;
            }

            // 在主线程执行命令
            final String finalCommand = command;
            Bukkit.getScheduler().runTask(WooHolograms.getInstance(), () -> {
                player.chat("/" + finalCommand);
            });
            return true;
        }
        
        {
            register();
        }
    };

    /**
     * 以控制台身份执行命令
     */
    public static final ActionType CONSOLE = new ActionType("CONSOLE") {
        @Override
        public boolean execute(Player player, String... args) {
            if (player == null || args == null || args.length == 0) {
                return true;
            }

            String command = String.join(" ", args);
            command = command.replace("{player}", player.getName());

            // 检查命令是否在黑名单中
            if (WooHolograms.getInstance().getConfigManager().isCommandBlacklisted(command)) {
                if (WooHolograms.getInstance().getConfigManager().isDebug()) {
                    WooHolograms.getInstance().getLogger().warning(
                            String.format("Blocked blacklisted console command for player %s: %s", player.getName(), command));
                }
                player.sendMessage(ColorUtil.colorize("&c该命令被禁止执行！"));
                return false;
            }

            // 在主线程执行命令
            final String finalCommand = command;
            Bukkit.getScheduler().runTask(WooHolograms.getInstance(), () -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
            });
            return true;
        }
        
        {
            register();
        }
    };

    /**
     * 播放音效
     */
    public static final ActionType SOUND = new ActionType("SOUND") {
        @Override
        public boolean execute(Player player, String... args) {
            if (player == null || args == null || args.length == 0) {
                return true;
            }

            String[] spl = args[0].split(":", 3);
            Sound sound;
            try {
                sound = Sound.valueOf(spl[0].toUpperCase());
            } catch (IllegalArgumentException e) {
                // 尝试使用自定义音效名称
                try {
                    sound = Sound.valueOf("ENTITY_EXPERIENCE_ORB_PICKUP");
                } catch (IllegalArgumentException ex) {
                    return true;
                }
            }

            float volume = 1.0f;
            float pitch = 1.0f;

            if (spl.length >= 3) {
                try {
                    volume = Float.parseFloat(spl[1]);
                    pitch = Float.parseFloat(spl[2]);
                } catch (NumberFormatException ignored) {
                }
            } else if (spl.length == 2) {
                try {
                    volume = Float.parseFloat(spl[1]);
                } catch (NumberFormatException ignored) {
                }
            }

            player.playSound(player.getLocation(), sound, volume, pitch);
            return true;
        }
        
        {
            register();
        }
    };

    /**
     * 传送玩家
     */
    public static final ActionType TELEPORT = new ActionType("TELEPORT") {
        @Override
        public boolean execute(Player player, String... args) {
            if (player == null || args == null || args.length == 0) {
                return true;
            }

            String locationString = String.join(":", args);
            Location location = LocationUtil.fromString(locationString);

            if (location == null) {
                return false;
            }

            // 在主线程传送
            final Location finalLocation = location;
            Bukkit.getScheduler().runTask(WooHolograms.getInstance(), () -> {
                player.teleport(finalLocation);
            });
            return true;
        }
        
        {
            register();
        }
    };

    /**
     * 连接到其他服务器（BungeeCord）
     */
    public static final ActionType SERVER = new ActionType("SERVER") {
        @Override
        public boolean execute(Player player, String... args) {
            if (player == null || args == null || args.length == 0) {
                return true;
            }

            String server = args[0];
            try {
                java.io.ByteArrayOutputStream byteArray = new java.io.ByteArrayOutputStream();
                java.io.DataOutputStream out = new java.io.DataOutputStream(byteArray);
                out.writeUTF("Connect");
                out.writeUTF(server);
                player.sendPluginMessage(WooHolograms.getInstance(), "BungeeCord", byteArray.toByteArray());
            } catch (IllegalArgumentException | IllegalStateException e) {
                if (WooHolograms.getInstance().getConfigManager().isDebug()) {
                    WooHolograms.getInstance().getLogger().warning(
                            String.format("BungeeCord connect failed: %s", e.getMessage()));
                }
            } catch (java.io.IOException e) {
                if (WooHolograms.getInstance().getConfigManager().isDebug()) {
                    WooHolograms.getInstance().getLogger().warning(
                            String.format("BungeeCord connect failed due to IO error: %s", e.getMessage()));
                }
            }
            return true;
        }
        
        {
            register();
        }
    };

    /**
     * 下一页
     */
    public static final ActionType NEXT_PAGE = new ActionType("NEXT_PAGE") {
        @Override
        public boolean execute(Player player, String... args) {
            if (player == null || args == null || args.length == 0) {
                return true;
            }

            Hologram hologram = WooHolograms.getInstance().getHologramManager().getHologram(args[0]);
            if (hologram == null) {
                return true;
            }

            int nextPage = hologram.getPlayerPage(player) + 1;
            if (nextPage < 0 || nextPage >= hologram.getPageCount()) {
                return true;
            }

            hologram.show(player, nextPage);
            return true;
        }
        
        {
            register();
        }
    };

    /**
     * 上一页
     */
    public static final ActionType PREV_PAGE = new ActionType("PREV_PAGE") {
        @Override
        public boolean execute(Player player, String... args) {
            if (player == null || args == null || args.length == 0) {
                return true;
            }

            Hologram hologram = WooHolograms.getInstance().getHologramManager().getHologram(args[0]);
            if (hologram == null) {
                return true;
            }

            int prevPage = hologram.getPlayerPage(player) - 1;
            if (prevPage < 0) {
                return true;
            }

            hologram.show(player, prevPage);
            return true;
        }
        
        {
            register();
        }
    };

    /**
     * 跳转到指定页
     */
    public static final ActionType PAGE = new ActionType("PAGE") {
        @Override
        public boolean execute(Player player, String... args) {
            if (player == null || args == null || args.length == 0) {
                return true;
            }

            String[] spl = args[0].split(":");
            if (spl.length < 2) {
                return true;
            }

            Hologram hologram = WooHolograms.getInstance().getHologramManager().getHologram(spl[0]);
            if (hologram == null) {
                return true;
            }

            int page;
            try {
                page = Integer.parseInt(spl[1]) - 1; // 转换为 0-based 索引
            } catch (NumberFormatException e) {
                return true;
            }

            if (page < 0 || page >= hologram.getPageCount()) {
                return true;
            }

            hologram.show(player, page);
            return true;
        }
        
        {
            register();
        }
    };

    /*
     * 实例字段和方法
     */

    /**
     * 动作类型名称
     */
    private final String name;

    /**
     * 构造函数
     * 
     * @param name 动作类型名称
     */
    protected ActionType(String name) {
        this.name = name.toUpperCase();
        // 注册操作延迟到构造完成后执行，避免在构造函数中泄漏 this
    }

    /**
     * 注册动作类型到缓存
     * 在子类构造完成后调用
     */
    protected void register() {
        if (VALUES.containsKey(this.name)) {
            throw new IllegalArgumentException("ActionType " + this.name + " 已存在！");
        }
        VALUES.put(this.name, this);
    }

    /**
     * 获取动作类型名称
     * 
     * @return 名称
     */
    public String getName() {
        return name;
    }

    /**
     * 执行动作
     * 
     * @param player 玩家
     * @param args 参数
     * @return 是否成功
     */
    public abstract boolean execute(Player player, String... args);

    @Override
    public String toString() {
        return name;
    }
}
