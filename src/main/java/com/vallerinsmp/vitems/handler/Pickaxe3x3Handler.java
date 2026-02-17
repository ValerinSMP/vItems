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
 * Handler for 3x3 Pickaxe
 * Instantly breaks 9 blocks in a 3x3 area
 */
public class Pickaxe3x3Handler extends ToolHandler {

    public Pickaxe3x3Handler(VItems plugin) {
        super(plugin, ItemType.PICKAXE_3X3);
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
            // Skip if not mineable by pickaxe
            if (!isPickaxeMineable(block.getType())) {
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
     */
    private List<Block> get3x3Area(Player player, Block center) {
        List<Block> blocks = new ArrayList<>();

        // Get player's facing direction
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
                // North/South (Z-axis)
                for (int x = -1; x <= 1; x++) {
                    for (int y = -1; y <= 1; y++) {
                        blocks.add(center.getRelative(x, y, 0));
                    }
                }
            } else if (normalizedYaw >= 45 && normalizedYaw < 135) {
                // East/West (X-axis)
                for (int z = -1; z <= 1; z++) {
                    for (int y = -1; y <= 1; y++) {
                        blocks.add(center.getRelative(0, y, z));
                    }
                }
            } else if (normalizedYaw >= 135 && normalizedYaw < 225) {
                // South/North (Z-axis)
                for (int x = -1; x <= 1; x++) {
                    for (int y = -1; y <= 1; y++) {
                        blocks.add(center.getRelative(x, y, 0));
                    }
                }
            } else {
                // West/East (X-axis)
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
     * Check if material is mineable by pickaxe
     */
    private boolean isPickaxeMineable(Material material) {
        return material.toString().contains("ORE") ||
                material.toString().contains("STONE") ||
                material.toString().contains("DEEPSLATE") ||
                material.toString().contains("NETHERRACK") ||
                material.toString().contains("OBSIDIAN") ||
                material.toString().contains("CONCRETE") ||
                material.toString().contains("TERRACOTTA") ||
                material.toString().contains("BRICKS") ||
                material == Material.COBBLESTONE ||
                material == Material.ANDESITE ||
                material == Material.DIORITE ||
                material == Material.GRANITE ||
                material == Material.END_STONE ||
                material == Material.SANDSTONE ||
                material == Material.RED_SANDSTONE ||
                material == Material.PRISMARINE ||
                material == Material.BASALT ||
                material == Material.BLACKSTONE;
    }
}
