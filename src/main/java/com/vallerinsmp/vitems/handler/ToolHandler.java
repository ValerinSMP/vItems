package com.vallerinsmp.vitems.handler;

import com.vallerinsmp.vitems.VItems;
import com.vallerinsmp.vitems.util.ItemType;
import com.vallerinsmp.vitems.util.WorldGuardHook;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

/**
 * Base handler for all custom tools
 */
public abstract class ToolHandler {

    protected final VItems plugin;
    protected final ItemType itemType;

    public ToolHandler(VItems plugin, ItemType itemType) {
        this.plugin = plugin;
        this.itemType = itemType;
    }

    /**
     * Check if player can use this tool
     */
    public boolean canUse(Player player, Block block) {
        // Check if tool is enabled
        String enabledPath = "tools." + itemType.getConfigKey() + ".enabled";
        if (!plugin.getConfigManager().getBoolean(enabledPath, true)) {
            Map<String, String> placeholders = Map.of();
            player.sendMessage(plugin.getConfigManager().getMessage("tool.disabled", placeholders));
            return false;
        }

        // Check cooldown
        if (hasCooldown() && plugin.getCooldownManager().hasCooldown(player, itemType)) {
            double remaining = plugin.getCooldownManager().getRemainingCooldown(player, itemType);
            Map<String, String> placeholders = Map.of("%time%", String.format("%.1f", remaining));
            player.sendMessage(plugin.getConfigManager().getMessage("tool.cooldown", placeholders));
            return false;
        }

        // Check WorldGuard
        if (!WorldGuardHook.canBreak(player, block)) {
            Map<String, String> placeholders = Map.of();
            player.sendMessage(plugin.getConfigManager().getMessage("tool.worldguard-protection", placeholders));
            return false;
        }

        return true;
    }

    /**
     * Apply durability damage to item
     */
    protected void applyDurability(ItemStack item, int damage) {
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof Damageable damageable) {
            int currentDamage = damageable.getDamage();
            damageable.setDamage(currentDamage + damage);

            // Check if item should break
            if (damageable.getDamage() >= item.getType().getMaxDurability()) {
                item.setAmount(0); // Break the item
            } else {
                item.setItemMeta(meta);
            }
        }
    }

    /**
     * Check if this tool has a cooldown
     */
    protected boolean hasCooldown() {
        String cooldownPath = "tools." + itemType.getConfigKey() + ".has-cooldown";
        return plugin.getConfigManager().getBoolean(cooldownPath, false);
    }

    /**
     * Get cooldown duration for this tool
     */
    protected double getCooldownDuration() {
        String cooldownPath = "tools." + itemType.getConfigKey() + ".cooldown-seconds";
        return plugin.getConfigManager().getDouble(cooldownPath, 0.0);
    }

    /**
     * Get max blocks for this tool
     */
    protected int getMaxBlocks() {
        String maxBlocksPath = "tools." + itemType.getConfigKey() + ".max-blocks";
        return plugin.getConfigManager().getInt(maxBlocksPath, 9);
    }

    /**
     * Apply cooldown to player
     */
    protected void applyCooldown(Player player) {
        if (hasCooldown()) {
            double duration = getCooldownDuration();
            plugin.getCooldownManager().setCooldown(player, itemType, duration);
        }
    }

    /**
     * Handle block break event
     * Must be implemented by each tool
     */
    public abstract void handleBlockBreak(Player player, Block block, ItemStack item);
}
