package com.vallerinsmp.vitems.manager;

import com.vallerinsmp.vitems.util.ItemType;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages cooldowns for custom tools
 */
public class CooldownManager {

    private final Map<UUID, Map<ItemType, Long>> cooldowns;

    public CooldownManager() {
        this.cooldowns = new ConcurrentHashMap<>();
    }

    /**
     * Check if player has cooldown for specific item type
     */
    public boolean hasCooldown(Player player, ItemType type) {
        Map<ItemType, Long> playerCooldowns = cooldowns.get(player.getUniqueId());
        if (playerCooldowns == null) {
            return false;
        }

        Long expiry = playerCooldowns.get(type);
        if (expiry == null) {
            return false;
        }

        if (System.currentTimeMillis() >= expiry) {
            playerCooldowns.remove(type);
            return false;
        }

        return true;
    }

    /**
     * Set cooldown for player and item type
     */
    public void setCooldown(Player player, ItemType type, double seconds) {
        long expiryTime = System.currentTimeMillis() + (long) (seconds * 1000);

        cooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
                .put(type, expiryTime);
    }

    /**
     * Get remaining cooldown time in seconds
     */
    public double getRemainingCooldown(Player player, ItemType type) {
        Map<ItemType, Long> playerCooldowns = cooldowns.get(player.getUniqueId());
        if (playerCooldowns == null) {
            return 0;
        }

        Long expiry = playerCooldowns.get(type);
        if (expiry == null) {
            return 0;
        }

        long remaining = expiry - System.currentTimeMillis();
        if (remaining <= 0) {
            playerCooldowns.remove(type);
            return 0;
        }

        return remaining / 1000.0;
    }

    /**
     * Clear all cooldowns for a player
     */
    public void clearCooldowns(Player player) {
        cooldowns.remove(player.getUniqueId());
    }

    /**
     * Clear all cooldowns
     */
    public void clearAll() {
        cooldowns.clear();
    }

    /**
     * Cleanup expired cooldowns (called periodically)
     */
    public void cleanup() {
        long now = System.currentTimeMillis();
        cooldowns.values().forEach(map -> map.entrySet().removeIf(entry -> entry.getValue() <= now));
        cooldowns.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }
}
