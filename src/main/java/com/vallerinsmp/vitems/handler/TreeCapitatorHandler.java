package com.vallerinsmp.vitems.handler;

import com.vallerinsmp.vitems.VItems;
import com.vallerinsmp.vitems.util.ItemType;
import com.vallerinsmp.vitems.util.WorldGuardHook;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Handler for Tree Capitator Axe
 * Progressively chops down entire trees with animation
 */
public class TreeCapitatorHandler extends ToolHandler {

    private final Set<UUID> activeCapitators = new HashSet<>();

    public TreeCapitatorHandler(VItems plugin) {
        super(plugin, ItemType.TREE_CAPITATOR);
    }

    @Override
    public void handleBlockBreak(Player player, Block centerBlock, ItemStack item) {
        if (!canUse(player, centerBlock)) {
            return;
        }

        // Check if player already has active tree capitator
        if (activeCapitators.contains(player.getUniqueId())) {
            return;
        }

        // Only work on logs
        if (!isLog(centerBlock.getType())) {
            return;
        }

        Material logType = centerBlock.getType();

        // Find all connected logs
        List<Block> tree = findTree(centerBlock, logType, getMaxBlocks());

        if (tree.isEmpty()) {
            return;
        }

        // Mark player as actively chopping
        activeCapitators.add(player.getUniqueId());

        // Apply cooldown immediately
        applyCooldown(player);

        boolean isCreative = player.getGameMode() == GameMode.CREATIVE;
        int delayTicks = plugin.getConfigManager().getInt("tools.tree_capitator.animation-delay-ticks", 2);

        // Progressive breaking animation
        new BukkitRunnable() {
            int index = 0;

            @Override
            public void run() {
                if (index >= tree.size()) {
                    activeCapitators.remove(player.getUniqueId());
                    this.cancel();
                    return;
                }

                Block block = tree.get(index);

                // Check WorldGuard before breaking
                if (!WorldGuardHook.canBreak(player, block)) {
                    index++;
                    return;
                }

                // Get drops
                if (!isCreative) {
                    Collection<ItemStack> drops = block.getDrops(item);
                    for (ItemStack drop : drops) {
                        block.getWorld().dropItemNaturally(block.getLocation(), drop);
                    }

                    // Apply durability
                    applyDurability(item, 1);
                }

                // Play break sound based on wood type
                Sound breakSound = getBreakSoundForLog(block.getType());
                block.getWorld().playSound(block.getLocation(), breakSound, 1.0f, 1.0f);

                // Break block
                block.setType(Material.AIR);

                index++;
            }
        }.runTaskTimer(plugin, 0L, delayTicks);
    }

    /**
     * Find all connected logs using BFS
     */
    private List<Block> findTree(Block start, Material logType, int maxBlocks) {
        List<Block> tree = new ArrayList<>();
        Set<Block> visited = new HashSet<>();
        Queue<Block> queue = new LinkedList<>();

        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty() && tree.size() < maxBlocks) {
            Block current = queue.poll();
            tree.add(current);

            // Check all 26 surrounding blocks (3x3x3 minus center)
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && y == 0 && z == 0)
                            continue;

                        Block neighbor = current.getRelative(x, y, z);

                        // Only add logs of the same type
                        if (!visited.contains(neighbor) && neighbor.getType() == logType) {
                            visited.add(neighbor);
                            queue.add(neighbor);
                        }
                    }
                }
            }
        }

        return tree;
    }

    /**
     * Check if material is a log
     */
    private boolean isLog(Material material) {
        return Tag.LOGS.isTagged(material);
    }

    /**
     * Get break sound for specific log type
     */
    private Sound getBreakSoundForLog(Material logType) {
        // All wood types use the same break sound in modern versions
        return Sound.BLOCK_WOOD_BREAK;
    }

    /**
     * Get active capitators set for cleanup
     */
    public Set<UUID> getActiveCapitators() {
        return activeCapitators;
    }
}
