package com.vallerinsmp.vitems.util;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * WorldGuard integration hook
 * Gracefully handles WorldGuard soft dependency
 */
public class WorldGuardHook {

    private static boolean worldGuardEnabled = false;

    /**
     * Initialize WorldGuard hook
     */
    public static void initialize() {
        try {
            Class.forName("com.sk89q.worldguard.WorldGuard");
            worldGuardEnabled = true;
        } catch (ClassNotFoundException e) {
            worldGuardEnabled = false;
        }
    }

    /**
     * Check if WorldGuard is enabled
     */
    public static boolean isEnabled() {
        return worldGuardEnabled;
    }

    /**
     * Check if player can break block at location
     * Returns true if WorldGuard is not enabled or player has permission
     */
    public static boolean canBreak(Player player, Block block) {
        if (!worldGuardEnabled) {
            return true;
        }

        try {
            Location loc = block.getLocation();
            RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
            com.sk89q.worldedit.util.Location wgLoc = BukkitAdapter.adapt(loc);

            return query.testState(wgLoc, WorldGuardPlugin.inst().wrapPlayer(player), Flags.BLOCK_BREAK);
        } catch (Exception e) {
            // If any error occurs, default to allowing the action
            return true;
        }
    }
}
