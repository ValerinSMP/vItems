package com.vallerinsmp.vitems.handler;

import com.vallerinsmp.vitems.VItems;
import com.vallerinsmp.vitems.util.ItemType;
import com.vallerinsmp.vitems.util.WorldGuardHook;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Handler for 3x3 Shovel
 * Instantly breaks 9 blocks in a 3x3 area
 */
public class Shovel3x3Handler extends ToolHandler {

    public Shovel3x3Handler(VItems plugin) {
        super(plugin, ItemType.SHOVEL_3X3);
    }

    @Override
    public void handleBlockBreak(Player player, Block centerBlock, ItemStack item,
            org.bukkit.block.BlockFace clickedFace) {
        if (!canUse(player, centerBlock)) {
            return;
        }

        // Get blocks to break in 3x3 area based on clicked face
        List<Block> blocksToBreak = get3x3Area(centerBlock, clickedFace);

        int blocksBroken = 0;
        boolean isCreative = player.getGameMode() == GameMode.CREATIVE;

        for (Block block : blocksToBreak) {
            // Skip if not diggable by shovel
            if (!isShovelDiggable(block.getType())) {
                continue;
            }

            // Check WorldGuard for each block
            if (!WorldGuardHook.canBreak(player, block)) {
                continue;
            }

            // Drop items with enchantments
            if (!isCreative) {
                Collection<ItemStack> drops = block.getDrops(item);
                for (ItemStack drop : drops) {
                    block.getWorld().dropItemNaturally(block.getLocation(), drop);
                }
            }

            // Break block
            block.setType(Material.AIR);
            blocksBroken++;
        }

        // Apply durability (1 per block broken)
        if (!isCreative && blocksBroken > 0) {
            applyDurability(item, blocksBroken);
        }
    }

    /**
     * Get 3x3 area of blocks based on the face that was clicked
     * The 3x3 area is perpendicular to the clicked face
     */
    private List<Block> get3x3Area(Block center, org.bukkit.block.BlockFace clickedFace) {
        List<Block> blocks = new ArrayList<>();

        // If face is null, default to a vertical 3x3 (north/south oriented)
        if (clickedFace == null) {
            clickedFace = org.bukkit.block.BlockFace.NORTH;
        }

        // Determine the 3x3 plane based on which face was clicked
        switch (clickedFace) {
            case UP, DOWN -> {
                // Clicked top or bottom -> dig horizontal 3x3
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        blocks.add(center.getRelative(x, 0, z));
                    }
                }
            }
            case NORTH, SOUTH -> {
                // Clicked north/south face -> dig vertical 3x3 on X-Y plane
                for (int x = -1; x <= 1; x++) {
                    for (int y = -1; y <= 1; y++) {
                        blocks.add(center.getRelative(x, y, 0));
                    }
                }
            }
            case EAST, WEST -> {
                // Clicked east/west face -> dig vertical 3x3 on Z-Y plane
                for (int z = -1; z <= 1; z++) {
                    for (int y = -1; y <= 1; y++) {
                        blocks.add(center.getRelative(0, y, z));
                    }
                }
            }
            default -> {
                // For diagonal faces, default to horizontal
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        blocks.add(center.getRelative(x, 0, z));
                    }
                }
            }
        }

        return blocks;
    }

    /**
     * Check if material is diggable by shovel
     */
    private boolean isShovelDiggable(Material material) {
        return material == Material.DIRT ||
                material == Material.GRASS_BLOCK ||
                material == Material.SAND ||
                material == Material.RED_SAND ||
                material == Material.GRAVEL ||
                material == Material.CLAY ||
                material == Material.SNOW ||
                material == Material.SNOW_BLOCK ||
                material == Material.SOUL_SAND ||
                material == Material.SOUL_SOIL ||
                material == Material.MYCELIUM ||
                material == Material.PODZOL ||
                material == Material.COARSE_DIRT ||
                material == Material.ROOTED_DIRT ||
                material == Material.MUD ||
                material == Material.MUDDY_MANGROVE_ROOTS;
    }
}
