package net.honeyberries.betterSpawnProtect.manager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.StructureGrowEvent;

import java.util.Iterator;

/**
 * Listener class for handling protection-related events in the BetterSpawnProtect plugin.
 * Prevents players and entities from modifying blocks within the protected area.
 */
public class ProtectionListener implements Listener {

    // Reference to the ProtectionManager for checking protected areas
    private final ProtectionManager protectionManager;

    // Reference to the MessageGate for managing message cooldowns
    private final MessageGate messageGate;

    // Permission string for bypassing protection
    private final String bypassPerm = "betterspawnprotect.bypass";

    // Message sent to players when they attempt to modify protected blocks
    private final Component denyMessage = Component.text("You cannot place/break blocks in the protected spawn area!", NamedTextColor.RED);

    /**
     * Constructs a ProtectionListener with the given ProtectionManager and MessageGate.
     *
     * @param protectionManager The ProtectionManager instance.
     * @param messageGate The MessageGate instance.
     */
    public ProtectionListener(ProtectionManager protectionManager, MessageGate messageGate) {
        this.protectionManager = protectionManager;
        this.messageGate = messageGate;
    }

    /**
     * Denies an action if the location is within the protected area and the player lacks bypass permission.
     *
     * @param player The player attempting the action.
     * @param loc The location of the action.
     * @param e The cancellable event.
     */
    private void denyIfProtected(Player player, Location loc, Cancellable e) {
        if (!protectionManager.isProtected(loc)) return;
        if (player.hasPermission(bypassPerm)) return;
        e.setCancelled(true);
        if (messageGate.canSend(player.getUniqueId())) {
            player.sendMessage(denyMessage);
        }
    }

    /**
     * Handles block break events and cancels them if the block is in the protected area.
     *
     * @param e The BlockBreakEvent.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent e) {
        denyIfProtected(e.getPlayer(), e.getBlock().getLocation(), e);
    }

    /**
     * Handles block place events and cancels them if the block is in the protected area.
     *
     * @param e The BlockPlaceEvent.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent e) {
        denyIfProtected(e.getPlayer(), e.getBlockPlaced().getLocation(), e);
    }

    /**
     * Handles physical interactions (e.g., trampling farmland) and cancels them if in the protected area.
     *
     * @param e The PlayerInteractEvent.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPhysical(PlayerInteractEvent e) {
        if (e.getAction() == Action.PHYSICAL && e.getClickedBlock() != null) {
            denyIfProtected(e.getPlayer(), e.getClickedBlock().getLocation(), e);
        }
    }

    /**
     * Handles bucket empty events and cancels them if the location is in the protected area.
     *
     * @param e The PlayerBucketEmptyEvent.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        denyIfProtected(e.getPlayer(), e.getBlockClicked().getLocation(), e);
    }

    /**
     * Handles bucket fill events and cancels them if the location is in the protected area.
     *
     * @param e The PlayerBucketFillEvent.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBucketFill(PlayerBucketFillEvent e) {
        denyIfProtected(e.getPlayer(), e.getBlockClicked().getLocation(), e);
    }

    /**
     * Handles hanging entity break events and cancels them if the location is in the protected area.
     *
     * @param e The HangingBreakEvent.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onHangingBreak(HangingBreakEvent e) {
        Location loc = e.getEntity().getLocation();
        if (!protectionManager.isProtected(loc)) return;

        if (e instanceof HangingBreakByEntityEvent byEntity) {
            if (byEntity.getRemover() instanceof Player p) {
                if (p.hasPermission(bypassPerm)) return;
                e.setCancelled(true);
                if (messageGate.canSend(p.getUniqueId())) {
                    p.sendMessage(denyMessage);
                }
            } else {
                e.setCancelled(true);
            }
        } else {
            e.setCancelled(true);
        }
    }

    /**
     * Handles entity-caused block changes and cancels them if the block is in the protected area.
     *
     * @param e The EntityChangeBlockEvent.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityChangeBlock(EntityChangeBlockEvent e) {
        if (protectionManager.isProtected(e.getBlock().getLocation())) {
            e.setCancelled(true);
        }
    }

    /**
     * Handles entity explosion events and removes protected blocks from the affected list.
     *
     * @param e The EntityExplodeEvent.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityExplode(EntityExplodeEvent e) {
        filterExplosion(e.blockList().iterator());
    }

    /**
     * Handles block explosion events and removes protected blocks from the affected list.
     *
     * @param e The BlockExplodeEvent.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockExplode(BlockExplodeEvent e) {
        filterExplosion(e.blockList().iterator());
    }

    /**
     * Filters out protected blocks from an explosion's affected block list.
     *
     * @param it The iterator over the affected blocks.
     */
    private void filterExplosion(Iterator<Block> it) {
        while (it.hasNext()) {
            Block b = it.next();
            if (protectionManager.isProtected(b.getLocation())) {
                it.remove();
            }
        }
    }

    /**
     * Handles block ignite events and cancels them if the block is in the protected area.
     *
     * @param e The BlockIgniteEvent.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onIgnite(BlockIgniteEvent e) {
        if (protectionManager.isProtected(e.getBlock().getLocation())) {
            if (e.getPlayer() != null && e.getPlayer().hasPermission(bypassPerm)) return;
            e.setCancelled(true);
        }
    }

    /**
     * Handles block burn events and cancels them if the block is in the protected area.
     *
     * @param e The BlockBurnEvent.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBurn(BlockBurnEvent e) {
        if (protectionManager.isProtected(e.getBlock().getLocation())) {
            e.setCancelled(true);
        }
    }

    /**
     * Handles block spread events (e.g., fire, vines) and cancels them if in the protected area.
     *
     * @param e The BlockSpreadEvent.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onSpread(BlockSpreadEvent e) {
        if (protectionManager.isProtected(e.getBlock().getLocation())) {
            e.setCancelled(true);
        }
    }

    /**
     * Handles structure growth events (e.g., tree growth) and cancels them if in the protected area.
     *
     * @param e The StructureGrowEvent.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onStructureGrow(StructureGrowEvent e) {
        if (protectionManager.isProtected(e.getLocation())) {
            if (e.getPlayer() != null && e.getPlayer().hasPermission(bypassPerm)) return;
            e.setCancelled(true);
        }
    }
}