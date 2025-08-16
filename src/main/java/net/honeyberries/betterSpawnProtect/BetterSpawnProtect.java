package net.honeyberries.betterSpawnProtect;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.honeyberries.betterSpawnProtect.command.BetterSpawnProtectCommand;
import net.honeyberries.betterSpawnProtect.configuration.ConfigManager;
import net.honeyberries.betterSpawnProtect.listener.ProtectionListener;
import net.honeyberries.betterSpawnProtect.manager.ProtectionManager;
import net.honeyberries.betterSpawnProtect.task.GamemodeTask;
import net.honeyberries.betterSpawnProtect.util.MessageGate;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.logging.Level;

/**
 * The main class for the BetterSpawnProtect plugin.
 * <p>
 * This class is responsible for initializing and managing the plugin's core components,
 * including the configuration, protection manager, event listeners, and commands.
 * It also handles the plugin's lifecycle events, such as enabling and disabling.
 */
public class BetterSpawnProtect extends JavaPlugin {

    private ProtectionManager protectionManager;
    private GamemodeTask gamemodeTask;

    /**
     * Called when the plugin is enabled.
     * <p>
     * This method initializes the plugin's components, registers event listeners,
     * starts the gamemode task, and registers the commands.
     */
    @Override
    public void onEnable() {
        // Initialize the configuration manager to handle config.yml
        ConfigManager configManager = ConfigManager.getInstance(this);

        // Initialize the protection manager to handle the protected area
        this.protectionManager = new ProtectionManager(this);

        // Initialize the message gate with a 4-second cooldown to prevent spam
        MessageGate messageGate = new MessageGate(4000L);

        // Register the event listener to protect the spawn area
        getServer().getPluginManager().registerEvents(new ProtectionListener(protectionManager, messageGate), this);

        // Start the repeating task to check player gamemodes
        this.gamemodeTask = new GamemodeTask(this, protectionManager);
        gamemodeTask.startTask();

        // Register the plugin's commands
        registerCommands();

        getLogger().info("BetterSpawnProtect has been enabled. " + protectionManager.getCenterSummary());
    }

    /**
     * Called when the plugin is disabled.
     * <p>
     * This method stops the gamemode task and saves the configuration to ensure
     * that any changes are persisted.
     */
    @Override
    public void onDisable() {
        // Stop the gamemode task to prevent it from running while the plugin is disabled
        if (gamemodeTask != null) {
            gamemodeTask.stopTask();
        }

        // Save the configuration to persist any changes made during the session
        ConfigManager configManager = ConfigManager.getInstance();
        if (configManager != null) {
            configManager.saveConfig();
        }
        getLogger().info("BetterSpawnProtect has been disabled.");
    }

    /**
     * Registers the plugin's commands using Paper's command registration system.
     * <p>
     * This method sets up the main `/bsp` command and its subcommands.
     */
    private void registerCommands() {
        BetterSpawnProtectCommand commandManager = new BetterSpawnProtectCommand(this, protectionManager);
        try {
            getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
                Commands commands = event.registrar();
                commands.register(
                        commandManager.getBuildCommand(),
                        "The main command for the BetterSpawnProtect plugin.",
                        List.of("bsp")
                );
            });
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to register commands for BetterSpawnProtect.", e);
        }
    }

    /**
     * Reloads the plugin's configuration and updates the protection manager.
     * <p>
     * This method is called when the `/bsp reload` command is executed.
     */
    public void reloadAll() {
        ConfigManager.getInstance().reloadConfig();
        protectionManager.reloadFromConfig();
        getLogger().info("BetterSpawnProtect configuration reloaded. " + protectionManager.getCenterSummary());
    }
}