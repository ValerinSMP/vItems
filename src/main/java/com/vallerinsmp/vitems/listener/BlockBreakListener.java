package com.vallerinsmp.vitems.listener;

import com.vallerinsmp.vitems.VItems;
import com.vallerinsmp.vitems.handler.*;
import com.vallerinsmp.vitems.util.ItemType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Main listener for block break events
 * Delegates to appropriate tool handlers
 */
public class BlockBreakListener implements Listener {

    private final VItems plugin;
    private final Pickaxe3x3Handler pickaxe3x3Handler;
    private final Shovel3x3Handler shovel3x3Handler;
    private final VeinminerHandler veinminerHandler;
    private final TreeCapitatorHandler treeCapitatorHandler;

    public BlockBreakListener(VItems plugin) {
        this.plugin = plugin;
        this.pickaxe3x3Handler = new Pickaxe3x3Handler(plugin);
        this.shovel3x3Handler = new Shovel3x3Handler(plugin);
        this.veinminerHandler = new VeinminerHandler(plugin);
        this.treeCapitatorHandler = new TreeCapitatorHandler(plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Check if item is a custom tool
        ItemType itemType = plugin.getItemManager().getItemType(item);
        if (itemType == null) {
            return;
        }

        // Cancel the original event to handle it ourselves
        event.setCancelled(true);

        // Delegate to appropriate handler
        switch (itemType) {
            case PICKAXE_3X3 -> pickaxe3x3Handler.handleBlockBreak(player, event.getBlock(), item);
            case SHOVEL_3X3 -> shovel3x3Handler.handleBlockBreak(player, event.getBlock(), item);
            case VEINMINER -> veinminerHandler.handleBlockBreak(player, event.getBlock(), item);
            case TREE_CAPITATOR -> treeCapitatorHandler.handleBlockBreak(player, event.getBlock(), item);
        }
    }

    /**
     * Get veinminer handler for cleanup
     */
    public VeinminerHandler getVeinminerHandler() {
        return veinminerHandler;
    }

    /**
     * Get tree capitator handler for cleanup
     */
    public TreeCapitatorHandler getTreeCapitatorHandler() {
        return treeCapitatorHandler;
    }
}
