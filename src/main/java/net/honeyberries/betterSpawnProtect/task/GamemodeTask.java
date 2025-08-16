package net.honeyberries.betterSpawnProtect.task;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.honeyberries.betterSpawnProtect.manager.ProtectionManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * A repeating task that periodically checks and updates player game modes based on whether
 * they are inside or outside the protected spawn area. This task ensures that players
 * without bypass permissions are in Adventure mode while inside the spawn, and Survival
 * mode when outside.
 */
public class GamemodeTask {
    private final ProtectionManager protectionManager;
    private final Plugin plugin;
    private ScheduledTask task;

    /**
     * Constructs a new {@code GamemodeTask} with the specified plugin and protection manager.
     *
     * @param plugin            The main plugin instance.
     * @param protectionManager The manager for the protected area.
     */
    public GamemodeTask(Plugin plugin, ProtectionManager protectionManager) {
        this.plugin = plugin;
        this.protectionManager = protectionManager;
    }

    /**
     * Starts the repeating task that checks and updates player game modes. The task runs
     * once per tick. It iterates through all online players and adjusts their game mode
     * based on their location and permissions.
     */
    public void startTask() {
        task = Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, globalTask -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.getScheduler().run(plugin, scheduledTask -> {
                    // Skip players who are not in Survival or Adventure, or who are dead
                    if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE && player.isDead()) {
                        return;
                    }

                    boolean isInProtectedArea = protectionManager.isProtected(player.getLocation());
                    boolean hasBypassPermission = player.hasPermission("betterspawnprotect.bypass");

                    if (isInProtectedArea) {
                        // If the player has bypass permission and is in Adventure mode, switch to Survival
                        if (hasBypassPermission && player.getGameMode() == GameMode.ADVENTURE) {
                            player.setGameMode(GameMode.SURVIVAL);
                        }
                        // If the player does not have bypass permission and is in Survival mode, switch to Adventure
                        else if (!hasBypassPermission && player.getGameMode() == GameMode.SURVIVAL) {
                            player.setGameMode(GameMode.ADVENTURE);
                        }
                    } else if (player.getGameMode() == GameMode.ADVENTURE) {
                        // If the player is outside the protected area and in Adventure mode, switch to Survival
                        player.setGameMode(GameMode.SURVIVAL);
                    }
                }, null);
            }
        },
            1, 1); // Run the task every tick
    }

    /**
     * Stops the repeating task, preventing further game mode updates. This should be called
     * when the plugin is disabled.
     */
    public void stopTask() {
        if (task != null) {
            task.cancel();
        }
    }
}