package net.honeyberries.betterSpawnProtect.manager;

import net.honeyberries.betterSpawnProtect.configuration.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.util.logging.Level;

/**
 * Manages the spawn protection area, handling its properties and checking locations against it.
 */
public class ProtectionManager {

    private final Plugin plugin;
    private World world;
    private double cx, cy, cz, radius, radiusSq;

    /**
     * Constructs a ProtectionManager and loads initial values from the configuration.
     *
     * @param plugin The BetterSpawnProtect plugin instance.
     */
    public ProtectionManager(Plugin plugin) {
        this.plugin = plugin;
        loadFromConfig();
    }

    /**
     * Loads protection settings from the configuration.
     */
    private void loadFromConfig() {
        ConfigManager configManager = ConfigManager.getInstance();
        if (configManager == null) {
            plugin.getLogger().log(Level.SEVERE, "Configuration manager not initialized!");
            return;
        }

        String worldName = configManager.getProtectionWorldName();
        this.world = Bukkit.getWorld(worldName);
        if (this.world == null) {
            plugin.getLogger().log(Level.WARNING, "Protected world '" + worldName + "' not found or not loaded!");
        }

        this.cx = configManager.getCenterX();
        this.cy = configManager.getCenterY();
        this.cz = configManager.getCenterZ();
        this.radius = configManager.getRadius();
        this.radiusSq = radius * radius;
    }

    /**
     * Reloads the protection configuration from the config file.
     */
    public void reloadFromConfig() {
        ConfigManager.getInstance().reloadConfig();
        loadFromConfig();
    }

    /**
     * Checks if a given location is within the protected area.
     *
     * @param loc The location to check.
     * @return {@code true} if the location is protected, {@code false} otherwise.
     */
    public boolean isProtected(Location loc) {
        if (world == null || loc.getWorld() == null || !loc.getWorld().equals(world)) {
            return false;
        }
        double dx = loc.getX() - cx;
        double dz = loc.getZ() - cz;
        return (dx * dx + dz * dz) <= radiusSq;
    }

    /**
     * Sets the center of the protected area and saves it to the configuration.
     *
     * @param newCenter The new center location.
     */
    public void setCenter(Location newCenter) {
        this.world = newCenter.getWorld();
        this.cx = newCenter.getX();
        this.cy = newCenter.getY();
        this.cz = newCenter.getZ();

        ConfigManager configManager = ConfigManager.getInstance();
        configManager.setProtectionWorld(world);
        configManager.setCenter(cx, cy, cz);
    }

    /**
     * Sets the radius of the protected area and saves it to the configuration.
     *
     * @param r The new radius. Must be a non-negative value.
     */
    public void setRadius(double r) {
        this.radius = Math.max(0, r);
        this.radiusSq = radius * radius;
        ConfigManager.getInstance().setRadius(radius);
    }

    /**
     * Provides a summary of the current protection settings.
     *
     * @return A string summarizing the center and radius.
     */
    public String getCenterSummary() {
        if (world == null) {
            return "Protected world is not loaded!";
        }
        return String.format("Center: %s (%.2f, %.2f, %.2f), Radius: %.2f",
                world.getName(), cx, cy, cz, radius);
    }

    /**
     * Gets the world where the protection is applied.
     *
     * @return The protected world.
     */
    public World getWorld() {
        return world;
    }

    /**
     * Gets the radius of the protected area.
     *
     * @return The protection radius.
     */
    public double getRadius() {
        return radius;
    }
}