package net.honeyberries.betterSpawnProtect;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.honeyberries.betterSpawnProtect.command.CommandManager;
import net.honeyberries.betterSpawnProtect.manager.ConfigManager;
import net.honeyberries.betterSpawnProtect.manager.MessageGate;
import net.honeyberries.betterSpawnProtect.manager.ProtectionListener;
import net.honeyberries.betterSpawnProtect.manager.ProtectionManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * Main class for the BetterSpawnProtect plugin.
 * This class handles the plugin lifecycle and manages core components such as
 * the ProtectionManager, MessageGate, and command registration.
 */
public class BetterSpawnProtect extends JavaPlugin {

    // Singleton instance of the plugin
    private static BetterSpawnProtect instance;

    // Manages spawn protection logic
    private ProtectionManager protectionManager;

    // Handles message cooldowns for players
    private MessageGate messageGate;

    // Manages configuration
    private ConfigManager configManager;

    /**
     * Gets the singleton instance of the BetterSpawnProtect plugin.
     *
     * @return the instance of the plugin
     */
    public static BetterSpawnProtect getInstance() {
        return instance;
    }

    /**
     * Called when the plugin is loaded. Initializes the singleton instance.
     */
    @Override
    public void onLoad() {
        instance = this;
    }

    /**
     * Called when the plugin is enabled. Initializes core components, registers
     * event listeners, and sets up commands.
     */
    @Override
    public void onEnable() {
        // Initialize the ConfigManager first
        this.configManager = ConfigManager.getInstance(this);

        // Initialize the ProtectionManager (now loads from config)
        this.protectionManager = new ProtectionManager();

        // Initialize the MessageGate with a 2-second cooldown per player
        this.messageGate = new MessageGate(2000L);

        // Register event listeners for protection logic
        Bukkit.getPluginManager().registerEvents(new ProtectionListener(protectionManager, messageGate), this);

        // Register commands using the CommandManager with proper lifecycle handling
        CommandManager commandManager = new CommandManager(this, protectionManager);

        // Use a more robust approach for command registration
        try {
            getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
                final var commands = event.registrar();
                commands.register(
                    commandManager.getBuildCommand(),
                    "Main command for BetterSpawnProtect",
                    List.of("bsp")
                );
            });
        } catch (Exception e) {
            getLogger().warning("Failed to register commands via lifecycle manager: " + e.getMessage());
            getLogger().info("Commands may need to be registered manually or the server may not support Paper's command lifecycle.");
        }

        // Log a message indicating the plugin has been enabled
        getLogger().info("BetterSpawnProtect enabled. " + protectionManager.getCenterSummary());
    }

    /**
     * Called when the plugin is disabled. Saves any pending configuration changes.
     */
    @Override
    public void onDisable() {
        // Ensure config is saved on disable
        if (configManager != null) {
            configManager.saveConfig();
        }
        getLogger().info("BetterSpawnProtect disabled.");
    }

    /**
     * Reloads the plugin configuration and updates the ProtectionManager.
     * Logs a message indicating the reload status.
     */
    public void reloadAll() {
        if (configManager != null) {
            configManager.reloadConfig();
        }
        protectionManager.reloadFromConfig();
        getLogger().info("BetterSpawnProtect reloaded. " + protectionManager.getCenterSummary());
    }

    /**
     * Gets the ConfigManager instance.
     *
     * @return The ConfigManager instance.
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }
}