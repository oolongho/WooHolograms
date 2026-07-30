package com.oolongho.holograms.command;

import com.oolongho.holograms.WooHolograms;
import com.oolongho.holograms.command.subcommand.*;
import com.oolongho.holograms.gui.HologramListGui;
import com.oolongho.holograms.gui.HologramListGui.SortType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HologramCommand implements CommandExecutor, TabCompleter {

    private final WooHolograms plugin;
    private final Map<String, Subcommand> subcommandMap;

    public HologramCommand(WooHolograms plugin) {
        this.plugin = plugin;
        this.subcommandMap = new LinkedHashMap<>();
        registerSubcommands();
    }

    private void registerSubcommands() {
        register(new CreateCommand(plugin));
        register(new DeleteCommand(plugin));
        register(new CopyCommand(plugin));
        register(new NearCommand(plugin));
        register(new EnableCommand(plugin));
        register(new DisableCommand(plugin));
        register(new ListCommand(plugin));
        register(new InfoCommand(plugin));
        register(new TeleportCommand(plugin));
        register(new MoveHereCommand(plugin));
        register(new MoveToCommand(plugin));
        register(new AddLineCommand(plugin));
        register(new RemoveLineCommand(plugin));
        register(new SetLineCommand(plugin));
        register(new InsertLineCommand(plugin));
        register(new AddPageCommand(plugin));
        register(new RemovePageCommand(plugin));
        register(new SwapPageCommand(plugin));
        register(new SetRangeCommand(plugin));
        register(new SetIntervalCommand(plugin));
        register(new SetPermissionCommand(plugin));
        register(new SetFacingCommand(plugin));
        register(new SetDoubleSidedCommand(plugin));
        register(new AddActionCommand(plugin));
        register(new DeleteActionCommand(plugin));
        register(new ActionsCommand(plugin));
        register(new OffsetCommand(plugin));
        register(new HeightCommand(plugin));
        register(new ReloadCommand(plugin));
        register(new SetPageCommand(plugin));
        register(new ConvertCommand(plugin));
        register(new GuiCommand(plugin));
        register(new ProfilerCommand(plugin));
        register(new SetScaleCommand(plugin));
        register(new SetShadowCommand(plugin));
        register(new SetGlowColorCommand(plugin));
        register(new SetChromaCommand(plugin));
        register(new HelpCommand(plugin, subcommandMap));
    }

    private void register(Subcommand subcommand) {
        subcommandMap.put(subcommand.getName().toLowerCase(), subcommand);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (player.hasPermission("wooholograms.command.gui")) {
                    plugin.getGuiManager().openGui(player, new HologramListGui(plugin, plugin.getGuiManager(), plugin.getChatInputManager(), 0, SortType.NAME, player));
                } else {
                    plugin.getMessages().send(sender, "general.no-permission-cmd");
                }
            } else {
                subcommandMap.get("help").execute(sender, new String[0]);
            }
            return true;
        }

        String subcommandName = args[0].toLowerCase();
        Subcommand subcommand = subcommandMap.get(subcommandName);

        if (subcommand == null) {
            plugin.getMessages().send(sender, "general.unknown-command");
            return true;
        }

        if (subcommand.isPlayerOnly() && !(sender instanceof Player)) {
            plugin.getMessages().send(sender, "general.player-only");
            return true;
        }

        if (!subcommand.hasPermission(sender)) {
            plugin.getMessages().send(sender, "general.no-permission-cmd");
            return true;
        }

        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        return subcommand.execute(sender, subArgs);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return subcommandMap.values().stream()
                    .filter(sub -> sub.hasPermission(sender))
                    .map(Subcommand::getName)
                    .filter(name -> name.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length > 1) {
            Subcommand subcommand = subcommandMap.get(args[0].toLowerCase());
            if (subcommand != null) {
                String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                return subcommand.tabComplete(sender, subArgs);
            }
        }
        return Collections.emptyList();
    }
}
