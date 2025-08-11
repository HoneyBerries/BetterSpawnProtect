package net.honeyberries.betterSpawnProtect.manager;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerLifecycleManager implements Listener {

    private final ProtectionManager protectionManager;
    private final Map<UUID, GameMode> previousGameModes = new HashMap<>();

    public PlayerLifecycleManager(ProtectionManager protectionManager) {
        this.protectionManager = protectionManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (protectionManager.isProtected(player.getLocation())) {
            previousGameModes.put(player.getUniqueId(), player.getGameMode());
            player.setGameMode(GameMode.ADVENTURE);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        previousGameModes.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();

        boolean fromProtected = protectionManager.isProtected(from);
        boolean toProtected = protectionManager.isProtected(to);

        if (!fromProtected && toProtected) {
            // Player entered the protected area
            previousGameModes.put(player.getUniqueId(), player.getGameMode());
            player.setGameMode(GameMode.ADVENTURE);
        } else if (fromProtected && !toProtected) {
            // Player left the protected area
            GameMode previousGameMode = previousGameModes.remove(player.getUniqueId());
            if (previousGameMode != null) {
                player.setGameMode(previousGameMode);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (protectionManager.isProtected(player.getLocation())) {
                event.setCancelled(true);
            }
        }
    }
}
