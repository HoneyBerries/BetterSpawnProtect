package net.honeyberries.betterSpawnProtect.listener;

import net.honeyberries.betterSpawnProtect.manager.ProtectionManager;
import net.honeyberries.betterSpawnProtect.util.MessageGate;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.StructureGrowEvent;

/**
 * The `ProtectionListener` class is responsible for handling all events related to spawn protection.
 * It listens for various player and world events and cancels them if they occur within the protected
 * spawn area and the player does not have bypass permissions. This class is crucial for enforcing
 * the protection rules defined in the {@link ProtectionManager}.
 */
public class ProtectionListener implements Listener {

    private final ProtectionManager protectionManager;
    private final MessageGate messageGate;
    private static final String BYPASS_PERMISSION = "betterspawnprotect.bypass";
    private static final Component DENY_MESSAGE = Component.text("You cannot modify the protected spawn area!", NamedTextColor.RED);

    /**
     * Constructs a new `ProtectionListener` with the specified {@link ProtectionManager} and {@link MessageGate}.
     *
     * @param protectionManager The manager responsible for the protected area.
     * @param messageGate       The gatekeeper for sending messages to players, preventing spam.
     */
    public ProtectionListener(ProtectionManager protectionManager, MessageGate messageGate) {
        this.protectionManager = protectionManager;
        this.messageGate = messageGate;
    }

    /**
     * Checks if a player is allowed to bypass the spawn protection.
     *
     * @param player The player to check.
     * @return {@code true} if the player can bypass protection (e.g., is in creative/spectator mode or has the bypass permission), {@code false} otherwise.
     */
    private boolean canBypass(Player player) {
        return player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR || player.hasPermission(BYPASS_PERMISSION);
    }

    /**
     * A generic handler for player actions that might be restricted. It checks if the action is
     * within a protected area and if the player has bypass permissions. If the action is not allowed,
     * the event is cancelled, and the player is notified.
     *
     * @param player The player performing the action.
     * @param loc    The location where the action is occurring.
     * @param event  The event to be cancelled if the action is disallowed.
     */
    private void handlePlayerAction(Player player, Location loc, Cancellable event) {
        if (canBypass(player)) {
            return; // Player has bypass permissions, so we don't need to do anything.
        }

        if (protectionManager.isProtected(loc)) {
            event.setCancelled(true);
            if (messageGate.canSend(player.getUniqueId())) {
                player.sendMessage(DENY_MESSAGE);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent e) {
        handlePlayerAction(e.getPlayer(), e.getBlock().getLocation(), e);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent e) {
        handlePlayerAction(e.getPlayer(), e.getBlockPlaced().getLocation(), e);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        handlePlayerAction(e.getPlayer(), e.getBlockClicked().getLocation(), e);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBucketFill(PlayerBucketFillEvent e) {
        handlePlayerAction(e.getPlayer(), e.getBlockClicked().getLocation(), e);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getClickedBlock() != null) {
            handlePlayerAction(e.getPlayer(), e.getClickedBlock().getLocation(), e);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent e) {
        handlePlayerAction(e.getPlayer(), e.getRightClicked().getLocation(), e);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPistonExtend(BlockPistonExtendEvent e) {
        for (Block block : e.getBlocks()) {
            if (protectionManager.isProtected(block.getRelative(e.getDirection()).getLocation())) {
                e.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPistonRetract(BlockPistonRetractEvent e) {
        for (Block block : e.getBlocks()) {
            if (protectionManager.isProtected(block.getLocation())) {
                e.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onHangingBreak(HangingBreakEvent e) {
        Location loc = e.getEntity().getLocation();
        if (!protectionManager.isProtected(loc)) {
            return;
        }

        if (e instanceof HangingBreakByEntityEvent byEntityEvent) {
            if (byEntityEvent.getRemover() instanceof Player player) {
                handlePlayerAction(player, loc, e);
            } else {
                e.setCancelled(true);
            }
        } else {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityChangeBlock(EntityChangeBlockEvent e) {
        if (protectionManager.isProtected(e.getBlock().getLocation())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityExplode(EntityExplodeEvent e) {
        e.blockList().removeIf(block -> protectionManager.isProtected(block.getLocation()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockExplode(BlockExplodeEvent e) {
        e.blockList().removeIf(block -> protectionManager.isProtected(block.getLocation()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onIgnite(BlockIgniteEvent e) {
        if (e.getPlayer() != null) {
            handlePlayerAction(e.getPlayer(), e.getBlock().getLocation(), e);
        } else if (protectionManager.isProtected(e.getBlock().getLocation())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBurn(BlockBurnEvent e) {
        if (protectionManager.isProtected(e.getBlock().getLocation())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onSpread(BlockSpreadEvent e) {
        if (protectionManager.isProtected(e.getBlock().getLocation())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onStructureGrow(StructureGrowEvent e) {
        if (e.getPlayer() != null) {
            handlePlayerAction(e.getPlayer(), e.getLocation(), e);
        } else if (protectionManager.isProtected(e.getLocation())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockForm(BlockFormEvent e) {
        if (protectionManager.isProtected(e.getBlock().getLocation())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockFade(BlockFadeEvent e) {
        if (protectionManager.isProtected(e.getBlock().getLocation())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockFromTo(BlockFromToEvent e) {
        if (protectionManager.isProtected(e.getToBlock().getLocation())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockFertilize(BlockFertilizeEvent e) {
        if (e.getPlayer() != null) {
            handlePlayerAction(e.getPlayer(), e.getBlock().getLocation(), e);
        } else if (protectionManager.isProtected(e.getBlock().getLocation())) {
            e.setCancelled(true);
        }
    }

    /**
     * Prevents players from damaging non-player entities within the protected spawn area.
     * This helps to protect ambient creatures and other entities that contribute to the
     * spawn's environment.
     *
     * @param e The {@link EntityDamageByEntityEvent} triggered when an entity damages another.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        Entity damager = e.getDamager();
        Entity victim = e.getEntity();

        // Check if the action is taking place in a protected area
        if (protectionManager.isProtected(victim.getLocation())) {
            // Prevent players from attacking non-player entities
            e.setCancelled(true);
            if (damager instanceof Player player && messageGate.canSend(player.getUniqueId())) {
                player.sendMessage(DENY_MESSAGE);
            }
        }
    }
}