package net.honeyberries.betterSpawnProtect;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

public class ProtectionManager {

    private final ConfigManager configManager;

    private World world;
    private double cx;
    private double cy;
    private double cz;
    private double radius;
    private double radiusSq;

    public ProtectionManager(ConfigManager configManager) {
        this.configManager = configManager;
        reloadFromConfig();
    }

    public void reloadFromConfig() {
        YamlConfiguration c = configManager.getConfig();
        this.world = Bukkit.getWorld(c.getString("spawn.world", "world"));
        this.cx = c.getDouble("spawn.x", 0.5);
        this.cy = c.getDouble("spawn.y", 64.0);
        this.cz = c.getDouble("spawn.z", 0.5);
        this.radius = Math.max(0, c.getDouble("spawn.radius", 32.0));
        this.radiusSq = radius * radius;
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
        configManager.setSpawn(world.getName(), cx, cy, cz);
        recalc();
    }

    public void setRadius(double r) {
        this.radius = Math.max(0, r);
        this.radiusSq = radius * radius;
        configManager.setRadius(this.radius);
    }

    private void recalc() {
        this.radius = configManager.getConfig().getDouble("spawn.radius", radius);
        this.radiusSq = radius * radius;
    }

    public String getCenterSummary() {
        if (world == null) return "World not loaded yet.";
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