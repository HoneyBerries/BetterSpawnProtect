package net.honeyberries.betterSpawnProtect;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.StructureGrowEvent;

import java.util.Iterator;

public class ProtectionListener implements Listener {

    private final ProtectionManager protectionManager;
    private final MessageGate messageGate;
    private final String bypassPerm = "betterspawnprotect.bypass";

    public ProtectionListener(ProtectionManager protectionManager, MessageGate messageGate) {
        this.protectionManager = protectionManager;
        this.messageGate = messageGate;
    }

    private void denyIfProtected(Player player, Location loc, Cancellable e) {
        if (!protectionManager.isProtected(loc)) return;
        if (player.hasPermission(bypassPerm)) return;
        e.setCancelled(true);
        if (messageGate.canSend(player.getUniqueId())) {
            player.sendMessage(ChatUtil.color(ConfigManager.getInstance().getDenyMessage()));
        }
    }

    // Player block breaks
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent e) {
        denyIfProtected(e.getPlayer(), e.getBlock().getLocation(), e);
    }

    // Player block places
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent e) {
        denyIfProtected(e.getPlayer(), e.getBlockPlaced().getLocation(), e);
    }

    // Interaction that might change blocks (e.g. trampling farmland / turtle eggs)
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPhysical(PlayerInteractEvent e) {
        if (e.getAction() == Action.PHYSICAL && e.getClickedBlock() != null) {
            denyIfProtected(e.getPlayer(), e.getClickedBlock().getLocation(), e);
        }
    }

    // Bucket empty/fill
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        if (e.getBlockClicked() != null) {
            denyIfProtected(e.getPlayer(), e.getBlockClicked().getLocation(), e);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBucketFill(PlayerBucketFillEvent e) {
        if (e.getBlockClicked() != null) {
            denyIfProtected(e.getPlayer(), e.getBlockClicked().getLocation(), e);
        }
    }

    // Hanging entity breaks (paintings, item frames) by player or explosion
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onHangingBreak(HangingBreakEvent e) {
        Location loc = e.getEntity().getLocation();
        if (!protectionManager.isProtected(loc)) return;

        if (e instanceof HangingBreakByEntityEvent byEntity) {
            if (byEntity.getRemover() instanceof Player p) {
                if (p.hasPermission(bypassPerm)) return;
                e.setCancelled(true);
                if (messageGate.canSend(p.getUniqueId())) {
                    p.sendMessage(ChatUtil.color(ConfigManager.getInstance().getDenyMessage()));
                }
            } else {
                // Entity-caused (arrow, creeper etc.)
                e.setCancelled(true);
            }
        } else {
            // Explosion or other cause
            e.setCancelled(true);
        }
    }

    // Entity caused block changes: enderman pickup/place, wither, falling blocks landing, ravager, silverfish, etc.
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityChangeBlock(EntityChangeBlockEvent e) {
        if (protectionManager.isProtected(e.getBlock().getLocation())) {
            e.setCancelled(true);
        }
    }

    // Explosions – remove protected blocks from the affected list
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityExplode(EntityExplodeEvent e) {
        filterExplosion(e.blockList().iterator());
    }

    // Block explosions (beds / respawn anchors, etc.)
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockExplode(BlockExplodeEvent e) {
        filterExplosion(e.blockList().iterator());
    }

    private void filterExplosion(Iterator<Block> it) {
        while (it.hasNext()) {
            Block b = it.next();
            if (protectionManager.isProtected(b.getLocation())) {
                it.remove();
            }
        }
    }

    // Prevent fire spread / ignition inside protection
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onIgnite(BlockIgniteEvent e) {
        if (protectionManager.isProtected(e.getBlock().getLocation())) {
            // Allow if caused by player with bypass? Usually you want to block if no bypass.
            if (e.getPlayer() != null && e.getPlayer().hasPermission(bypassPerm)) return;
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBurn(BlockBurnEvent e) {
        if (protectionManager.isProtected(e.getBlock().getLocation())) {
            e.setCancelled(true);
        }
    }

    // Optional: stop block spread (e.g. fire, vines) inside region
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onSpread(BlockSpreadEvent e) {
        if (protectionManager.isProtected(e.getBlock().getLocation())) {
            e.setCancelled(true);
        }
    }

    // Optional: prevent unwanted growth altering protected blocks (tree growth replacing blocks)
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onStructureGrow(StructureGrowEvent e) {
        if (protectionManager.isProtected(e.getLocation())) {
            // If player triggered and has bypass, allow
            if (e.getPlayer() != null && e.getPlayer().hasPermission(bypassPerm)) return;
            e.setCancelled(true);
        }
    }
}