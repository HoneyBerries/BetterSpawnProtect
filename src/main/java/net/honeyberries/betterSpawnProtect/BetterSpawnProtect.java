package net.honeyberries.betterSpawnProtect;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class BetterSpawnProtect extends JavaPlugin {

    private static BetterSpawnProtect instance;
    private ConfigManager configManager;
    private ProtectionManager protectionManager;
    private MessageGate messageGate;

    public static BetterSpawnProtect getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        // Init config manager (creates config.yml if missing)
        this.configManager = new ConfigManager(this);
        this.configManager.load();

        this.protectionManager = new ProtectionManager(configManager);
        this.messageGate = new MessageGate(2000L); // 2s cooldown per player

        // Register listeners
        Bukkit.getPluginManager().registerEvents(new ProtectionListener(protectionManager, messageGate), this);

        // Register command
        BSPCommand command = new BSPCommand(protectionManager, configManager);
        PluginCommand pc = getCommand("betterspawnprotect");
        if (pc != null) {
            pc.setExecutor(command);
            pc.setTabCompleter(command);
        } else {
            getLogger().severe("Command 'betterspawnprotect' not defined in plugin.yml");
        }

        getLogger().info("BetterSpawnProtect enabled. Center: " + protectionManager.getCenterSummary());
    }

    @Override
    public void onDisable() {
        // Persist config (if modified)
        configManager.save();
    }

    public void reloadAll() {
        configManager.load();
        protectionManager.reloadFromConfig();
        getLogger().info("BetterSpawnProtect reloaded. " + protectionManager.getCenterSummary());
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}