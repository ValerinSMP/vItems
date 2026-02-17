package com.vallerinsmp.vitems.manager;

import com.vallerinsmp.vitems.VItems;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages plugin configuration and messages
 */
public class ConfigManager {

    private final VItems plugin;
    private FileConfiguration config;
    private FileConfiguration messages;
    private final MiniMessage miniMessage;
    private final Map<String, String> messageCache;

    public ConfigManager(VItems plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        this.messageCache = new HashMap<>();
        reload();
    }

    /**
     * Reload all configurations
     */
    public void reload() {
        // Save default configs if they don't exist
        plugin.saveDefaultConfig();
        saveMessagesConfig();

        // Reload config
        plugin.reloadConfig();
        config = plugin.getConfig();

        // Reload messages
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        messages = YamlConfiguration.loadConfiguration(messagesFile);

        // Clear message cache
        messageCache.clear();
    }

    private void saveMessagesConfig() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
    }

    /**
     * Get configuration value
     */
    public FileConfiguration getConfig() {
        return config;
    }

    /**
     * Get string from config with default
     */
    public String getString(String path, String def) {
        return config.getString(path, def);
    }

    /**
     * Get int from config with default
     */
    public int getInt(String path, int def) {
        return config.getInt(path, def);
    }

    /**
     * Get double from config with default
     */
    public double getDouble(String path, double def) {
        return config.getDouble(path, def);
    }

    /**
     * Get boolean from config with default
     */
    public boolean getBoolean(String path, boolean def) {
        return config.getBoolean(path, def);
    }

    /**
     * Get raw message string from messages.yml
     */
    public String getRawMessage(String path) {
        return messages.getString(path, path);
    }

    /**
     * Get formatted message as Component with placeholder replacement
     */
    public Component getMessage(String path, Map<String, String> placeholders) {
        String message = messageCache.computeIfAbsent(path, this::getRawMessage);

        // Replace prefix
        String prefix = getRawMessage("general.prefix");
        message = message.replace("%prefix%", prefix);

        // Replace custom placeholders
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                message = message.replace(entry.getKey(), entry.getValue());
            }
        }

        return miniMessage.deserialize(message);
    }

    /**
     * Get formatted message as Component without placeholders
     */
    public Component getMessage(String path) {
        return getMessage(path, null);
    }

    /**
     * Get list of strings from messages
     */
    public List<String> getMessageList(String path) {
        return messages.getStringList(path);
    }

    /**
     * Get formatted list of components
     */
    public List<Component> getComponentList(String path) {
        return getMessageList(path).stream()
                .map(miniMessage::deserialize)
                .toList();
    }

    /**
     * Parse MiniMessage string to Component
     */
    public Component parse(String text) {
        return miniMessage.deserialize(text);
    }
}
