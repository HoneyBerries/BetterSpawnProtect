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
 * Manages the plugin's configuration file, {@code config.yml}. This class follows the
 * singleton pattern to ensure that there is only one instance of the configuration manager
 * throughout the plugin's lifecycle. It handles loading, saving, and providing access to
 * all configuration values, as well as creating a default configuration if one does not exist.
 */
public class ConfigManager {

    private static ConfigManager instance;
    private final Plugin plugin;
    private final File configFile;
    private YamlConfiguration config;

    /**
     * Private constructor to enforce the singleton pattern. Initializes the configuration
     * file and loads the initial values.
     *
     * @param plugin The instance of the plugin.
     */
    private ConfigManager(Plugin plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
        loadConfig();
    }

    /**
     * Returns the singleton instance of the {@code ConfigManager}. If the instance does not
     * yet exist, it is created.
     *
     * @param plugin The instance of the plugin.
     * @return The singleton {@code ConfigManager} instance.
     */
    public static ConfigManager getInstance(Plugin plugin) {
        if (instance == null) {
            instance = new ConfigManager(plugin);
        }
        return instance;
    }

    /**
     * Returns the singleton instance of the {@code ConfigManager}, assuming it has already
     * been initialized.
     *
     * @return The {@code ConfigManager} instance, or {@code null} if it has not been initialized.
     */
    public static ConfigManager getInstance() {
        return instance;
    }

    /**
     * Loads the configuration from {@code config.yml}. If the file does not exist, it
     * creates a default configuration file.
     */
    private void loadConfig() {
        if (!configFile.exists()) {
            plugin.getDataFolder().mkdirs();
            createDefaultConfig();
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    /**
     * Creates a default configuration file with predefined values and descriptive comments.
     * This method is called when the {@code config.yml} file is not found.
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
     * Saves the current configuration to the {@code config.yml} file. If an error occurs
     * during saving, it is logged to the console.
     */
    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config.yml", e);
        }
    }

    /**
     * Reloads the configuration from the {@code config.yml} file. This is useful for
     * applying changes without restarting the server.
     */
    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    /**
     * Retrieves the name of the protected world from the configuration.
     *
     * @return The name of the protected world as a {@link String}.
     */
    public String getProtectionWorldName() {
        return config.getString("protection.world", "world");
    }

    /**
     * Retrieves the {@link World} object corresponding to the protected world name.
     *
     * @return The {@link World} object, or {@code null} if the world is not found.
     */
    public World getProtectionWorld() {
        return Bukkit.getWorld(getProtectionWorldName());
    }

    /**
     * Retrieves the X coordinate of the protection center.
     *
     * @return The X coordinate as a {@code double}.
     */
    public double getCenterX() {
        return config.getDouble("protection.center.x", 0.5);
    }

    /**
     * Retrieves the Y coordinate of the protection center.
     *
     * @return The Y coordinate as a {@code double}.
     */
    public double getCenterY() {
        return config.getDouble("protection.center.y", 64.0);
    }

    /**
     * Retrieves the Z coordinate of the protection center.
     *
     * @return The Z coordinate as a {@code double}.
     */
    public double getCenterZ() {
        return config.getDouble("protection.center.z", 0.5);
    }

    /**
     * Retrieves the radius of the protected area.
     *
     * @return The radius as a {@code double}.
     */
    public double getRadius() {
        return config.getDouble("protection.radius", 32.0);
    }

    /**
     * Sets the protected world in the configuration and saves the changes.
     *
     * @param world The {@link World} to set as protected.
     */
    public void setProtectionWorld(World world) {
        config.set("protection.world", world.getName());
        saveConfig();
    }

    /**
     * Sets the center of the protected area in the configuration and saves the changes.
     *
     * @param x The X coordinate of the center.
     * @param y The Y coordinate of the center.
     * @param z The Z coordinate of the center.
     */
    public void setCenter(double x, double y, double z) {
        config.set("protection.center.x", x);
        config.set("protection.center.y", y);
        config.set("protection.center.z", z);
        saveConfig();
    }

    /**
     * Sets the radius of the protected area in the configuration and saves the changes.
     *
     * @param radius The radius to set.
     */
    public void setRadius(double radius) {
        config.set("protection.radius", radius);
        saveConfig();
    }
}