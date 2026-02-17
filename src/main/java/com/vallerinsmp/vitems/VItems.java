package com.vallerinsmp.vitems;

import com.vallerinsmp.vitems.command.VItemsCommand;
import com.vallerinsmp.vitems.listener.BlockBreakListener;
import com.vallerinsmp.vitems.manager.ConfigManager;
import com.vallerinsmp.vitems.manager.CooldownManager;
import com.vallerinsmp.vitems.manager.ItemManager;
import com.vallerinsmp.vitems.manager.ItemUpdateManager;
import com.vallerinsmp.vitems.util.WorldGuardHook;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

/**
 * vItems - Custom Tools Plugin for ValerinSMP
 * 
 * Author: mtynnn
 * Organization: ValerinSMP
 * API: Paper 1.21
 * Java: 21
 * 
 * Features:
 * - 3x3 Pickaxe & Shovel (instant breaking)
 * - Veinminer Pickaxe (progressive ore mining)
 * - Tree Capitator Axe (progressive tree chopping)
 * - Full PlugMan reload support
 * - WorldGuard integration
 */
public final class VItems extends JavaPlugin {

    // Managers
    private ConfigManager configManager;
    private CooldownManager cooldownManager;
    private ItemManager itemManager;
    private ItemUpdateManager itemUpdateManager;

    // Listeners
    private BlockBreakListener blockBreakListener;

    // Tasks for cleanup
    private final List<BukkitTask> activeTasks = new ArrayList<>();

    @Override
    public void onEnable() {
        // Initialize WorldGuard hook
        WorldGuardHook.initialize();

        // Initialize managers
        this.configManager = new ConfigManager(this);
        this.cooldownManager = new CooldownManager();
        this.itemManager = new ItemManager(this);
        this.itemUpdateManager = new ItemUpdateManager(this);

        // Register listeners
        this.blockBreakListener = new BlockBreakListener(this);
        getServer().getPluginManager().registerEvents(blockBreakListener, this);

        // Register commands
        PluginCommand vitemsCmd = getCommand("vitems");
        if (vitemsCmd != null) {
            VItemsCommand commandExecutor = new VItemsCommand(this);
            vitemsCmd.setExecutor(commandExecutor);
            vitemsCmd.setTabCompleter(commandExecutor);
        }

        // Start cooldown cleanup task (runs every 30 seconds)
        BukkitTask cleanupTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
            cooldownManager.cleanup();
        }, 600L, 600L); // 30 seconds in ticks
        activeTasks.add(cleanupTask);

        // Log startup
        getLogger().info("vItems has been enabled!");
        getLogger().info("Author: mtynnn | Organization: ValerinSMP");
        if (WorldGuardHook.isEnabled()) {
            getLogger().info("WorldGuard integration enabled");
        }
    }

    @Override
    public void onDisable() {
        // Cancel all active tasks (PlugMan support)
        for (BukkitTask task : activeTasks) {
            if (task != null && !task.isCancelled()) {
                task.cancel();
            }
        }
        activeTasks.clear();

        // Cancel any active veinminer/tree capitator animations
        if (blockBreakListener != null) {
            blockBreakListener.getVeinminerHandler().getActiveVeinminers().clear();
            blockBreakListener.getTreeCapitatorHandler().getActiveCapitators().clear();
        }

        // Clear all cooldowns
        if (cooldownManager != null) {
            cooldownManager.clearAll();
        }

        // Unregister all listeners (PlugMan support)
        Bukkit.getScheduler().cancelTasks(this);

        getLogger().info("vItems has been disabled!");
    }

    // Getter methods for managers
    public ConfigManager getConfigManager() {
        return configManager;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public ItemManager getItemManager() {
        return itemManager;
    }

    public ItemUpdateManager getItemUpdateManager() {
        return itemUpdateManager;
    }
}
