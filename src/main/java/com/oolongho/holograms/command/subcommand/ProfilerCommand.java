package com.oolongho.holograms.command.subcommand;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.command.Subcommand;
import com.oolongho.holograms.util.ColorUtil;
import com.oolongho.holograms.util.Profiler;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

/**
 * 性能分析器命令
 * /wh profiler — 显示性能报告
 * /wh profiler on — 启用分析器
 * /wh profiler off — 禁用分析器
 * /wh profiler reset — 重置统计数据
 *
 */
public class ProfilerCommand extends Subcommand {

    private final WooHolograms plugin;

    public ProfilerCommand(WooHolograms plugin) {
        super("profiler", "性能分析器", "/wh profiler [on|off|reset]", "wooholograms.command.profiler");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Profiler profiler = Profiler.getInstance();

        if (args.length == 0) {
            // 显示报告
            if (!profiler.isEnabled()) {
                sender.sendMessage(ColorUtil.colorize("&e性能分析器未启用，使用 /wh profiler on 启用"));
                return true;
            }
            sender.sendMessage(profiler.getReport());
            return true;
        }

        String action = args[0].toLowerCase();
        switch (action) {
            case "on" -> {
                profiler.setEnabled(true);
                sender.sendMessage(ColorUtil.colorize("&a性能分析器已启用"));
            }
            case "off" -> {
                profiler.setEnabled(false);
                sender.sendMessage(ColorUtil.colorize("&e性能分析器已禁用，统计数据已清除"));
            }
            case "reset" -> {
                profiler.reset();
                sender.sendMessage(ColorUtil.colorize("&a性能统计数据已重置"));
            }
            default -> sender.sendMessage(ColorUtil.colorize("&c用法: " + getUsage()));
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("on", "off", "reset").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}
