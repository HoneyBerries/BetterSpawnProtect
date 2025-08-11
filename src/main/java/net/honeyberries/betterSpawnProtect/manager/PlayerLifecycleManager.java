package net.honeyberries.betterSpawnProtect.manager;

import net.honeyberries.betterSpawnProtect.BetterSpawnProtect;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;

/**
 * Handles player lifecycle events.
 * Game mode switching is now handled by the tick-based system in the main plugin class.
 */
public class PlayerLifecycleManager implements Listener {
    private final ProtectionManager protectionManager;

    private final BetterSpawnProtect plugin = BetterSpawnProtect.getInstance();

    /**
     * Constructs a new PlayerLifecycleManager.
     *
     * @param protectionManager the protection manager to check protected areas
     */
    public PlayerLifecycleManager(ProtectionManager protectionManager) {
        this.protectionManager = protectionManager;
    }

    /**
     * Handles player quit events.
     * Currently just maintains the reference for potential future use.
     *
     * @param event the player quit event
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Cleanup can be added here if needed in the future
    }
}