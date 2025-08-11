package net.honeyberries.betterSpawnProtect.manager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class ProtectionManager {

    private World world;
    private double cx;
    private double cy;
    private double cz;
    private double radius;
    private double radiusSq;

    public ProtectionManager() {
        // Default values
        this.world = Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0);
        this.cx = 0.5;
        this.cy = 64.0;
        this.cz = 0.5;
        this.radius = 32.0;
        this.radiusSq = radius * radius;
    }

    public void reloadFromConfig() {
        // This method is now obsolete but is kept for compatibility with the reload command.
        // In the future, this could load from a database or other persistent storage.
    }

    public boolean isProtected(Location loc) {
        if (world == null || loc.getWorld() == null) return false;
        if (!loc.getWorld().equals(world)) return false;
        // Horizontal distance check (circle)
        double dx = loc.getX() - cx;
        double dz = loc.getZ() - cz;
        return (dx * dx + dz * dz) <= radiusSq;
    }

    public void setCenter(Location newCenter) {
        this.world = newCenter.getWorld();
        this.cx = newCenter.getX();
        this.cy = newCenter.getY();
        this.cz = newCenter.getZ();
    }

    public void setRadius(double r) {
        this.radius = Math.max(0, r);
        this.radiusSq = radius * radius;
    }

    public String getCenterSummary() {
        if (world == null) {
            // Attempt to get the default world if it's still null
            this.world = Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0);
            if (world == null) return "World not loaded yet.";
        }
        return String.format("Center: %s (%.2f, %.2f, %.2f) Radius: %.2f",
                world.getName(), cx, cy, cz, radius);
    }

    public World getWorld() {
        return world;
    }

    public double getRadius() {
        return radius;
    }
}