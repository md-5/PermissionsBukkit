package com.platymuus.bukkit.permissions;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerListener implements Listener {

    private PermissionsPlugin plugin;

    public PlayerListener(PermissionsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.debug("Player " + event.getPlayer().getName() + " joined, registering...");
        plugin.registerPlayer(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.debug("Player " + event.getPlayer().getName() + " left, unregistering...");
        plugin.unregisterPlayer(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        plugin.setLastWorld(event.getPlayer().getName(), event.getTo().getWorld().getName());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.hasBlock()) {
            if (!event.getPlayer().isOp() && !event.getPlayer().hasPermission("permissions.build")) {
                if (event.getAction() != Action.PHYSICAL && plugin.getConfig().getString("messages.build", "").length() > 0) {
                    String message = plugin.getConfig().getString("messages.build", "").replace('&', '\u00A7');
                    event.getPlayer().sendMessage(message);
                }
                event.setCancelled(true);
            }
        }
    }
}
