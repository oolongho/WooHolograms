package com.oolonghoo.holograms.command;

import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

/**
 * 子命令抽象类
 * 定义子命令的基本结构
 * 
 */
public abstract class Subcommand {

    private final String name;
    private final String description;
    private final String usage;
    private final String permission;
    private final List<String> aliases;
    private boolean playerOnly = false;

    public Subcommand(String name, String description, String permission, List<String> aliases) {
        this.name = name;
        this.description = description;
        this.usage = "/" + name;
        this.permission = permission;
        this.aliases = aliases != null ? aliases : new ArrayList<>();
    }

    public Subcommand(String name, String description, String usage, String permission, List<String> aliases) {
        this.name = name;
        this.description = description;
        this.usage = usage;
        this.permission = permission;
        this.aliases = aliases != null ? aliases : new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getUsage() {
        return usage;
    }

    public String getPermission() {
        return permission;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public boolean isPlayerOnly() {
        return playerOnly;
    }

    public void setPlayerOnly(boolean playerOnly) {
        this.playerOnly = playerOnly;
    }

    public boolean hasPermission(CommandSender sender) {
        return permission == null || permission.isEmpty() || sender.hasPermission(permission);
    }

    public abstract boolean execute(CommandSender sender, String[] args);

    public abstract List<String> tabComplete(CommandSender sender, String[] args);
}
