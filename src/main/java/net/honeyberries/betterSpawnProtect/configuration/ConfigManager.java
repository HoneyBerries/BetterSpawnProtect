package net.honeyberries.betterSpawnProtect.configuration;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.logging.Level;

/**
 * Manages the plugin's configuration file (config.yml).
 * Handles loading, saving, and providing access to configuration values.
 */
public class ConfigManager {

    private static ConfigManager instance;
    private final Plugin plugin;
    private final File configFile;
    private YamlConfiguration config;

    /**
     * Private constructor to enforce the singleton pattern.
     *
     * @param plugin The plugin instance.
     */
    private ConfigManager(Plugin plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
        loadConfig();
    }

    /**
     * Gets the singleton instance of the ConfigManager.
     *
     * @param plugin The plugin instance.
     * @return The singleton ConfigManager instance.
     */
    public static ConfigManager getInstance(Plugin plugin) {
        if (instance == null) {
            instance = new ConfigManager(plugin);
        }
        return instance;
    }

    /**
     * Gets the singleton instance, assuming it has been initialized.
     *
     * @return The ConfigManager instance, or null if not yet initialized.
     */
    public static ConfigManager getInstance() {
        return instance;
    }

    /**
     * Loads the configuration from the file, creating a default if it doesn't exist.
     */
    private void loadConfig() {
        if (!configFile.exists()) {
            plugin.getDataFolder().mkdirs();
            createDefaultConfig();
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    /**
     * Creates a default configuration file with predefined values and comments.
     */
    private void createDefaultConfig() {
        config = new YamlConfiguration();
        config.options().header("BetterSpawnProtect Configuration");

        config.set("protection.world", "world");
        config.set("protection.center.x", 0.5);
        config.set("protection.center.y", 64.0);
        config.set("protection.center.z", 0.5);
        config.set("protection.radius", 32.0);

        config.setComments("protection.world", Collections.singletonList("The world where spawn protection is active."));
        config.setComments("protection.center", Collections.singletonList("The center coordinates of the protected area."));
        config.setComments("protection.radius", Collections.singletonList("The radius of the protected area in blocks."));

        saveConfig();
    }

    /**
     * Saves the current configuration to the config.yml file.
     */
    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config.yml", e);
        }
    }

    /**
     * Reloads the configuration from the config.yml file.
     */
    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    /**
     * Gets the name of the protected world from the configuration.
     *
     * @return The name of the world.
     */
    public String getProtectionWorldName() {
        return config.getString("protection.world", "world");
    }

    /**
     * Gets the protected world from the server.
     *
     * @return The World object, or null if not found.
     */
    public World getProtectionWorld() {
        return Bukkit.getWorld(getProtectionWorldName());
    }

    /**
     * Gets the X coordinate of the protection center.
     *
     * @return The X coordinate.
     */
    public double getCenterX() {
        return config.getDouble("protection.center.x", 0.5);
    }

    /**
     * Gets the Y coordinate of the protection center.
     *
     * @return The Y coordinate.
     */
    public double getCenterY() {
        return config.getDouble("protection.center.y", 64.0);
    }

    /**
     * Gets the Z coordinate of the protection center.
     *
     * @return The Z coordinate.
     */
    public double getCenterZ() {
        return config.getDouble("protection.center.z", 0.5);
    }

    /**
     * Gets the radius of the protected area.
     *
     * @return The radius.
     */
    public double getRadius() {
        return config.getDouble("protection.radius", 32.0);
    }

    /**
     * Sets the protected world in the configuration.
     *
     * @param world The world to set as protected.
     */
    public void setProtectionWorld(World world) {
        config.set("protection.world", world.getName());
        saveConfig();
    }

    /**
     * Sets the center of the protected area in the configuration.
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
     * Sets the radius of the protected area in the configuration.
     *
     * @param radius The radius to set.
     */
    public void setRadius(double radius) {
        config.set("protection.radius", radius);
        saveConfig();
    }
}