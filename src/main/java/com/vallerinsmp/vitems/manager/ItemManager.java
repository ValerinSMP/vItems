package com.vallerinsmp.vitems.manager;

import com.vallerinsmp.vitems.VItems;
import com.vallerinsmp.vitems.util.ItemType;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

/**
 * Manages custom item creation and identification
 */
public class ItemManager {

    private final VItems plugin;
    private final NamespacedKey itemTypeKey;

    public ItemManager(VItems plugin) {
        this.plugin = plugin;
        this.itemTypeKey = new NamespacedKey(plugin, "item_type");
    }

    /**
     * Create a custom item of specified type
     */
    public ItemStack createItem(ItemType type) {
        Material material = getMaterialForType(type);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        // Set NBT identifier
        meta.getPersistentDataContainer().set(itemTypeKey, PersistentDataType.STRING, type.getNbtIdentifier());

        // Set display name
        String namePath = "items." + type.getConfigKey() + ".name";
        Component name = plugin.getConfigManager().getMessage(namePath);
        meta.displayName(name);

        // Set lore
        String lorePath = "items." + type.getConfigKey() + ".lore";
        List<Component> lore = plugin.getConfigManager().getComponentList(lorePath);
        meta.lore(lore);

        // Add glow effect
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Get the ItemType from an ItemStack
     */
    public ItemType getItemType(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }

        String nbtValue = meta.getPersistentDataContainer().get(itemTypeKey, PersistentDataType.STRING);
        return ItemType.fromNbt(nbtValue);
    }

    /**
     * Check if an ItemStack is a custom vItems tool
     */
    public boolean isCustomItem(ItemStack item) {
        return getItemType(item) != null;
    }

    /**
     * Update an existing item's metadata to match current configuration
     */
    public void updateItem(ItemStack item) {
        ItemType type = getItemType(item);
        if (type == null) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }

        // Update display name
        String namePath = "items." + type.getConfigKey() + ".name";
        Component name = plugin.getConfigManager().getMessage(namePath);
        meta.displayName(name);

        // Update lore
        String lorePath = "items." + type.getConfigKey() + ".lore";
        List<Component> lore = plugin.getConfigManager().getComponentList(lorePath);
        meta.lore(lore);

        item.setItemMeta(meta);
    }

    /**
     * Give a custom item to a player
     */
    public void giveItem(Player player, ItemType type) {
        ItemStack item = createItem(type);
        player.getInventory().addItem(item);
    }

    /**
     * Get the material for a specific item type
     */
    private Material getMaterialForType(ItemType type) {
        return switch (type) {
            case PICKAXE_3X3, VEINMINER -> Material.NETHERITE_PICKAXE;
            case SHOVEL_3X3 -> Material.NETHERITE_SHOVEL;
            case TREE_CAPITATOR -> Material.NETHERITE_AXE;
        };
    }

    /**
     * Get the NamespacedKey used for item identification
     */
    public NamespacedKey getItemTypeKey() {
        return itemTypeKey;
    }
}
