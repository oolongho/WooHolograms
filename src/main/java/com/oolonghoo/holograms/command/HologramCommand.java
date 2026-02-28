package com.oolonghoo.holograms.command;

import com.oolonghoo.holograms.WooHolograms;
import com.oolonghoo.holograms.action.Action;
import com.oolonghoo.holograms.action.ActionType;
import com.oolonghoo.holograms.action.ClickType;
import com.oolonghoo.holograms.gui.ChatInputManager;
import com.oolonghoo.holograms.gui.GuiManager;
import com.oolonghoo.holograms.gui.HologramDetailGui;
import com.oolonghoo.holograms.gui.HologramListGui;
import com.oolonghoo.holograms.hologram.Hologram;
import com.oolonghoo.holograms.hologram.HologramLine;
import com.oolonghoo.holograms.hologram.HologramPage;
import com.oolonghoo.holograms.util.ColorUtil;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.AbstractMap;
import java.util.stream.Collectors;

public class HologramCommand implements CommandExecutor, TabCompleter {

    private final WooHolograms plugin;
    private final List<Subcommand> subcommands;

    public HologramCommand(WooHolograms plugin) {
        this.plugin = plugin;
        this.subcommands = new ArrayList<>();
        
        registerSubcommands();
    }

    private void registerSubcommands() {
        subcommands.add(new CreateCommand());
        subcommands.add(new DeleteCommand());
        subcommands.add(new CopyCommand());
        subcommands.add(new NearCommand());
        subcommands.add(new EnableCommand());
        subcommands.add(new DisableCommand());
        subcommands.add(new ListCommand());
        subcommands.add(new InfoCommand());
        subcommands.add(new TeleportCommand());
        subcommands.add(new MoveHereCommand());
        subcommands.add(new MoveToCommand());
        subcommands.add(new AddLineCommand());
        subcommands.add(new DeleteLineCommand());
        subcommands.add(new SetLineCommand());
        subcommands.add(new InsertLineCommand());
        subcommands.add(new AddPageCommand());
        subcommands.add(new DeletePageCommand());
        subcommands.add(new SetRangeCommand());
        subcommands.add(new SetIntervalCommand());
        subcommands.add(new SetPermissionCommand());
        subcommands.add(new AddActionCommand());
        subcommands.add(new DeleteActionCommand());
        subcommands.add(new ActionsCommand());
        subcommands.add(new OffsetCommand());
        subcommands.add(new HeightCommand());
        subcommands.add(new ReloadCommand());
        subcommands.add(new HelpCommand());
        subcommands.add(new GuiCommand());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        String subcommandName = args[0].toLowerCase();
        
        for (Subcommand subcommand : subcommands) {
            if (subcommand.getName().equalsIgnoreCase(subcommandName)) {
                if (subcommand.isPlayerOnly() && !(sender instanceof Player)) {
                    sender.sendMessage(ColorUtil.colorize("&c此命令只能由玩家执行！"));
                    return true;
                }
                
                if (!hasPermission(sender, subcommand.getPermission())) {
                    sender.sendMessage(ColorUtil.colorize("&c你没有权限执行此命令！"));
                    return true;
                }
                
                String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                return subcommand.execute(sender, subArgs);
            }
        }
        
        sender.sendMessage(ColorUtil.colorize("&c未知的命令！使用 /wh help 查看帮助。"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            for (Subcommand subcommand : subcommands) {
                if (hasPermission(sender, subcommand.getPermission())) {
                    if (subcommand.getName().startsWith(args[0].toLowerCase())) {
                        completions.add(subcommand.getName());
                    }
                }
            }
        } else if (args.length > 1) {
            String subcommandName = args[0].toLowerCase();
            for (Subcommand subcommand : subcommands) {
                if (subcommand.getName().equalsIgnoreCase(subcommandName)) {
                    String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                    return subcommand.tabComplete(sender, subArgs);
                }
            }
        }
        
        return completions;
    }

    private boolean hasPermission(CommandSender sender, String permission) {
        return permission == null || permission.isEmpty() || sender.hasPermission(permission);
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ColorUtil.colorize("&e========== &6WooHolograms 帮助 &e=========="));
        sender.sendMessage(ColorUtil.colorize("&e/wh create <名称> [文本] &7- 创建全息图"));
        sender.sendMessage(ColorUtil.colorize("&e/wh delete <名称> &7- 删除全息图"));
        sender.sendMessage(ColorUtil.colorize("&e/wh copy <源> <目标> &7- 克隆全息图"));
        sender.sendMessage(ColorUtil.colorize("&e/wh near [范围] &7- 查找附近全息图"));
        sender.sendMessage(ColorUtil.colorize("&e/wh enable <名称> &7- 启用全息图"));
        sender.sendMessage(ColorUtil.colorize("&e/wh disable <名称> &7- 禁用全息图"));
        sender.sendMessage(ColorUtil.colorize("&e/wh list &7- 列出所有全息图"));
        sender.sendMessage(ColorUtil.colorize("&e/wh info <名称> &7- 查看全息图详情"));
        sender.sendMessage(ColorUtil.colorize("&e/wh teleport <名称> &7- 传送到全息图"));
        sender.sendMessage(ColorUtil.colorize("&e/wh movehere <名称> &7- 移动全息图到当前位置"));
        sender.sendMessage(ColorUtil.colorize("&e/wh moveto <名称> <x> <y> <z> &7- 移动到指定坐标"));
        sender.sendMessage(ColorUtil.colorize("&e/wh addline <名称> <文本> &7- 添加行"));
        sender.sendMessage(ColorUtil.colorize("&e/wh deleteline <名称> <行号> &7- 删除行"));
        sender.sendMessage(ColorUtil.colorize("&e/wh setline <名称> <行号> <文本> &7- 设置行内容"));
        sender.sendMessage(ColorUtil.colorize("&e/wh insertline <名称> <行号> <文本> &7- 插入行"));
        sender.sendMessage(ColorUtil.colorize("&e/wh addpage <名称> [内容] &7- 添加页面"));
        sender.sendMessage(ColorUtil.colorize("&e/wh deletepage <名称> <页码> &7- 删除页面"));
        sender.sendMessage(ColorUtil.colorize("&e/wh setrange <名称> <范围> &7- 设置显示范围"));
        sender.sendMessage(ColorUtil.colorize("&e/wh setinterval <名称> <tick> &7- 设置更新间隔"));
        sender.sendMessage(ColorUtil.colorize("&e/wh setpermission <名称> [权限] &7- 设置查看权限"));
        sender.sendMessage(ColorUtil.colorize("&e/wh addaction <名称> <页码> <点击类型> <动作类型> [值] &7- 添加动作"));
        sender.sendMessage(ColorUtil.colorize("&e/wh deleteaction <名称> <页码> <点击类型> <索引> &7- 删除动作"));
        sender.sendMessage(ColorUtil.colorize("&e/wh actions <名称> <页码> <点击类型> &7- 列出动作"));
        sender.sendMessage(ColorUtil.colorize("&e/wh offset <名称> <行号> <x> <y> <z> &7- 设置行偏移"));
        sender.sendMessage(ColorUtil.colorize("&e/wh height <名称> <行号> <高度> &7- 设置行高度"));
        sender.sendMessage(ColorUtil.colorize("&e/wh reload &7- 重载配置"));
        sender.sendMessage(ColorUtil.colorize("&e/wh help &7- 显示帮助"));
        sender.sendMessage(ColorUtil.colorize("&e===================================="));
    }

    private class CreateCommand extends Subcommand {
        public CreateCommand() {
            super("create", "创建一个新的全息图", "wooholograms.admin", Collections.emptyList());
            setPlayerOnly(true);
        }

        @Override
        public boolean execute(CommandSender sender, String[] args) {
            Player player = (Player) sender;
            
            if (args.length < 1) {
                player.sendMessage(ColorUtil.colorize("&c用法: /wh create <名称> [文本]"));
                return true;
            }
            
            String name = args[0];
            
            if (plugin.getHologramManager().containsHologram(name)) {
                player.sendMessage(ColorUtil.colorize("&c全息图 " + name + " 已存在！"));
                return true;
            }
            
            Location location = player.getLocation();
            Hologram hologram = plugin.getHologramManager().createHologram(name, location);
            
            if (hologram == null) {
                player.sendMessage(ColorUtil.colorize("&c创建全息图失败！"));
                return true;
            }
            
            HologramPage page = hologram.getPage(0);
            if (page != null) {
                if (args.length > 1) {
                    String text = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                    page.addLine(text);
                } else {
                    page.addLine("&7请输入文本......");
                }
            }
            
            hologram.save();
            hologram.show(player, 0);
            
            player.sendMessage(ColorUtil.colorize("&a成功创建全息图 " + name + "！"));
            return true;
        }

        @Override
        public List<String> tabComplete(CommandSender sender, String[] args) {
            return new ArrayList<>();
        }
    }

    private class DeleteCommand extends Subcommand {
        public DeleteCommand() {
            super("delete", "删除一个全息图", "wooholograms.admin", Collections.emptyList());
        }

        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (args.length < 1) {
                sender.sendMessage(ColorUtil.colorize("&c用法: /wh delete <名称>"));
                return true;
            }
            
            String name = args[0];
            
            if (!plugin.getHologramManager().containsHologram(name)) {
                sender.sendMessage(ColorUtil.colorize("&c全息图 " + name + " 不存在！"));
                return true;
            }
            
            plugin.getHologramManager().deleteHologram(name);
            sender.sendMessage(ColorUtil.colorize("&a成功删除全息图 " + name + "！"));
            return true;
        }

        @Override
        public List<String> tabComplete(CommandSender sender, String[] args) {
            if (args.length == 1) {
                return plugin.getHologramManager().getHologramNames().stream()
                        .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
            }
            return new ArrayList<>();
        }
    }

    private class ListCommand extends Subcommand {
        public ListCommand() {
            super("list", "列出所有全息图", "wooholograms.admin", Collections.emptyList());
        }

        @Override
        public boolean execute(CommandSender sender, String[] args) {
            sender.sendMessage(ColorUtil.colorize("&e========== &6全息图列表 &e=========="));
            
            for (Hologram hologram : plugin.getHologramManager().getHolograms()) {
                Location loc = hologram.getLocation();
                sender.sendMessage(ColorUtil.colorize("&e" + hologram.getName() + 
                        " &7- 世界: " + (loc.getWorld() != null ? loc.getWorld().getName() : "null") + 
                        ", 位置: " + String.format("%.1f, %.1f, %.1f", loc.getX(), loc.getY(), loc.getZ())));
            }
            
            sender.sendMessage(ColorUtil.colorize("&e总计: &f" + plugin.getHologramManager().getHologramCount() + " 个全息图"));
            return true;
        }

        @Override
        public List<String> tabComplete(CommandSender sender, String[] args) {
            return new ArrayList<>();
        }
    }

    private class InfoCommand extends Subcommand {
        public InfoCommand() {
            super("info", "查看全息图详情", "wooholograms.admin", Collections.emptyList());
        }

        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (args.length < 1) {
                sender.sendMessage(ColorUtil.colorize("&c用法: /wh info <名称>"));
                return true;
            }
            
            String name = args[0];
            Hologram hologram = plugin.getHologramManager().getHologram(name);
            
            if (hologram == null) {
                sender.sendMessage(ColorUtil.colorize("&c全息图 " + name + " 不存在！"));
                return true;
            }
            
            sender.sendMessage(ColorUtil.colorize("&e========== &6全息图: " + name + " &e=========="));
            Location loc = hologram.getLocation();
            sender.sendMessage(ColorUtil.colorize("&e位置: &f" + 
                    (loc.getWorld() != null ? loc.getWorld().getName() : "null") + ", " +
                    String.format("%.2f, %.2f, %.2f", loc.getX(), loc.getY(), loc.getZ())));
            sender.sendMessage(ColorUtil.colorize("&e显示范围: &f" + hologram.getDisplayRange()));
            sender.sendMessage(ColorUtil.colorize("&e启用: &f" + hologram.isEnabled()));
            sender.sendMessage(ColorUtil.colorize("&e页面数: &f" + hologram.getPageCount()));
            
            int pageIndex = 0;
            for (HologramPage page : hologram.getPages()) {
                sender.sendMessage(ColorUtil.colorize("&e--- 第 " + (pageIndex + 1) + " 页 (" + page.size() + " 行) ---"));
                int lineIndex = 0;
                for (HologramLine line : page.getLines()) {
                    sender.sendMessage(ColorUtil.colorize("&7" + (lineIndex + 1) + ". &f" + line.getContent()));
                    lineIndex++;
                }
                pageIndex++;
            }
            
            return true;
        }

        @Override
        public List<String> tabComplete(CommandSender sender, String[] args) {
            if (args.length == 1) {
                return plugin.getHologramManager().getHologramNames().stream()
                        .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
            }
            return new ArrayList<>();
        }
    }

    private class TeleportCommand extends Subcommand {
        public TeleportCommand() {
            super("teleport", "传送到全息图位置", "wooholograms.admin", Collections.emptyList());
            setPlayerOnly(true);
        }

        @Override
        public boolean execute(CommandSender sender, String[] args) {
            Player player = (Player) sender;
            
            if (args.length < 1) {
                player.sendMessage(ColorUtil.colorize("&c用法: /wh teleport <名称>"));
                return true;
            }
            
            String name = args[0];
            Hologram hologram = plugin.getHologramManager().getHologram(name);
            
            if (hologram == null) {
                player.sendMessage(ColorUtil.colorize("&c全息图 " + name + " 不存在！"));
                return true;
            }
            
            player.teleport(hologram.getLocation());
            player.sendMessage(ColorUtil.colorize("&a已传送到全息图 " + name + "！"));
            return true;
        }

        @Override
        public List<String> tabComplete(CommandSender sender, String[] args) {
            if (args.length == 1) {
                return plugin.getHologramManager().getHologramNames().stream()
                        .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
            }
            return new ArrayList<>();
        }
    }

    private class MoveHereCommand extends Subcommand {
        public MoveHereCommand() {
            super("movehere", "将全息图移动到你当前位置", "wooholograms.admin", Collections.emptyList());
            setPlayerOnly(true);
        }

        @Override
        public boolean execute(CommandSender sender, String[] args) {
            Player player = (Player) sender;
            
            if (args.length < 1) {
                player.sendMessage(ColorUtil.colorize("&c用法: /wh movehere <名称>"));
                return true;
            }
            
            String name = args[0];
            Hologram hologram = plugin.getHologramManager().getHologram(name);
            
            if (hologram == null) {
                player.sendMessage(ColorUtil.colorize("&c全息图 " + name + " 不存在！"));
                return true;
            }
            
            Location newLocation = player.getLocation();
            hologram.setLocation(newLocation);
            hologram.save();
            
            // 重新显示给所有观看者
            for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
                if (hologram.isInDisplayRange(onlinePlayer)) {
                    hologram.hide(onlinePlayer);
                    hologram.show(onlinePlayer, 0);
                }
            }
            
            player.sendMessage(ColorUtil.colorize("&a已将全息图 " + name + " 移动到当前位置！"));
            return true;
        }

        @Override
        public List<String> tabComplete(CommandSender sender, String[] args) {
            if (args.length == 1) {
                return plugin.getHologramManager().getHologramNames().stream()
                        .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
            }
            return new ArrayList<>();
        }
    }

    private class MoveToCommand extends Subcommand {
        public MoveToCommand() {
            super("moveto", "将全息图移动到指定坐标", "wooholograms.admin", Collections.emptyList());
        }

        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (args.length < 4) {
                sender.sendMessage(ColorUtil.colorize("&c用法: /wh moveto <名称> <x> <y> <z>"));
                return true;
            }
            
            String name = args[0];
            Hologram hologram = plugin.getHologramManager().getHologram(name);
            
            if (hologram == null) {
                sender.sendMessage(ColorUtil.colorize("&c全息图 " + name + " 不存在！"));
                return true;
            }
            
            Location currentLoc = hologram.getLocation();
            if (currentLoc == null || currentLoc.getWorld() == null) {
                sender.sendMessage(ColorUtil.colorize("&c全息图位置无效！"));
                return true;
            }
            
            double x, y, z;
            
            try {
                x = Double.parseDouble(args[1]);
                y = Double.parseDouble(args[2]);
                z = Double.parseDouble(args[3]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ColorUtil.colorize("&c坐标必须是数字！"));
                return true;
            }
            
            Location newLocation = new Location(currentLoc.getWorld(), x, y, z, currentLoc.getYaw(), currentLoc.getPitch());
            hologram.setLocation(newLocation);
            hologram.save();
            
            for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
                if (hologram.isInDisplayRange(onlinePlayer)) {
                    hologram.hide(onlinePlayer);
                    hologram.show(onlinePlayer, 0);
                }
            }
            
            sender.sendMessage(ColorUtil.colorize("&a已将全息图 " + name + " 移动到 " + String.format("%.2f, %.2f, %.2f", x, y, z) + "！"));
            return true;
        }

        @Override
        public List<String> tabComplete(CommandSender sender, String[] args) {
            if (args.length == 1) {
                return plugin.getHologramManager().getHologramNames().stream()
                        .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (args.length >= 2 && args.length <= 4) {
                List<String> completions = new ArrayList<>();
                
                if (sender instanceof Player) {
                    String hologramName = args[0];
                    Hologram hologram = plugin.getHologramManager().getHologram(hologramName);
                    
                    if (hologram != null) {
                        Location holoLoc = hologram.getLocation();
                        if (holoLoc != null) {
                            if (args.length == 2) {
                                completions.add(String.format("%.2f", holoLoc.getX()));
                            } else if (args.length == 3) {
                                completions.add(String.format("%.2f", holoLoc.getY()));
                            } else if (args.length == 4) {
                                completions.add(String.format("%.2f", holoLoc.getZ()));
                            }
                        }
                    }
                }
                
                String currentArg = args[args.length - 1].toLowerCase();
                return completions.stream()
                        .filter(s -> s.toLowerCase().startsWith(currentArg))
                        .collect(Collectors.toList());
            }
            return new ArrayList<>();
        }
    }

    private class AddLineCommand extends Subcommand {
        public AddLineCommand() {
            super("addline", "向全息图添加一行", "wooholograms.admin", Collections.emptyList());
        }

        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (args.length < 2) {
                sender.sendMessage(ColorUtil.colorize("&c用法: /wh addline <名称> <文本>"));
                return true;
            }
            
            String name = args[0];
            Hologram hologram = plugin.getHologramManager().getHologram(name);
            
            if (hologram == null) {
                sender.sendMessage(ColorUtil.colorize("&c全息图 " + name + " 不存在！"));
                return true;
            }
            
            String text = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            HologramPage page = hologram.getPage(0);
            if (page != null) {
                page.addLine(text);
                plugin.getLogger().info("[DEBUG] Added line to hologram " + name + ", page size: " + page.size());
            } else {
                sender.sendMessage(ColorUtil.colorize("&c全息图没有页面！"));
                return true;
            }
            hologram.save();
            
            for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
                if (hologram.isInDisplayRange(onlinePlayer)) {
                    hologram.show(onlinePlayer, 0);
                }
            }
            
            sender.sendMessage(ColorUtil.colorize("&a已添加新行！"));
            return true;
        }

        @Override
        public List<String> tabComplete(CommandSender sender, String[] args) {
            if (args.length == 1) {
                return plugin.getHologramManager().getHologramNames().stream()
                        .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
            }
            return new ArrayList<>();
        }
    }

    private class DeleteLineCommand extends Subcommand {
        public DeleteLineCommand() {
            super("deleteline", "删除全息图的一行", "wooholograms.admin", Collections.emptyList());
        }

        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (args.length < 2) {
                sender.sendMessage(ColorUtil.colorize("&c用法: /wh deleteline <名称> <行号>"));
                return true;
            }
            
            String name = args[0];
            Hologram hologram = plugin.getHologramManager().getHologram(name);
            
            if (hologram == null) {
                sender.sendMessage(ColorUtil.colorize("&c全息图 " + name + " 不存在！"));
                return true;
            }
            
            try {
                int lineNumber = Integer.parseInt(args[1]);
                HologramPage page = hologram.getPage(0);
                
                if (page == null || lineNumber < 1 || lineNumber > page.size()) {
                    sender.sendMessage(ColorUtil.colorize("&c无效的行号！"));
                    return true;
                }
                
                page.removeLine(lineNumber - 1);
                hologram.save();
                
                for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
                    if (hologram.isInDisplayRange(onlinePlayer)) {
                        hologram.show(onlinePlayer, 0);
                    }
                }
                
                sender.sendMessage(ColorUtil.colorize("&a已删除第 " + lineNumber + " 行！"));
            } catch (NumberFormatException e) {
                sender.sendMessage(ColorUtil.colorize("&c行号必须是数字！"));
            }
            
            return true;
        }

        @Override
        public List<String> tabComplete(CommandSender sender, String[] args) {
            if (args.length == 1) {
                return plugin.getHologramManager().getHologramNames().stream()
                        .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (args.length == 2) {
                String hologramName = args[0];
                Hologram hologram = plugin.getHologramManager().getHologram(hologramName);
                if (hologram != null && hologram.getPage(0) != null) {
                    int lineCount = hologram.getPage(0).size();
                    List<String> lineNumbers = new ArrayList<>();
                    for (int i = 1; i <= lineCount; i++) {
                        lineNumbers.add(String.valueOf(i));
                    }
                    return lineNumbers.stream()
                            .filter(n -> n.startsWith(args[1]))
                            .collect(Collectors.toList());
                }
            }
            return new ArrayList<>();
        }
    }

    private class SetLineCommand extends Subcommand {
        public SetLineCommand() {
            super("setline", "设置全息图的一行文本", "wooholograms.admin", Collections.emptyList());
        }

        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (args.length < 3) {
                sender.sendMessage(ColorUtil.colorize("&c用法: /wh setline <名称> <行号> <文本>"));
                return true;
            }
            
            String name = args[0];
            Hologram hologram = plugin.getHologramManager().getHologram(name);
            
            if (hologram == null) {
                sender.sendMessage(ColorUtil.colorize("&c全息图 " + name + " 不存在！"));
                return true;
            }
            
            try {
                int lineNumber = Integer.parseInt(args[1]);
                HologramPage page = hologram.getPage(0);
                
                if (page == null || lineNumber < 1 || lineNumber > page.size()) {
                    sender.sendMessage(ColorUtil.colorize("&c无效的行号！"));
                    return true;
                }
                
                String text = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                page.setLine(lineNumber - 1, text);
                hologram.save();
                
                for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
                    if (hologram.isInDisplayRange(onlinePlayer)) {
                        hologram.show(onlinePlayer, 0);
                    }
                }
                
                sender.sendMessage(ColorUtil.colorize("&a已设置第 " + lineNumber + " 行！"));
            } catch (NumberFormatException e) {
                sender.sendMessage(ColorUtil.colorize("&c行号必须是数字！"));
            }
            
            return true;
        }

        @Override
        public List<String> tabComplete(CommandSender sender, String[] args) {
            if (args.length == 1) {
                return plugin.getHologramManager().getHologramNames().stream()
                        .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (args.length == 2) {
                String hologramName = args[0];
                Hologram hologram = plugin.getHologramManager().getHologram(hologramName);
                if (hologram != null && hologram.getPage(0) != null) {
                    int lineCount = hologram.getPage(0).size();
                    List<String> lineNumbers = new ArrayList<>();
                    for (int i = 1; i <= lineCount; i++) {
                        lineNumbers.add(String.valueOf(i));
                    }
                    return lineNumbers.stream()
                            .filter(n -> n.startsWith(args[1]))
                            .collect(Collectors.toList());
                }
            }
            return new ArrayList<>();
        }
    }

    private class InsertLineCommand extends Subcommand {
        public InsertLineCommand() {
            super("insertline", "在指定位置插入一行", "wooholograms.admin", Collections.emptyList());
        }

        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (args.length < 3) {
                sender.sendMessage(ColorUtil.colorize("&c用法: /wh insertline <名称> <行号> <文本>"));
                return true;
            }
            
            String name = args[0];
            Hologram hologram = plugin.getHologramManager().getHologram(name);
            
            if (hologram == null) {
                sender.sendMessage(ColorUtil.colorize("&c全息图 " + name + " 不存在！"));
                return true;
            }
            
            try {
                int lineNumber = Integer.parseInt(args[1]);
                String text = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                
                HologramPage page = hologram.getPage(0);
                if (page != null) {
                    page.insertLine(lineNumber - 1, text);
                }
                hologram.save();
                
                for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
                    if (hologram.isInDisplayRange(onlinePlayer)) {
                        hologram.show(onlinePlayer, 0);
                    }
                }
                
                sender.sendMessage(ColorUtil.colorize("&a已在第 " + lineNumber + " 行插入新行！"));
            } catch (NumberFormatException e) {
                sender.sendMessage(ColorUtil.colorize("&c行号必须是数字！"));
            }
            
            return true;
        }

        @Override
        public List<String> tabComplete(CommandSender sender, String[] args) {
            if (args.length == 1) {
                return plugin.getHologramManager().getHologramNames().stream()
                        .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (args.length == 2) {
                String hologramName = args[0];
                Hologram hologram = plugin.getHologramManager().getHologram(hologramName);
                if (hologram != null && hologram.getPage(0) != null) {
                    int lineCount = hologram.getPage(0).size();
                    List<String> lineNumbers = new ArrayList<>();
                    for (int i = 1; i <= lineCount + 1; i++) {
                        lineNumbers.add(String.valueOf(i));
                    }
                    return lineNumbers.stream()
                            .filter(n -> n.startsWith(args[1]))
                            .collect(Collectors.toList());
                }
            }
            return new ArrayList<>();
        }
    }

    private class ReloadCommand extends Subcommand {
        public ReloadCommand() {
            super("reload", "重新加载插件配置", "wooholograms.admin", Collections.emptyList());
        }

        @Override
        public boolean execute(CommandSender sender, String[] args) {
            plugin.getConfigManager().reload();
            plugin.getMessages().reload();
            plugin.getStorage().reload();
            plugin.getHologramManager().reload();
            
            sender.sendMessage(ColorUtil.colorize("&a配置已重新加载！"));
            return true;
        }

        @Override
        public List<String> tabComplete(CommandSender sender, String[] args) {
            return new ArrayList<>();
        }
    }

    private class CopyCommand extends Subcommand {
        public CopyCommand() {
            super("copy", "克隆一个全息图", "wooholograms.admin", Collections.emptyList());
        }

        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (args.length < 2) {
                sender.sendMessage(ColorUtil.colorize("&c用法: /wh copy <源名称> <目标名称>"));
                return true;
            }

            String sourceName = args[0];
            String targetName = args[1];

            Hologram source = plugin.getHologramManager().getHologram(sourceName);
            if (source == null) {
                sender.sendMessage(ColorUtil.colorize("&c全息图 " + sourceName + " 不存在！"));
                return true;
            }

            if (plugin.getHologramManager().containsHologram(targetName)) {
                sender.sendMessage(ColorUtil.colorize("&c全息图 " + targetName + " 已存在！"));
                return true;
            }

            Hologram target = plugin.getHologramManager().createHologram(targetName, source.getLocation());
            if (target == null) {
                sender.sendMessage(ColorUtil.colorize("&c创建全息图失败！"));
                return true;
            }

            target.setDisplayRange(source.getDisplayRange());
            target.setUpdateRange(source.getUpdateRange());
            target.setUpdateInterval(source.getUpdateInterval());
            target.setPermission(source.getPermission());
            target.setDefaultVisibleState(source.isDefaultVisibleState());
            target.setDownOrigin(source.isDownOrigin());
            target.setFacing(source.getFacing());

            for (int i = 0; i < source.getPageCount(); i++) {
                HologramPage sourcePage = source.getPage(i);
                HologramPage targetPage;
                
                if (i == 0) {
                    targetPage = target.getPage(0);
                } else {
                    targetPage = target.addPage();
                }

                if (sourcePage != null && targetPage != null) {
                    for (HologramLine sourceLine : sourcePage.getLines()) {
                        HologramLine targetLine = targetPage.addLine(sourceLine.getContent());
                        if (targetLine != null) {
                            targetLine.setOffsetX(sourceLine.getOffsetX());
                            targetLine.setOffsetY(sourceLine.getOffsetY());
                            targetLine.setOffsetZ(sourceLine.getOffsetZ());
                            targetLine.setHeight(sourceLine.getHeight());
                            targetLine.setPermission(sourceLine.getPermission());
                        }
                    }
                    // 复制点击动作
                    for (ClickType clickType : ClickType.values()) {
                        List<Action> actions = sourcePage.getActions(clickType);
                        for (Action action : actions) {
                            targetPage.addAction(clickType, action);
                        }
                    }
                }
            }

            target.save();
            target.showToNearby();

            sender.sendMessage(ColorUtil.colorize("&a成功克隆全息图 " + sourceName + " 到 " + targetName + "！"));
            return true;
        }

        @Override
        public List<String> tabComplete(CommandSender sender, String[] args) {
            if (args.length == 1) {
                return plugin.getHologramManager().getHologramNames().stream()
                        .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
            }
            return new ArrayList<>();
        }
    }

    private class NearCommand extends Subcommand {
        public NearCommand() {
            super("near", "查找附近的全息图", "wooholograms.admin", Collections.emptyList());
            setPlayerOnly(true);
        }

        @Override
        public boolean execute(CommandSender sender, String[] args) {
            Player player = (Player) sender;
            
            int range = 50;
            if (args.length >= 1) {
                try {
                    range = Integer.parseInt(args[0]);
                    if (range <= 0) {
                        player.sendMessage(ColorUtil.colorize("&c范围必须是正整数！"));
                        return true;
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(ColorUtil.colorize("&c无效的范围！"));
                    return true;
                }
            }

            Location playerLoc = player.getLocation();
            List<Map.Entry<Hologram, Double>> nearbyHolograms = new ArrayList<>();

            for (Hologram hologram : plugin.getHologramManager().getHolograms()) {
                Location holoLoc = hologram.getLocation();
                if (holoLoc == null || holoLoc.getWorld() == null) {
                    continue;
                }

                if (!holoLoc.getWorld().equals(playerLoc.getWorld())) {
                    continue;
                }

                double distance = playerLoc.distance(holoLoc);
                if (distance <= range) {
                    nearbyHolograms.add(new AbstractMap.SimpleEntry<>(hologram, distance));
                }
            }

            if (nearbyHolograms.isEmpty()) {
                player.sendMessage(ColorUtil.colorize("&e附近 " + range + " 格内没有全息图。"));
                return true;
            }

            nearbyHolograms.sort(Map.Entry.comparingByValue());

            player.sendMessage(ColorUtil.colorize("&e========== &6附近全息图 (" + range + "格) &e=========="));
            
            for (Map.Entry<Hologram, Double> entry : nearbyHolograms) {
                Hologram hologram = entry.getKey();
                double distance = entry.getValue();
                Location loc = hologram.getLocation();
                
                player.sendMessage(ColorUtil.colorize("&e" + hologram.getName() + 
                        " &7- 距离: " + String.format("%.1f", distance) + " 格" +
                        ", 位置: " + String.format("%.1f, %.1f, %.1f", loc.getX(), loc.getY(), loc.getZ())));
            }
            
            player.sendMessage(ColorUtil.colorize("&e总计: &f" + nearbyHolograms.size() + " 个全息图"));
            return true;
        }

        @Override
        public List<String> tabComplete(CommandSender sender, String[] args) {
            if (args.length == 1) {
                List<String> ranges = Arrays.asList("10", "25", "50", "100", "200");
                return ranges.stream()
                        .filter(r -> r.startsWith(args[0]))
                        .collect(Collectors.toList());
            }
            return new ArrayList<>();
        }
    }

    private class EnableCommand extends Subcommand {
        public EnableCommand() {
            super("enable", "启用一个全息图", "wooholograms.admin", Collections.emptyList());
        }

        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (args.length < 1) {
                sender.sendMessage(ColorUtil.colorize("&c用法: /wh enable <名称>"));
                return true;
            }

            String name = args[0];
            Hologram hologram = plugin.getHologramManager().getHologram(name);

            if (hologram == null) {
                sender.sendMessage(ColorUtil.colorize("&c全息图 " + name + " 不存在！"));
                return true;
            }

            if (hologram.isEnabled()) {
                sender.sendMessage(ColorUtil.colorize("&e全息图 " + name + " 已经是启用状态！"));
                return true;
            }

            hologram.setEnabled(true);
            hologram.save();
            hologram.showToNearby();

            sender.sendMessage(ColorUtil.colorize("&a已启用全息图 " + name + "！"));
            return true;
        }

        @Override
        public List<String> tabComplete(CommandSender sender, String[] args) {
            if (args.length == 1) {
                return plugin.getHologramManager().getHologramNames().stream()
                        .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
            }
            return new ArrayList<>();
        }
    }

    private class DisableCommand extends Subcommand {
        public DisableCommand() {
            super("disable", "禁用一个全息图", "wooholograms.admin", Collections.emptyList());
        }

        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (args.length < 1) {
                sender.sendMessage(ColorUtil.colorize("&c用法: /wh disable <名称>"));
                return true;
            }

            String name = args[0];
            Hologram hologram = plugin.getHologramManager().getHologram(name);

            if (hologram == null) {
                sender.sendMessage(ColorUtil.colorize("&c全息图 " + name + " 不存在！"));
                return true;
            }

            if (!hologram.isEnabled()) {
                sender.sendMessage(ColorUtil.colorize("&e全息图 " + name + " 已经是禁用状态！"));
                return true;
            }

            hologram.setEnabled(false);
            hologram.save();
            hologram.hideFromAll();

            sender.sendMessage(ColorUtil.colorize("&a已禁用全息图 " + name + "！"));
            return true;
        }

        @Override
        public List<String> tabComplete(CommandSender sender, String[] args) {
            if (args.length == 1) {
                return plugin.getHologramManager().getHologramNames().stream()
                        .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
            }
            return new ArrayList<>();
        }
    }

    private class AddPageCommand extends Subcommand {
        public AddPageCommand() {
            super("addpage", "添加一个新页面", "wooholograms.admin", Collections.emptyList());
        }

        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (args.length < 1) {
                sender.sendMessage(ColorUtil.colorize("&c用法: /wh addpage <名称> [内容]"));
                return true;
            }

            String name = args[0];
            Hologram hologram = plugin.getHologramManager().getHologram(name);

            if (hologram == null) {
                sender.sendMessage(ColorUtil.colorize("&c全息图 " + name + " 不存在！"));
                return true;
            }

            HologramPage page = hologram.addPage();
            if (page == null) {
                sender.sendMessage(ColorUtil.colorize("&c添加页面失败！"));
                return true;
            }

            if (args.length > 1) {
                String text = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                page.addLine(text);
            }

            hologram.save();
            sender.sendMessage(ColorUtil.colorize("&a已添加第 " + hologram.getPageCount() + " 页！"));
            return true;
        }

        @Override
        public List<String> tabComplete(CommandSender sender, String[] args) {
            if (args.length == 1) {
                return plugin.getHologramManager().getHologramNames().stream()
                        .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
            }
            return new ArrayList<>();
        }
    }

    private class DeletePageCommand extends Subcommand {
        public DeletePageCommand() {
            super("deletepage", "删除一个页面", "wooholograms.admin", Collections.emptyList());
        }

        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (args.length < 2) {
                sender.sendMessage(ColorUtil.colorize("&c用法: /wh deletepage <名称> <页码>"));
                return true;
            }

            String name = args[0];
            Hologram hologram = plugin.getHologramManager().getHologram(name);

            if (hologram == null) {
                sender.sendMessage(ColorUtil.colorize("&c全息图 " + name + " 不存在！"));
                return true;
            }

            try {
                int pageNumber = Integer.parseInt(args[1]);
                if (pageNumber < 1 || pageNumber > hologram.getPageCount()) {
                    sender.sendMessage(ColorUtil.colorize("&c无效的页码！"));
                    return true;
                }

                if (hologram.getPageCount() <= 1) {
                    sender.sendMessage(ColorUtil.colorize("&c至少需要保留一个页面！"));
                    return true;
                }

                hologram.removePage(pageNumber - 1);
                hologram.save();

                sender.sendMessage(ColorUtil.colorize("&a已删除第 " + pageNumber + " 页！"));
            } catch (NumberFormatException e) {
                sender.sendMessage(ColorUtil.colorize("&c页码必须是数字！"));
            }

            return true;
        }

        @Override
        public List<String> tabComplete(CommandSender sender, String[] args) {
            if (args.length == 1) {
                return plugin.getHologramManager().getHologramNames().stream()
                        .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (args.length == 2) {
                String hologramName = args[0];
                Hologram hologram = plugin.getHologramManager().getHologram(hologramName);
                if (hologram != null) {
                    int pageCount = hologram.getPageCount();
                    List<String> pages = new ArrayList<>();
                    for (int i = 1; i <= pageCount; i++) {
                        pages.add(String.valueOf(i));
                    }
                    return pages.stream()
                            .filter(p -> p.startsWith(args[1]))
                            .collect(Collectors.toList());
                }
            }
            return new ArrayList<>();
        }
    }

    private class SetRangeCommand extends Subcommand {
        public SetRangeCommand() {
            super("setrange", "设置显示范围", "wooholograms.admin", Collections.emptyList());
        }

        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (args.length < 2) {
                sender.sendMessage(ColorUtil.colorize("&c用法: /wh setrange <名称> <范围>"));
                return true;
            }

            String name = args[0];
            Hologram hologram = plugin.getHologramManager().getHologram(name);

            if (hologram == null) {
                sender.sendMessage(ColorUtil.colorize("&c全息图 " + name + " 不存在！"));
                return true;
            }

            try {
                int range = Integer.parseInt(args[1]);
                if (range <= 0) {
                    sender.sendMessage(ColorUtil.colorize("&c范围必须是正整数！"));
                    return true;
                }

                hologram.setDisplayRange(range);
                hologram.save();

                sender.sendMessage(ColorUtil.colorize("&a已将 " + name + " 的显示范围设置为 " + range + " 格！"));
            } catch (NumberFormatException e) {
                sender.sendMessage(ColorUtil.colorize("&c范围必须是数字！"));
            }

            return true;
        }

        @Override
        public List<String> tabComplete(CommandSender sender, String[] args) {
            if (args.length == 1) {
                return plugin.getHologramManager().getHologramNames().stream()
                        .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (args.length == 2) {
                List<String> ranges = Arrays.asList("16", "32", "48", "64", "128");
                return ranges.stream()
                        .filter(r -> r.startsWith(args[1]))
                        .collect(Collectors.toList());
            }
            return new ArrayList<>();
        }
    }

    private class SetIntervalCommand extends Subcommand {
        public SetIntervalCommand() {
            super("setinterval", "设置更新间隔", "wooholograms.admin", Collections.emptyList());
        }

        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (args.length < 2) {
                sender.sendMessage(ColorUtil.colorize("&c用法: /wh setinterval <名称> <tick>"));
                return true;
            }

            String name = args[0];
            Hologram hologram = plugin.getHologramManager().getHologram(name);

            if (hologram == null) {
                sender.sendMessage(ColorUtil.colorize("&c全息图 " + name + " 不存在！"));
                return true;
            }

            try {
                int interval = Integer.parseInt(args[1]);
                if (interval <= 0) {
                    sender.sendMessage(ColorUtil.colorize("&c间隔必须是正整数！"));
                    return true;
                }

                hologram.setUpdateInterval(interval);
                hologram.save();

                sender.sendMessage(ColorUtil.colorize("&a已将 " + name + " 的更新间隔设置为 " + interval + " tick！"));
            } catch (NumberFormatException e) {
                sender.sendMessage(ColorUtil.colorize("&c间隔必须是数字！"));
            }

            return true;
        }

        @Override
        public List<String> tabComplete(CommandSender sender, String[] args) {
            if (args.length == 1) {
                return plugin.getHologramManager().getHologramNames().stream()
                        .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (args.length == 2) {
                List<String> intervals = Arrays.asList("1", "5", "10", "20", "40", "60");
                return intervals.stream()
                        .filter(i -> i.startsWith(args[1]))
                        .collect(Collectors.toList());
            }
            return new ArrayList<>();
        }
    }

    private class SetPermissionCommand extends Subcommand {
        public SetPermissionCommand() {
            super("setpermission", "设置查看权限", "wooholograms.admin", Collections.emptyList());
        }

        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (args.length < 1) {
                sender.sendMessage(ColorUtil.colorize("&c用法: /wh setpermission <名称> [权限]"));
                return true;
            }

            String name = args[0];
            Hologram hologram = plugin.getHologramManager().getHologram(name);

            if (hologram == null) {
                sender.sendMessage(ColorUtil.colorize("&c全息图 " + name + " 不存在！"));
                return true;
            }

            String permission = args.length > 1 ? args[1] : null;
            hologram.setPermission(permission);
            hologram.save();

            if (permission == null || permission.isEmpty()) {
                sender.sendMessage(ColorUtil.colorize("&a已清除 " + name + " 的查看权限！"));
            } else {
                sender.sendMessage(ColorUtil.colorize("&a已将 " + name + " 的查看权限设置为 " + permission + "！"));
            }

            return true;
        }

        @Override
        public List<String> tabComplete(CommandSender sender, String[] args) {
            if (args.length == 1) {
                return plugin.getHologramManager().getHologramNames().stream()
                        .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
            }
            return new ArrayList<>();
        }
    }

    private class AddActionCommand extends Subcommand {
        public AddActionCommand() {
            super("addaction", "添加点击动作", "wooholograms.admin", Collections.emptyList());
        }

        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (args.length < 4) {
                sender.sendMessage(ColorUtil.colorize("&c用法: /wh addaction <名称> <页码> <点击类型> <动作类型> [值]"));
                sender.sendMessage(ColorUtil.colorize("&7点击类型: left, right, shift_left, shift_right"));
                sender.sendMessage(ColorUtil.colorize("&7动作类型: message, command, console, sound, teleport, connect, next_page, prev_page, page"));
                return true;
            }

            String name = args[0];
            Hologram hologram = plugin.getHologramManager().getHologram(name);

            if (hologram == null) {
                sender.sendMessage(ColorUtil.colorize("&c全息图 " + name + " 不存在！"));
                return true;
            }

            try {
                int pageIndex = Integer.parseInt(args[1]) - 1;
                if (pageIndex < 0 || pageIndex >= hologram.getPageCount()) {
                    sender.sendMessage(ColorUtil.colorize("&c无效的页码！"));
                    return true;
                }

                ClickType clickType = ClickType.fromId(args[2]);
                ActionType actionType = ActionType.getByName(args[3]);

                if (actionType == null) {
                    sender.sendMessage(ColorUtil.colorize("&c无效的动作类型！"));
                    return true;
                }

                String data = args.length > 4 ? String.join(" ", Arrays.copyOfRange(args, 4, args.length)) : "";
                Action action = new Action(actionType, data, clickType);

                HologramPage page = hologram.getPage(pageIndex);
                if (page != null) {
                    page.addAction(clickType, action);
                    hologram.save();
                    sender.sendMessage(ColorUtil.colorize("&a已添加动作到第 " + (pageIndex + 1) + " 页！"));
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(ColorUtil.colorize("&c页码必须是数字！"));
            }

            return true;
        }

        @Override
        public List<String> tabComplete(CommandSender sender, String[] args) {
            if (args.length == 1) {
                return plugin.getHologramManager().getHologramNames().stream()
                        .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (args.length == 2) {
                String hologramName = args[0];
                Hologram hologram = plugin.getHologramManager().getHologram(hologramName);
                if (hologram != null) {
                    int pageCount = hologram.getPageCount();
                    List<String> pages = new ArrayList<>();
                    for (int i = 1; i <= pageCount; i++) {
                        pages.add(String.valueOf(i));
                    }
                    return pages.stream()
                            .filter(p -> p.startsWith(args[1]))
                            .collect(Collectors.toList());
                }
            } else if (args.length == 3) {
                return Arrays.asList("left", "right", "shift_left", "shift_right").stream()
                        .filter(c -> c.startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (args.length == 4) {
                return Arrays.asList("message", "command", "console", "sound", "teleport", "connect", "next_page", "prev_page", "page").stream()
                        .filter(a -> a.startsWith(args[3].toLowerCase()))
                        .collect(Collectors.toList());
            }
            return new ArrayList<>();
        }
    }

    private class DeleteActionCommand extends Subcommand {
        public DeleteActionCommand() {
            super("deleteaction", "删除点击动作", "wooholograms.admin", Collections.emptyList());
        }

        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (args.length < 4) {
                sender.sendMessage(ColorUtil.colorize("&c用法: /wh deleteaction <名称> <页码> <点击类型> <索引>"));
                return true;
            }

            String name = args[0];
            Hologram hologram = plugin.getHologramManager().getHologram(name);

            if (hologram == null) {
                sender.sendMessage(ColorUtil.colorize("&c全息图 " + name + " 不存在！"));
                return true;
            }

            try {
                int pageIndex = Integer.parseInt(args[1]) - 1;
                if (pageIndex < 0 || pageIndex >= hologram.getPageCount()) {
                    sender.sendMessage(ColorUtil.colorize("&c无效的页码！"));
                    return true;
                }

                ClickType clickType = ClickType.fromId(args[2]);
                int actionIndex = Integer.parseInt(args[3]) - 1;

                HologramPage page = hologram.getPage(pageIndex);
                if (page != null) {
                    List<Action> actions = page.getActions(clickType);
                    if (actionIndex < 0 || actionIndex >= actions.size()) {
                        sender.sendMessage(ColorUtil.colorize("&c无效的动作索引！"));
                        return true;
                    }

                    page.removeAction(clickType, actionIndex);
                    hologram.save();
                    sender.sendMessage(ColorUtil.colorize("&a已删除第 " + (actionIndex + 1) + " 个动作！"));
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(ColorUtil.colorize("&c页码和索引必须是数字！"));
            }

            return true;
        }

        @Override
        public List<String> tabComplete(CommandSender sender, String[] args) {
            if (args.length == 1) {
                return plugin.getHologramManager().getHologramNames().stream()
                        .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (args.length == 2) {
                String hologramName = args[0];
                Hologram hologram = plugin.getHologramManager().getHologram(hologramName);
                if (hologram != null) {
                    int pageCount = hologram.getPageCount();
                    List<String> pages = new ArrayList<>();
                    for (int i = 1; i <= pageCount; i++) {
                        pages.add(String.valueOf(i));
                    }
                    return pages.stream()
                            .filter(p -> p.startsWith(args[1]))
                            .collect(Collectors.toList());
                }
            } else if (args.length == 3) {
                return Arrays.asList("left", "right", "shift_left", "shift_right").stream()
                        .filter(c -> c.startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            }
            return new ArrayList<>();
        }
    }

    private class ActionsCommand extends Subcommand {
        public ActionsCommand() {
            super("actions", "列出点击动作", "wooholograms.admin", Collections.emptyList());
        }

        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (args.length < 3) {
                sender.sendMessage(ColorUtil.colorize("&c用法: /wh actions <名称> <页码> <点击类型>"));
                return true;
            }

            String name = args[0];
            Hologram hologram = plugin.getHologramManager().getHologram(name);

            if (hologram == null) {
                sender.sendMessage(ColorUtil.colorize("&c全息图 " + name + " 不存在！"));
                return true;
            }

            try {
                int pageIndex = Integer.parseInt(args[1]) - 1;
                if (pageIndex < 0 || pageIndex >= hologram.getPageCount()) {
                    sender.sendMessage(ColorUtil.colorize("&c无效的页码！"));
                    return true;
                }

                ClickType clickType = ClickType.fromId(args[2]);
                HologramPage page = hologram.getPage(pageIndex);

                if (page != null) {
                    List<Action> actions = page.getActions(clickType);
                    sender.sendMessage(ColorUtil.colorize("&e========== &6" + name + " 第" + (pageIndex + 1) + "页 " + clickType.getDescription() + "动作 &e=========="));

                    if (actions.isEmpty()) {
                        sender.sendMessage(ColorUtil.colorize("&7没有动作"));
                    } else {
                        for (int i = 0; i < actions.size(); i++) {
                            Action action = actions.get(i);
                            sender.sendMessage(ColorUtil.colorize("&e" + (i + 1) + ". &f" + action.toString()));
                        }
                    }
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(ColorUtil.colorize("&c页码必须是数字！"));
            }

            return true;
        }

        @Override
        public List<String> tabComplete(CommandSender sender, String[] args) {
            if (args.length == 1) {
                return plugin.getHologramManager().getHologramNames().stream()
                        .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (args.length == 2) {
                String hologramName = args[0];
                Hologram hologram = plugin.getHologramManager().getHologram(hologramName);
                if (hologram != null) {
                    int pageCount = hologram.getPageCount();
                    List<String> pages = new ArrayList<>();
                    for (int i = 1; i <= pageCount; i++) {
                        pages.add(String.valueOf(i));
                    }
                    return pages.stream()
                            .filter(p -> p.startsWith(args[1]))
                            .collect(Collectors.toList());
                }
            } else if (args.length == 3) {
                return Arrays.asList("left", "right", "shift_left", "shift_right").stream()
                        .filter(c -> c.startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            }
            return new ArrayList<>();
        }
    }

    private class OffsetCommand extends Subcommand {
        public OffsetCommand() {
            super("offset", "设置行偏移", "wooholograms.admin", Collections.emptyList());
        }

        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (args.length < 5) {
                sender.sendMessage(ColorUtil.colorize("&c用法: /wh offset <名称> <行号> <x> <y> <z>"));
                return true;
            }

            String name = args[0];
            Hologram hologram = plugin.getHologramManager().getHologram(name);

            if (hologram == null) {
                sender.sendMessage(ColorUtil.colorize("&c全息图 " + name + " 不存在！"));
                return true;
            }

            try {
                int lineNumber = Integer.parseInt(args[1]);
                double offsetX = Double.parseDouble(args[2]);
                double offsetY = Double.parseDouble(args[3]);
                double offsetZ = Double.parseDouble(args[4]);

                HologramPage page = hologram.getPage(0);
                if (page == null || lineNumber < 1 || lineNumber > page.size()) {
                    sender.sendMessage(ColorUtil.colorize("&c无效的行号！"));
                    return true;
                }

                HologramLine line = page.getLine(lineNumber - 1);
                if (line != null) {
                    line.setOffsetX(offsetX);
                    line.setOffsetY(offsetY);
                    line.setOffsetZ(offsetZ);
                    hologram.save();
                    hologram.realignLines();

                    sender.sendMessage(ColorUtil.colorize("&a已设置第 " + lineNumber + " 行的偏移为 (" + offsetX + ", " + offsetY + ", " + offsetZ + ")！"));
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(ColorUtil.colorize("&c行号和偏移值必须是数字！"));
            }

            return true;
        }

        @Override
        public List<String> tabComplete(CommandSender sender, String[] args) {
            if (args.length == 1) {
                return plugin.getHologramManager().getHologramNames().stream()
                        .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (args.length == 2) {
                String hologramName = args[0];
                Hologram hologram = plugin.getHologramManager().getHologram(hologramName);
                if (hologram != null && hologram.getPage(0) != null) {
                    int lineCount = hologram.getPage(0).size();
                    List<String> lineNumbers = new ArrayList<>();
                    for (int i = 1; i <= lineCount; i++) {
                        lineNumbers.add(String.valueOf(i));
                    }
                    return lineNumbers.stream()
                            .filter(n -> n.startsWith(args[1]))
                            .collect(Collectors.toList());
                }
            } else if (args.length >= 3 && args.length <= 5) {
                return Arrays.asList("0", "0.25", "0.5", "1", "-0.25", "-0.5", "-1").stream()
                        .filter(v -> v.startsWith(args[args.length - 1]))
                        .collect(Collectors.toList());
            }
            return new ArrayList<>();
        }
    }

    private class HeightCommand extends Subcommand {
        public HeightCommand() {
            super("height", "设置行高度", "wooholograms.admin", Collections.emptyList());
        }

        @Override
        public boolean execute(CommandSender sender, String[] args) {
            if (args.length < 3) {
                sender.sendMessage(ColorUtil.colorize("&c用法: /wh height <名称> <行号> <高度>"));
                return true;
            }

            String name = args[0];
            Hologram hologram = plugin.getHologramManager().getHologram(name);

            if (hologram == null) {
                sender.sendMessage(ColorUtil.colorize("&c全息图 " + name + " 不存在！"));
                return true;
            }

            try {
                int lineNumber = Integer.parseInt(args[1]);
                double height = Double.parseDouble(args[2]);

                HologramPage page = hologram.getPage(0);
                if (page == null || lineNumber < 1 || lineNumber > page.size()) {
                    sender.sendMessage(ColorUtil.colorize("&c无效的行号！"));
                    return true;
                }

                HologramLine line = page.getLine(lineNumber - 1);
                if (line != null) {
                    line.setHeight(height);
                    hologram.save();
                    hologram.realignLines();

                    sender.sendMessage(ColorUtil.colorize("&a已设置第 " + lineNumber + " 行的高度为 " + height + "！"));
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(ColorUtil.colorize("&c行号和高度必须是数字！"));
            }

            return true;
        }

        @Override
        public List<String> tabComplete(CommandSender sender, String[] args) {
            if (args.length == 1) {
                return plugin.getHologramManager().getHologramNames().stream()
                        .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (args.length == 2) {
                String hologramName = args[0];
                Hologram hologram = plugin.getHologramManager().getHologram(hologramName);
                if (hologram != null && hologram.getPage(0) != null) {
                    int lineCount = hologram.getPage(0).size();
                    List<String> lineNumbers = new ArrayList<>();
                    for (int i = 1; i <= lineCount; i++) {
                        lineNumbers.add(String.valueOf(i));
                    }
                    return lineNumbers.stream()
                            .filter(n -> n.startsWith(args[1]))
                            .collect(Collectors.toList());
                }
            } else if (args.length == 3) {
                return Arrays.asList("0.2", "0.25", "0.3", "0.5").stream()
                        .filter(v -> v.startsWith(args[2]))
                        .collect(Collectors.toList());
            }
            return new ArrayList<>();
        }
    }

    private class HelpCommand extends Subcommand {
        public HelpCommand() {
            super("help", "显示帮助信息", null, Collections.emptyList());
        }

        @Override
        public boolean execute(CommandSender sender, String[] args) {
            sendHelp(sender);
            return true;
        }

        @Override
        public List<String> tabComplete(CommandSender sender, String[] args) {
            return new ArrayList<>();
        }
    }

    private class GuiCommand extends Subcommand {
        public GuiCommand() {
            super("gui", "打开GUI管理界面", "wooholograms.admin", Collections.emptyList());
            setPlayerOnly(true);
        }

        @Override
        public boolean execute(CommandSender sender, String[] args) {
            Player player = (Player) sender;
            GuiManager guiManager = plugin.getGuiManager();
            ChatInputManager chatInputManager = plugin.getChatInputManager();
            
            if (args.length >= 1) {
                String hologramName = args[0];
                Hologram hologram = plugin.getHologramManager().getHologram(hologramName);
                
                if (hologram == null) {
                    player.sendMessage(ColorUtil.colorize("&c全息图 " + hologramName + " 不存在！"));
                    return true;
                }
                
                guiManager.openGui(player, new HologramDetailGui(plugin, guiManager, chatInputManager, hologramName, 0));
            } else {
                guiManager.openGui(player, new HologramListGui(plugin, guiManager, chatInputManager, 0));
            }
            
            return true;
        }

        @Override
        public List<String> tabComplete(CommandSender sender, String[] args) {
            if (args.length == 1) {
                return plugin.getHologramManager().getHologramNames().stream()
                        .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
            }
            return new ArrayList<>();
        }
    }
}
