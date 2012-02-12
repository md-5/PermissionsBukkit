package com.platymuus.bukkit.permissions;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class BuildListener implements Listener {

    private PermissionsPlugin plugin;

    public BuildListener(PermissionsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        handle(event, event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        handle(event, event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        handle(event, event.getPlayer());
    }

    private void handle(Cancellable event, Player player) {
        if (!player.hasPermission("permissions.build")) {
            if (plugin.getConfig().getString("messages.build", "").length() > 0) {
                String message = plugin.getConfig().getString("messages.build", "").replace('&', '\u00A7');
                player.sendMessage(message);
            }
            event.setCancelled(true);
        }
    }
}
