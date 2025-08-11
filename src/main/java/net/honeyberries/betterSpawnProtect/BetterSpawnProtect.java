package net.honeyberries.betterSpawnProtect;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.honeyberries.betterSpawnProtect.command.CommandManager;
import net.honeyberries.betterSpawnProtect.manager.ConfigManager;
import net.honeyberries.betterSpawnProtect.manager.MessageGate;
import net.honeyberries.betterSpawnProtect.manager.PlayerLifecycleManager;
import net.honeyberries.betterSpawnProtect.manager.ProtectionListener;
import net.honeyberries.betterSpawnProtect.manager.ProtectionManager;
import net.honeyberries.betterSpawnProtect.task.GamemodeTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * Main class for the BetterSpawnProtect plugin.
 * Handles plugin lifecycle, core managers, listeners, and command registration.
 */
public class BetterSpawnProtect extends JavaPlugin {

    private static BetterSpawnProtect instance;

    private ProtectionManager protectionManager;
    private MessageGate messageGate;
    private ConfigManager configManager;
    private GamemodeTask gamemodeTask;

    /**
     * Gets the singleton instance of the plugin.
     */
    public static BetterSpawnProtect getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        // Initialize config manager
        this.configManager = ConfigManager.getInstance(this);

        // Initialize protection manager
        this.protectionManager = new ProtectionManager();

        // Initialize message gate (2s cooldown)
        this.messageGate = new MessageGate(8000L);

        // Register listeners
        Bukkit.getPluginManager().registerEvents(new ProtectionListener(protectionManager, messageGate), this);
        Bukkit.getPluginManager().registerEvents(new PlayerLifecycleManager(protectionManager), this);

        // Start repeating gamemode task
        this.gamemodeTask = new GamemodeTask(this, protectionManager);
        gamemodeTask.startTask();

        // Register commands
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
            getLogger().warning("Failed to register commands via lifecycle manager: " + e.getMessage());
        }

        getLogger().info("BetterSpawnProtect enabled. " + protectionManager.getCenterSummary());
    }

    @Override
    public void onDisable() {
        // Stop gamemode task
        gamemodeTask.stopTask();

        // Save config
        if (configManager != null) {
            configManager.saveConfig();
        }
        getLogger().info("BetterSpawnProtect disabled.");
    }

    /**
     * Reloads plugin configuration and updates protection manager.
     */
    public void reloadAll() {
        if (configManager != null) {
            configManager.reloadConfig();
        }
        protectionManager.reloadFromConfig();
        getLogger().info("BetterSpawnProtect reloaded. " + protectionManager.getCenterSummary());
    }

}