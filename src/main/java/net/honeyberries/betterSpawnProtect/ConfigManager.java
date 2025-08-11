package net.honeyberries.betterSpawnProtect;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;

public class ConfigManager {

    private static ConfigManager INSTANCE;

    public static ConfigManager getInstance() {
        return INSTANCE;
    }

    private final BetterSpawnProtect plugin;
    private File configFile;
    private YamlConfiguration config;

    public ConfigManager(BetterSpawnProtect plugin) {
        this.plugin = plugin;
        INSTANCE = this;
    }

    public void load() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "config.yml");
        }
        if (!configFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try (InputStream in = plugin.getResource("config.yml")) {
                if (in != null) {
                    copy(in, configFile);
                } else {
                    // Create default
                    plugin.getLogger().warning("Bundled config.yml not found; creating default.");
                    writeDefault();
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create default config: " + e.getMessage());
            }
        }
        config = new YamlConfiguration();
        try {
            config.load(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().severe("Could not load config.yml: " + e.getMessage());
        }
        // Ensure defaults exist
        boolean saveNeeded = false;
        if (!config.contains("spawn.world")) {
            World defaultWorld = Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0);
            config.set("spawn.world", defaultWorld != null ? defaultWorld.getName() : "world");
            saveNeeded = true;
        }
        if (!config.contains("spawn.x")) { config.set("spawn.x", 0.5); saveNeeded = true; }
        if (!config.contains("spawn.y")) { config.set("spawn.y", 64.0); saveNeeded = true; }
        if (!config.contains("spawn.z")) { config.set("spawn.z", 0.5); saveNeeded = true; }
        if (!config.contains("spawn.radius")) { config.set("spawn.radius", 32.0); saveNeeded = true; }
        if (!config.contains("messages.deny")) {
            config.set("messages.deny", "&cYou cannot modify blocks in the protected spawn.");
            saveNeeded = true;
        }
        if (saveNeeded) save();
    }

    private void writeDefault() throws IOException {
        config = new YamlConfiguration();
        config.set("spawn.world", "world");
        config.set("spawn.x", 0.5);
        config.set("spawn.y", 64.0);
        config.set("spawn.z", 0.5);
        config.set("spawn.radius", 32.0);
        config.set("messages.deny", "&cYou cannot modify blocks in the protected spawn.");
        save();
    }

    private void copy(InputStream in, File out) throws IOException {
        try (OutputStream o = new FileOutputStream(out)) {
            byte[] buf = new byte[8192];
            int r;
            while ((r = in.read(buf)) != -1) {
                o.write(buf, 0, r);
            }
        }
    }

    public void save() {
        if (config == null || configFile == null) return;
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save config.yml: " + e.getMessage());
        }
    }

    public YamlConfiguration getConfig() {
        return config;
    }

    public void setSpawn(String world, double x, double y, double z) {
        config.set("spawn.world", world);
        config.set("spawn.x", x);
        config.set("spawn.y", y);
        config.set("spawn.z", z);
        save();
    }

    public void setRadius(double radius) {
        config.set("spawn.radius", radius);
        save();
    }

    public String getDenyMessage() {
        return config.getString("messages.deny");
    }
}