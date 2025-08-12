package net.honeyberries.betterSpawnProtect.configuration;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

/**
 * Manages the plugin configuration using YAML.
 * Handles loading and saving of spawn protection settings.
 */
public class ConfigManager {

    // Singleton instance
    private static ConfigManager instance;

    // Reference to the plugin
    private final Plugin plugin;

    // Configuration file and YAML configuration
    private final File configFile;
    private YamlConfiguration config;

    /**
     * Private constructor for singleton pattern.
     *
     * @param plugin The plugin instance.
     */
    private ConfigManager(Plugin plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
        loadConfig();
    }

    /**
     * Gets the singleton instance of ConfigManager.
     *
     * @param plugin The plugin instance.
     * @return The ConfigManager instance.
     */
    public static ConfigManager getInstance(Plugin plugin) {
        if (instance == null) {
            instance = new ConfigManager(plugin);
        }
        return instance;
    }

    /**
     * Gets the singleton instance (only if already initialized).
     *
     * @return The ConfigManager instance or null if not initialized.
     */
    public static ConfigManager getInstance() {
        return instance;
    }

    /**
     * Loads the configuration from file or creates default values.
     */
    private void loadConfig() {
        if (!configFile.exists()) {
            plugin.getDataFolder().mkdirs();
            createDefaultConfig();
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    /**
     * Creates a default configuration file with default values.
     */
    private void createDefaultConfig() {
        config = new YamlConfiguration();

        // Set default values
        config.set("protection.world", "world");
        config.set("protection.center.x", 0.5);
        config.set("protection.center.y", 64.0);
        config.set("protection.center.z", 0.5);
        config.set("protection.radius", 32.0);

        saveConfig();
    }

    /**
     * Saves the current configuration to file.
     */
    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save config.yml: " + e.getMessage());
        }
    }

    /**
     * Reloads the configuration from file.
     */
    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    /**
     * Gets the protection world name from config.
     *
     * @return The world name.
     */
    public String getProtectionWorldName() {
        return config.getString("protection.world", "world");
    }

    /**
     * Gets the protection world.
     *
     * @return The world object or null if not found.
     */
    public World getProtectionWorld() {
        String worldName = getProtectionWorldName();
        return Bukkit.getWorld(worldName);
    }

    /**
     * Gets the protection center X coordinate.
     *
     * @return The X coordinate.
     */
    public double getCenterX() {
        return config.getDouble("protection.center.x", 0.5);
    }

    /**
     * Gets the protection center Y coordinate.
     *
     * @return The Y coordinate.
     */
    public double getCenterY() {
        return config.getDouble("protection.center.y", 64.0);
    }

    /**
     * Gets the protection center Z coordinate.
     *
     * @return The Z coordinate.
     */
    public double getCenterZ() {
        return config.getDouble("protection.center.z", 0.5);
    }

    /**
     * Gets the protection radius.
     *
     * @return The radius.
     */
    public double getRadius() {
        return config.getDouble("protection.radius", 32.0);
    }

    /**
     * Sets the protection world.
     *
     * @param world The world to set.
     */
    public void setProtectionWorld(World world) {
        config.set("protection.world", world.getName());
        saveConfig();
    }

    /**
     * Sets the protection center coordinates.
     *
     * @param x The X coordinate.
     * @param y The Y coordinate.
     * @param z The Z coordinate.
     */
    public void setCenter(double x, double y, double z) {
        config.set("protection.center.x", x);
        config.set("protection.center.y", y);
        config.set("protection.center.z", z);
        saveConfig();
    }

    /**
     * Sets the protection radius.
     *
     * @param radius The radius to set.
     */
    public void setRadius(double radius) {
        config.set("protection.radius", radius);
        saveConfig();
    }
}