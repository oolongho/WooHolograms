package com.oolongho.holograms.command.subcommand;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.command.Subcommand;
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
        super("profiler", "cmd.desc-profiler", "cmd.usage-profiler", "wooholograms.command.profiler");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Profiler profiler = Profiler.getInstance();

        if (args.length == 0) {
            // 显示报告
            if (!profiler.isEnabled()) {
                plugin.getMessages().send(sender, "profiler.not-enabled");
                return true;
            }
            sender.sendMessage(plugin.getMessages().parse(profiler.getReport(plugin.getMessages())));
            return true;
        }

        String action = args[0].toLowerCase();
        switch (action) {
            case "on" -> {
                profiler.setEnabled(true);
                plugin.getMessages().send(sender, "profiler.enabled");
            }
            case "off" -> {
                profiler.setEnabled(false);
                plugin.getMessages().send(sender, "profiler.disabled");
            }
            case "reset" -> {
                profiler.reset();
                plugin.getMessages().send(sender, "profiler.reset");
            }
            default -> plugin.getMessages().send(sender, "profiler.usage");
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
