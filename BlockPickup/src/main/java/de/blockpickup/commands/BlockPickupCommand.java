package de.blockpickup.commands;

import de.blockpickup.BlockPickupPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class BlockPickupCommand implements CommandExecutor, TabCompleter {

    private final BlockPickupPlugin plugin;

    public BlockPickupCommand(BlockPickupPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                if (!sender.hasPermission("blockpickup.admin")) {
                    sender.sendMessage(plugin.getMessage("no-permission"));
                    return true;
                }
                plugin.reloadPluginConfig();
                sender.sendMessage(plugin.getMessage("config-reloaded"));
                return true;

            case "help":
                sendHelp(sender);
                return true;

            case "info":
                sendInfo(sender);
                return true;

            default:
                sendHelp(sender);
                return true;
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6§l=== BlockPickup Hilfe ===");
        sender.sendMessage("§e/blockpickup reload §7- Lade die Konfiguration neu");
        sender.sendMessage("§e/blockpickup info §7- Zeige Plugin-Informationen");
        sender.sendMessage("§e/blockpickup help §7- Zeige diese Hilfe");
        sender.sendMessage("");
        sender.sendMessage("§7Halte Shift/Schleichen und breche einen Container ab,");
        sender.sendMessage("§7um ihn mit Inhalt aufzuheben!");
    }

    private void sendInfo(CommandSender sender) {
        sender.sendMessage("§6§l=== BlockPickup Info ===");
        sender.sendMessage("§7Version: §e" + plugin.getDescription().getVersion());
        sender.sendMessage("§7Autor: §e" + plugin.getDescription().getAuthors());
        sender.sendMessage("");
        sender.sendMessage("§7Blöcke aktiviert: §e" + plugin.getConfigManager().areBlocksEnabled());
        sender.sendMessage("§7Entities aktiviert: §e" + plugin.getConfigManager().areEntitiesEnabled());
        sender.sendMessage("§7Sneaken erforderlich: §e" + plugin.getConfigManager().requiresSneak());
        sender.sendMessage("§7Leere Hand erforderlich: §e" + plugin.getConfigManager().requiresEmptyHand());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("reload");
            completions.add("help");
            completions.add("info");
        }

        return completions;
    }
}
