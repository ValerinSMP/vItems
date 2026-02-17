package com.vallerinsmp.vitems.util;

/**
 * Enum representing all custom item types in vItems
 */
public enum ItemType {
    PICKAXE_3X3("3x3pickaxe", "pickaxe_3x3"),
    SHOVEL_3X3("3x3shovel", "shovel_3x3"),
    VEINMINER("veinminer", "veinminer"),
    TREE_CAPITATOR("treecapitator", "tree_capitator");

    private final String nbtKey;
    private final String configKey;

    ItemType(String nbtKey, String configKey) {
        this.nbtKey = nbtKey;
        this.configKey = configKey;
    }

    /**
     * Get the NBT identifier for this item type (e.g., "vitems:3x3pickaxe")
     */
    public String getNbtIdentifier() {
        return "vitems:" + nbtKey;
    }

    /**
     * Get the config key for this item type (e.g., "pickaxe_3x3")
     */
    public String getConfigKey() {
        return configKey;
    }

    /**
     * Get ItemType from NBT identifier
     */
    public static ItemType fromNbt(String nbtValue) {
        if (nbtValue == null || !nbtValue.startsWith("vitems:")) {
            return null;
        }
        
        String key = nbtValue.substring(7); // Remove "vitems:" prefix
        for (ItemType type : values()) {
            if (type.nbtKey.equals(key)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Get ItemType from config key
     */
    public static ItemType fromConfigKey(String configKey) {
        for (ItemType type : values()) {
            if (type.configKey.equalsIgnoreCase(configKey)) {
                return type;
            }
        }
        return null;
    }
}
