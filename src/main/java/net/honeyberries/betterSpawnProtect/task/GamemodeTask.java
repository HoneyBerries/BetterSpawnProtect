package net.honeyberries.betterSpawnProtect.task;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.honeyberries.betterSpawnProtect.manager.ProtectionManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * Periodically checks and updates player game modes based on spawn protection.
 */
public class GamemodeTask {
    private final ProtectionManager protectionManager;
    private final Plugin plugin;
    private ScheduledTask task;

    public GamemodeTask(Plugin plugin, ProtectionManager protectionManager) {
        this.plugin = plugin;
        this.protectionManager = protectionManager;
    }

    /**
     * Starts the repeating task.
     */
    public void startTask() {
        task = Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, globalTask -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.getScheduler().run(plugin, scheduledTask -> {
                    if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE && player.isDead()) {
                        return;
                    }
                    boolean isInProtectedArea = protectionManager.isProtected(player.getLocation());
                    boolean hasBypassPermission = player.hasPermission("betterspawnprotect.bypass");

                    if (isInProtectedArea) {
                        if (hasBypassPermission && player.getGameMode() == GameMode.ADVENTURE) {
                            player.setGameMode(GameMode.SURVIVAL);
                        } else if (!hasBypassPermission && player.getGameMode() == GameMode.SURVIVAL) {
                            player.setGameMode(GameMode.ADVENTURE);
                        }
                    } else if (player.getGameMode() == GameMode.ADVENTURE) {
                        player.setGameMode(GameMode.SURVIVAL);
                    }
                }, null);
            }
        },
            1, 1);
    }

    /**
     * Stops the repeating task.
     */
    public void stopTask() {
        if (task != null) {
            task.cancel();
        }
    }
}