package com.vallerinsmp.vitems.handler;

import com.vallerinsmp.vitems.VItems;
import com.vallerinsmp.vitems.util.ItemType;
import com.vallerinsmp.vitems.util.WorldGuardHook;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Handler for Veinminer Pickaxe
 * Progressively mines connected ore veins with animation
 */
public class VeinminerHandler extends ToolHandler {

    private final Set<UUID> activeVeinminers = new HashSet<>();

    public VeinminerHandler(VItems plugin) {
        super(plugin, ItemType.VEINMINER);
    }

    @Override
    public void handleBlockBreak(Player player, Block startBlock, ItemStack item,
            org.bukkit.block.BlockFace clickedFace) {
        if (!canUse(player, startBlock)) {
            return;
        }

        // Check if player already has active veinminer
        if (activeVeinminers.contains(player.getUniqueId())) {
            return;
        }

        Material oreType = startBlock.getType();

        // Only work on ores
        if (!isOre(oreType)) {
            return;
        }

        // Find all connected ore blocks
        List<Block> vein = findVein(startBlock, oreType, getMaxBlocks());

        if (vein.isEmpty()) {
            return;
        }

        // Mark player as actively veinmining
        activeVeinminers.add(player.getUniqueId());

        // Apply cooldown immediately
        applyCooldown(player);

        boolean isCreative = player.getGameMode() == GameMode.CREATIVE;
        int delayTicks = plugin.getConfigManager().getInt("tools.veinminer.animation-delay-ticks", 2);

        // Progressive breaking animation
        new BukkitRunnable() {
            int index = 0;

            @Override
            public void run() {
                if (index >= vein.size()) {
                    activeVeinminers.remove(player.getUniqueId());
                    this.cancel();
                    return;
                }

                Block block = vein.get(index);

                // Check WorldGuard before breaking
                if (!WorldGuardHook.canBreak(player, block)) {
                    index++;
                    return;
                }

                // Get drops with fortune enchantment
                if (!isCreative) {
                    Collection<ItemStack> drops = block.getDrops(item);
                    for (ItemStack drop : drops) {
                        block.getWorld().dropItemNaturally(block.getLocation(), drop);
                    }

                    // Apply durability
                    applyDurability(item, 1);
                }

                // Play break sound
                Sound breakSound = block.getBlockSoundGroup().getBreakSound();
                block.getWorld().playSound(block.getLocation(), breakSound, 1.0f, 1.0f);

                // Break block
                block.setType(Material.AIR);

                index++;
            }
        }.runTaskTimer(plugin, 0L, delayTicks);
    }

    /**
     * Find all connected ore blocks using BFS
     */
    private List<Block> findVein(Block start, Material oreType, int maxBlocks) {
        List<Block> vein = new ArrayList<>();
        Set<Block> visited = new HashSet<>();
        Queue<Block> queue = new LinkedList<>();

        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty() && vein.size() < maxBlocks) {
            Block current = queue.poll();
            vein.add(current);

            // Check all 26 surrounding blocks (3x3x3 minus center)
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && y == 0 && z == 0)
                            continue;

                        Block neighbor = current.getRelative(x, y, z);

                        if (!visited.contains(neighbor) && neighbor.getType() == oreType) {
                            visited.add(neighbor);
                            queue.add(neighbor);
                        }
                    }
                }
            }
        }

        return vein;
    }

    /**
     * Check if material is an ore
     */
    private boolean isOre(Material material) {
        return material == Material.COAL_ORE ||
                material == Material.DEEPSLATE_COAL_ORE ||
                material == Material.IRON_ORE ||
                material == Material.DEEPSLATE_IRON_ORE ||
                material == Material.COPPER_ORE ||
                material == Material.DEEPSLATE_COPPER_ORE ||
                material == Material.GOLD_ORE ||
                material == Material.DEEPSLATE_GOLD_ORE ||
                material == Material.REDSTONE_ORE ||
                material == Material.DEEPSLATE_REDSTONE_ORE ||
                material == Material.EMERALD_ORE ||
                material == Material.DEEPSLATE_EMERALD_ORE ||
                material == Material.LAPIS_ORE ||
                material == Material.DEEPSLATE_LAPIS_ORE ||
                material == Material.DIAMOND_ORE ||
                material == Material.DEEPSLATE_DIAMOND_ORE ||
                material == Material.NETHER_GOLD_ORE ||
                material == Material.NETHER_QUARTZ_ORE ||
                material == Material.ANCIENT_DEBRIS;
    }

    /**
     * Get active veinminers set for cleanup
     */
    public Set<UUID> getActiveVeinminers() {
        return activeVeinminers;
    }
}
