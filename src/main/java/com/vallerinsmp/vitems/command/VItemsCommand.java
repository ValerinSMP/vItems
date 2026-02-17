package com.vallerinsmp.vitems.command;

import com.vallerinsmp.vitems.VItems;
import com.vallerinsmp.vitems.util.ItemType;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Main command handler for /vitems
 */
public class VItemsCommand implements CommandExecutor, TabCompleter {

    private final VItems plugin;

    public VItemsCommand(VItems plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {

        // No arguments - show usage
        if (args.length == 0) {
            sender.sendMessage(plugin.getConfigManager().getMessage("command.usage"));
            return true;
        }

        // Subcommands
        switch (args[0].toLowerCase()) {
            case "give" -> {
                return handleGive(sender, args);
            }
            case "reload" -> {
                return handleReload(sender);
            }
            case "list" -> {
                return handleList(sender);
            }
            default -> {
                sender.sendMessage(plugin.getConfigManager().getMessage("command.usage"));
                return true;
            }
        }
    }

    /**
     * Handle /vitems give <player> <item>
     */
    private boolean handleGive(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(plugin.getConfigManager().getMessage("command.give-usage"));
            return true;
        }

        // Get player
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(plugin.getConfigManager().getMessage("command.player-not-found"));
            return true;
        }

        // Get item type
        ItemType itemType = ItemType.fromConfigKey(args[2]);
        if (itemType == null) {
            sender.sendMessage(plugin.getConfigManager().getMessage("command.invalid-item"));
            return true;
        }

        // Give item
        plugin.getItemManager().giveItem(target, itemType);

        // Send messages
        Map<String, String> placeholders = Map.of(
                "%item%", itemType.getConfigKey(),
                "%player%", target.getName());
        sender.sendMessage(plugin.getConfigManager().getMessage("command.item-given", placeholders));
        target.sendMessage(plugin.getConfigManager().getMessage("command.item-received",
                Map.of("%item%", itemType.getConfigKey())));

        return true;
    }

    /**
     * Handle /vitems reload
     */
    private boolean handleReload(CommandSender sender) {
        plugin.getConfigManager().reload();
        plugin.getItemUpdateManager().updateAllItems();

        sender.sendMessage(plugin.getConfigManager().getMessage("general.config-reloaded"));
        return true;
    }

    /**
     * Handle /vitems list
     */
    private boolean handleList(CommandSender sender) {
        sender.sendMessage(plugin.getConfigManager().getMessage("command.list-header"));

        for (ItemType type : ItemType.values()) {
            String namePath = "items." + type.getConfigKey() + ".name";
            String name = plugin.getConfigManager().getRawMessage(namePath);

            Map<String, String> placeholders = Map.of(
                    "%item%", type.getConfigKey(),
                    "%description%", name);
            sender.sendMessage(plugin.getConfigManager().getMessage("command.list-item", placeholders));
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
            @NotNull String label, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Subcommands
            completions.addAll(Arrays.asList("give", "reload", "list"));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            // Player names
            completions.addAll(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList()));
        } else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            // Item types
            completions.addAll(Arrays.stream(ItemType.values())
                    .map(ItemType::getConfigKey)
                    .collect(Collectors.toList()));
        }

        // Filter by current input
        String currentArg = args[args.length - 1].toLowerCase();
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(currentArg))
                .collect(Collectors.toList());
    }
}
