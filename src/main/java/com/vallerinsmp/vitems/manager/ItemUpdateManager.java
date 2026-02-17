package com.vallerinsmp.vitems.manager;

import com.vallerinsmp.vitems.VItems;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Progressively updates existing custom items when config is reloaded
 * to prevent lag spikes
 */
public class ItemUpdateManager {

    private final VItems plugin;

    public ItemUpdateManager(VItems plugin) {
        this.plugin = plugin;
    }

    /**
     * Update all custom items for online players progressively
     */
    public void updateAllItems() {
        if (!plugin.getConfigManager().getBoolean("settings.update-items-on-reload", true)) {
            return;
        }

        int batchSize = plugin.getConfigManager().getInt("settings.update-batch-size", 50);
        List<ItemStack> itemsToUpdate = collectCustomItems();

        if (itemsToUpdate.isEmpty()) {
            return;
        }

        // Update items in batches to prevent lag
        new BukkitRunnable() {
            int index = 0;

            @Override
            public void run() {
                int processed = 0;

                while (index < itemsToUpdate.size() && processed < batchSize) {
                    ItemStack item = itemsToUpdate.get(index);
                    plugin.getItemManager().updateItem(item);
                    index++;
                    processed++;
                }

                if (index >= itemsToUpdate.size()) {
                    this.cancel();

                    if (plugin.getConfigManager().getBoolean("settings.debug", false)) {
                        plugin.getLogger().info("Updated " + itemsToUpdate.size() + " custom items");
                    }
                }
            }
        }.runTaskTimer(plugin, 1L, 1L); // Run every tick
    }

    /**
     * Collect all custom items from online players
     */
    private List<ItemStack> collectCustomItems() {
        List<ItemStack> items = new ArrayList<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            // Check main inventory
            for (ItemStack item : player.getInventory().getContents()) {
                if (plugin.getItemManager().isCustomItem(item)) {
                    items.add(item);
                }
            }

            // Check armor
            for (ItemStack item : player.getInventory().getArmorContents()) {
                if (plugin.getItemManager().isCustomItem(item)) {
                    items.add(item);
                }
            }

            // Check off-hand
            ItemStack offHand = player.getInventory().getItemInOffHand();
            if (plugin.getItemManager().isCustomItem(offHand)) {
                items.add(offHand);
            }
        }

        return items;
    }
}
