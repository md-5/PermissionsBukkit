package com.platymuus.bukkit.permissions;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockListener implements Listener {

    private PermissionsPlugin plugin;

    public BlockListener(PermissionsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!event.getPlayer().isOp() && !event.getPlayer().hasPermission("permissions.build")) {
            if (plugin.getConfig().getString("messages.build", "").length() > 0) {
                String message = plugin.getConfig().getString("messages.build", "").replace('&', '\u00A7');
                event.getPlayer().sendMessage(message);
            }
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!event.getPlayer().isOp() && !event.getPlayer().hasPermission("permissions.build")) {
            if (plugin.getConfig().getString("messages.build", "").length() > 0) {
                String message = plugin.getConfig().getString("messages.build", "").replace('&', '\u00A7');
                event.getPlayer().sendMessage(message);
            }
            event.setCancelled(true);
        }
    }
}
