package com.oolongho.holograms.command;

import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * 子命令抽象类
 * 定义子命令的基本结构
 *
 *
 */
public abstract class Subcommand {

    private final String name;
    private final String descriptionKey;
    private final String usageKey;
    private final String permission;
    private final String commandPermission;
    private boolean playerOnly = false;

    public Subcommand(String name, String descriptionKey, String permission) {
        this.name = name;
        this.descriptionKey = descriptionKey;
        this.usageKey = "cmd.usage-" + name;
        this.permission = permission;
        this.commandPermission = "wooholograms.command." + name;
    }

    public Subcommand(String name, String descriptionKey, String usageKey, String permission) {
        this.name = name;
        this.descriptionKey = descriptionKey;
        this.usageKey = usageKey;
        this.permission = permission;
        this.commandPermission = "wooholograms.command." + name;
    }

    public String getName() {
        return name;
    }

    /**
     * 获取描述的语言键（调用方通过 {@code plugin.getMessages().getRaw(key)} 解析）
     */
    public String getDescriptionKey() {
        return descriptionKey;
    }

    /**
     * 获取用法的语言键（调用方通过 {@code plugin.getMessages().getRaw(key)} 解析）
     */
    public String getUsageKey() {
        return usageKey;
    }

    public String getPermission() {
        return permission;
    }

    public String getCommandPermission() {
        return commandPermission;
    }

    public boolean isPlayerOnly() {
        return playerOnly;
    }

    public void setPlayerOnly(boolean playerOnly) {
        this.playerOnly = playerOnly;
    }

    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission(commandPermission) || sender.hasPermission(permission) || sender.hasPermission("wooholograms.admin");
    }

    public abstract boolean execute(CommandSender sender, String[] args);

    public abstract List<String> tabComplete(CommandSender sender, String[] args);
}
