package net.honeyberries.betterSpawnProtect.manager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Manages spawn protection logic, including defining a protected area
 * and checking if a location falls within the protected zone.
 */
public class ProtectionManager {

    // The world where the protection is applied
    private World world;

    // Coordinates of the center of the protected area
    private double cx;
    private double cy;
    private double cz;

    // Radius of the protected area
    private double radius;

    // Square of the radius for distance calculations
    private double radiusSq;

    /**
     * Constructs a ProtectionManager with default values.
     * Defaults to the first loaded world, center at (0.5, 64.0, 0.5),
     * and a radius of 32.0.
     */
    public ProtectionManager() {
        this.world = Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().getFirst();
        this.cx = 0.5;
        this.cy = 64.0;
        this.cz = 0.5;
        this.radius = 32.0;
        this.radiusSq = radius * radius;
    }

    /**
     * Reloads the protection configuration.
     * Currently obsolete but retained for compatibility with the reload command.
     * Future implementations may load data from persistent storage.
     */
    public void reloadFromConfig() {
        // This method is now obsolete but is kept for compatibility with the reload command.
    }

    /**
     * Checks if a given location is within the protected area.
     *
     * @param loc The location to check.
     * @return true if the location is protected, false otherwise.
     */
    public boolean isProtected(Location loc) {
        if (world == null || loc.getWorld() == null) return false;
        if (!loc.getWorld().equals(world)) return false;
        double dx = loc.getX() - cx;
        double dz = loc.getZ() - cz;
        return (dx * dx + dz * dz) <= radiusSq;
    }

    /**
     * Sets the center of the protected area.
     *
     * @param newCenter The new center location.
     */
    public void setCenter(Location newCenter) {
        this.world = newCenter.getWorld();
        this.cx = newCenter.getX();
        this.cy = newCenter.getY();
        this.cz = newCenter.getZ();
    }

    /**
     * Sets the radius of the protected area.
     *
     * @param r The new radius. Must be non-negative.
     */
    public void setRadius(double r) {
        this.radius = Math.max(0, r);
        this.radiusSq = radius * radius;
    }

    /**
     * Provides a summary of the center and radius of the protected area.
     *
     * @return A string summarizing the center and radius.
     */
    public String getCenterSummary() {
        if (world == null) {
            this.world = Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().getFirst();
            if (world == null) return "World not loaded yet.";
        }
        return String.format("Center: %s (%.2f, %.2f, %.2f) Radius: %.2f",
                world.getName(), cx, cy, cz, radius);
    }

    /**
     * Gets the world where the protection is applied.
     *
     * @return The world object.
     */
    public World getWorld() {
        return world;
    }

    /**
     * Gets the radius of the protected area.
     *
     * @return The radius.
     */
    public double getRadius() {
        return radius;
    }
}