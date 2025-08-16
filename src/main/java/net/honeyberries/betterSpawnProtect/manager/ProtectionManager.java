package net.honeyberries.betterSpawnProtect.manager;

import net.honeyberries.betterSpawnProtect.configuration.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.util.logging.Level;

/**
 * Manages the spawn protection area, including its properties and location checks.
 * This class is responsible for loading the protection settings from the configuration,
 * determining if a location is within the protected zone, and updating the protection
 * parameters as needed.
 */
public class ProtectionManager {

    private final Plugin plugin;
    private World world;
    private double cx, cy, cz, radius, radiusSq;

    /**
     * Constructs a new {@code ProtectionManager} and initializes its settings from the
     * plugin's configuration.
     *
     * @param plugin The instance of the BetterSpawnProtect plugin.
     */
    public ProtectionManager(Plugin plugin) {
        this.plugin = plugin;
        loadFromConfig();
    }

    /**
     * Loads the protection settings from the {@link ConfigManager}. This includes the
     * protected world, center coordinates, and radius. If the configuration manager
     * is not initialized or the world is not found, appropriate warnings are logged.
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
        this.radiusSq = radius * radius; // Pre-calculate the squared radius for efficiency
    }

    /**
     * Reloads the protection settings from the configuration file. This is typically
     * called when the plugin's reload command is executed.
     */
    public void reloadFromConfig() {
        ConfigManager.getInstance().reloadConfig();
        loadFromConfig();
    }

    /**
     * Checks if a given location is within the protected spawn area. The check is performed
     * in 2D (X and Z coordinates only) and is based on a circular radius.
     *
     * @param loc The location to check.
     * @return {@code true} if the location is within the protected area, {@code false} otherwise.
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
     * Sets the center of the protected area to a new location and saves the updated
     * settings to the configuration.
     *
     * @param newCenter The new center location for the protected area.
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
     * Sets the radius of the protected area and saves the updated value to the configuration.
     *
     * @param r The new radius. Must be a non-negative value.
     */
    public void setRadius(double r) {
        this.radius = Math.max(0, r);
        this.radiusSq = radius * radius;
        ConfigManager.getInstance().setRadius(radius);
    }

    /**
     * Provides a formatted string summarizing the current protection settings, including
     * the world name, center coordinates, and radius.
     *
     * @return A summary string of the protection settings.
     */
    public String getCenterSummary() {
        if (world == null) {
            return "Protected world is not loaded!";
        }
        return String.format("Center: %s (%.2f, %.2f, %.2f), Radius: %.2f",
                world.getName(), cx, cy, cz, radius);
    }

    /**
     * Returns the world where the protection is applied.
     *
     * @return The protected {@link World}.
     */
    public World getWorld() {
        return world;
    }

    /**
     * Returns the radius of the protected area.
     *
     * @return The protection radius.
     */
    public double getRadius() {
        return radius;
    }
}