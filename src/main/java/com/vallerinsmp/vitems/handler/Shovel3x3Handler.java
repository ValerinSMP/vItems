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
    public void handleBlockBreak(Player player, Block centerBlock, ItemStack item) {
        if (!canUse(player, centerBlock)) {
            return;
        }

        // Get blocks to break in 3x3 area
        List<Block> blocksToBreak = get3x3Area(player, centerBlock);

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
     * Get 3x3 area of blocks based on player's facing direction
     * Same as pickaxe
     */
    private List<Block> get3x3Area(Player player, Block center) {
        List<Block> blocks = new ArrayList<>();

        float yaw = player.getLocation().getYaw();
        float pitch = player.getLocation().getPitch();

        // Determine if looking up/down (vertical breaking)
        if (Math.abs(pitch) > 60) {
            // Horizontal 3x3 (looking up or down)
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    blocks.add(center.getRelative(x, 0, z));
                }
            }
        } else {
            // Vertical 3x3 based on facing direction
            float normalizedYaw = (yaw + 360) % 360;

            if (normalizedYaw >= 315 || normalizedYaw < 45) {
                for (int x = -1; x <= 1; x++) {
                    for (int y = -1; y <= 1; y++) {
                        blocks.add(center.getRelative(x, y, 0));
                    }
                }
            } else if (normalizedYaw >= 45 && normalizedYaw < 135) {
                for (int z = -1; z <= 1; z++) {
                    for (int y = -1; y <= 1; y++) {
                        blocks.add(center.getRelative(0, y, z));
                    }
                }
            } else if (normalizedYaw >= 135 && normalizedYaw < 225) {
                for (int x = -1; x <= 1; x++) {
                    for (int y = -1; y <= 1; y++) {
                        blocks.add(center.getRelative(x, y, 0));
                    }
                }
            } else {
                for (int z = -1; z <= 1; z++) {
                    for (int y = -1; y <= 1; y++) {
                        blocks.add(center.getRelative(0, y, z));
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
