package net.honeyberries.betterSpawnProtect;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.honeyberries.betterSpawnProtect.command.CommandManager;
import net.honeyberries.betterSpawnProtect.configuration.ConfigManager;
import net.honeyberries.betterSpawnProtect.listener.ProtectionListener;
import net.honeyberries.betterSpawnProtect.manager.ProtectionManager;
import net.honeyberries.betterSpawnProtect.task.GamemodeTask;
import net.honeyberries.betterSpawnProtect.util.MessageGate;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.logging.Level;

/**
 * Main class for the BetterSpawnProtect plugin.
 * Handles plugin initialization, lifecycle, and core components.
 */
public class BetterSpawnProtect extends JavaPlugin {

    private ProtectionManager protectionManager;
    private GamemodeTask gamemodeTask;

    @Override
    public void onEnable() {
        // Initialize configuration manager
        ConfigManager configManager = ConfigManager.getInstance(this);

        // Initialize protection manager
        this.protectionManager = new ProtectionManager(this);

        // Initialize message gate (4s cooldown)
        MessageGate messageGate = new MessageGate(4000L);

        // Register event listeners
        Bukkit.getPluginManager().registerEvents(new ProtectionListener(protectionManager, messageGate), this);

        // Start repeating gamemode task
        this.gamemodeTask = new GamemodeTask(this, protectionManager);
        gamemodeTask.startTask();

        // Register commands
        registerCommands();

        getLogger().info("BetterSpawnProtect has been enabled. " + protectionManager.getCenterSummary());
    }

    @Override
    public void onDisable() {
        // Stop gamemode task
        if (gamemodeTask != null) {
            gamemodeTask.stopTask();
        }

        // Save configuration
        ConfigManager configManager = ConfigManager.getInstance();
        if (configManager != null) {
            configManager.saveConfig();
        }
        getLogger().info("BetterSpawnProtect has been disabled.");
    }

    /**
     * Registers the plugin's commands.
     */
    private void registerCommands() {
        CommandManager commandManager = new CommandManager(this, protectionManager);
        try {
            getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
                Commands commands = event.registrar();
                commands.register(
                        commandManager.getBuildCommand(),
                        "Main command for BetterSpawnProtect",
                        List.of("bsp")
                );
            });
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to register commands", e);
        }
    }

    /**
     * Reloads the plugin's configuration and updates the protection manager.
     */
    public void reloadAll() {
        ConfigManager.getInstance().reloadConfig();
        protectionManager.reloadFromConfig();
        getLogger().info("BetterSpawnProtect configuration reloaded. " + protectionManager.getCenterSummary());
    }
}