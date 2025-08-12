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

import java.util.Iterator;

/**
 * Listener for handling events related to spawn protection.
 * This class prevents unauthorized modifications to the protected spawn area.
 */
public class ProtectionListener implements Listener {

    private final ProtectionManager protectionManager;
    private final MessageGate messageGate;
    private final String bypassPerm = "betterspawnprotect.bypass";
    private final Component denyMessage = Component.text("You cannot modify the protected spawn area!", NamedTextColor.RED);

    /**
     * Constructs a new ProtectionListener.
     *
     * @param protectionManager The manager for the protected area.
     * @param messageGate       The gatekeeper for sending messages to players.
     */
    public ProtectionListener(ProtectionManager protectionManager, MessageGate messageGate) {
        this.protectionManager = protectionManager;
        this.messageGate = messageGate;
    }

    /**
     * Checks if a player can bypass protection.
     *
     * @param player The player to check.
     * @return {@code true} if the player can bypass, {@code false} otherwise.
     */
    private boolean canBypass(Player player) {
        return player.getGameMode() == GameMode.CREATIVE || player.hasPermission(bypassPerm);
    }

    /**
     * Handles a player action, cancelling it if the location is protected and the player cannot bypass.
     *
     * @param player The player performing the action.
     * @param loc    The location of the action.
     * @param event  The event to cancel.
     */
    private void handlePlayerAction(Player player, Location loc, Cancellable event) {
        if (canBypass(player)) {
            return;
        }

        if (protectionManager.isProtected(loc)) {
            event.setCancelled(true);
            if (messageGate.canSend(player.getUniqueId())) {
                player.sendMessage(denyMessage);
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

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        Entity damager = e.getDamager();
        Entity victim = e.getEntity();

        // Allow PvP
        if (victim instanceof Player && damager instanceof Player) {
            return;
        }

        // If the action is in a protected area
        if (protectionManager.isProtected(victim.getLocation())) {
            // Prevent players from attacking non-player entities
            if (damager instanceof Player player && !canBypass(player)) {
                e.setCancelled(true);
                if (messageGate.canSend(player.getUniqueId())) {
                    player.sendMessage(denyMessage);
                }
            }
            // Prevent non-player entities from attacking players
            else if (victim instanceof Player && !(damager instanceof Player)) {
                e.setCancelled(true);
            }
        }
    }
}