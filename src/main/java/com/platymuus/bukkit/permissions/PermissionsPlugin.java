package com.platymuus.bukkit.permissions;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main class for PermissionsBukkit.
 */
public class PermissionsPlugin extends JavaPlugin {

    private BuildListener blockListener = new BuildListener(this);
    private PlayerListener playerListener = new PlayerListener(this);
    private PermissionsCommand commandExecutor = new PermissionsCommand(this);
    private HashMap<String, PermissionAttachment> permissions = new HashMap<String, PermissionAttachment>();
    private HashMap<String, String> lastWorld = new HashMap<String, String>();

    // -- Basic stuff
    @Override
    public void onEnable() {
        // Write some default configuration
        if (!new File(getDataFolder(), "config.yml").exists()) {
            getLogger().info("Generating default configuration");
            saveDefaultConfig();
        }

        // Commands
        getCommand("permissions").setExecutor(commandExecutor);

        // Events
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(blockListener, this);
        pm.registerEvents(playerListener, this);

        // Register everyone online right now
        for (Player p : getServer().getOnlinePlayers()) {
            registerPlayer(p);
        }
    }

    @Override
    public void onDisable() {
        // Unregister everyone
        for (Player p : getServer().getOnlinePlayers()) {
            unregisterPlayer(p);
        }
    }

    // -- External API
    /**
     * Get the group with the given name.
     *
     * @param groupName The name of the group.
     * @return A Group if it exists or null otherwise.
     */
    public Group getGroup(String groupName) {
        if (getNode("groups") != null) {
            for (String key : getNode("groups").getKeys(false)) {
                if (key.equalsIgnoreCase(groupName)) {
                    return new Group(this, key);
                }
            }
        }
        return null;
    }

    /**
     * Returns a list of groups a player is in.
     *
     * @param playerName The name of the player.
     * @return The groups this player is in. May be empty.
     */
    public List<Group> getGroups(String playerName) {
        ArrayList<Group> result = new ArrayList<Group>();
        if (getNode("users." + playerName) != null) {
            for (String key : getNode("users." + playerName).getStringList("groups")) {
                result.add(new Group(this, key));
            }
        } else {
            result.add(new Group(this, "default"));
        }
        return result;
    }

    /**
     * Returns permission info on the given player.
     *
     * @param playerName The name of the player.
     * @return A PermissionsInfo about this player.
     */
    public PermissionInfo getPlayerInfo(String playerName) {
        if (getNode("users." + playerName) == null) {
            return null;
        } else {
            return new PermissionInfo(this, getNode("users." + playerName), "groups");
        }
    }

    /**
     * Returns a list of all defined groups.
     *
     * @return The list of groups.
     */
    public List<Group> getAllGroups() {
        ArrayList<Group> result = new ArrayList<Group>();
        if (getNode("groups") != null) {
            for (String key : getNode("groups").getKeys(false)) {
                result.add(new Group(this, key));
            }
        }
        return result;
    }

    // -- Plugin stuff
    protected void registerPlayer(Player player) {
        if (permissions.containsKey(player.getName())) {
            debug("Registering " + player.getName() + ": was already registered");
            unregisterPlayer(player);
        }
        PermissionAttachment attachment = player.addAttachment(this);
        permissions.put(player.getName(), attachment);
        setLastWorld(player.getName(), player.getWorld().getName());
    }

    protected void unregisterPlayer(Player player) {
        if (permissions.containsKey(player.getName())) {
            try {
                player.removeAttachment(permissions.get(player.getName()));
            } catch (IllegalArgumentException ex) {
                debug("Unregistering " + player.getName() + ": player did not have attachment");
            }
            permissions.remove(player.getName());
            lastWorld.remove(player.getName());
        } else {
            debug("Unregistering " + player.getName() + ": was not registered");
        }
    }

    protected void setLastWorld(String player, String world) {
        if (permissions.containsKey(player) && (lastWorld.get(player) == null || !lastWorld.get(player).equals(world))) {
            debug("Player " + player + " moved to world " + world + ", recalculating...");
            lastWorld.put(player, world);
            calculateAttachment(getServer().getPlayer(player));
        }
    }

    protected void refreshPermissions() {
        saveConfig();
        for (String player : permissions.keySet()) {
            PermissionAttachment attachment = permissions.get(player);
            for (String key : attachment.getPermissions().keySet()) {
                attachment.unsetPermission(key);
            }

            calculateAttachment(getServer().getPlayer(player));
        }
    }

    protected ConfigurationSection getNode(String child) {
        return getNode("", child);
    }

    protected void debug(String message) {
        if (getConfig().getBoolean("debug", false)) {
            getLogger().info("[Debug] " + message);
        }
    }

    // -- Private stuff
    private ConfigurationSection getNode(String parent, String child) {
        ConfigurationSection parentNode = null;
        if (child.contains(".")) {
            int index = child.lastIndexOf('.');
            parentNode = getNode("", child.substring(0, index));
            child = child.substring(index + 1);
        } else if (parent.length() == 0) {
            parentNode = getConfig();
        } else if (parent.contains(".")) {
            int index = parent.indexOf('.');
            parentNode = getNode(parent.substring(0, index), parent.substring(index + 1));
        } else {
            parentNode = getNode("", parent);
        }

        if (parentNode == null) {
            return null;
        }

        for (String entry : parentNode.getKeys(false)) {
            if (child.equalsIgnoreCase(entry)) {
                return parentNode.getConfigurationSection(entry);
            }
        }
        return null;
    }

    private void calculateAttachment(Player player) {
        if (player == null) {
            return;
        }
        PermissionAttachment attachment = permissions.get(player.getName());
        if (attachment == null) {
            debug("Calculating permissions on " + player.getName() + ": attachment was null");
            return;
        }

        for (String key : attachment.getPermissions().keySet()) {
            attachment.unsetPermission(key);
        }

        for (Map.Entry<String, Object> entry : calculatePlayerPermissions(player.getName().toLowerCase(), lastWorld.get(player.getName())).entrySet()) {
            if (entry.getValue() != null && entry.getValue() instanceof Boolean) {
                attachment.setPermission(entry.getKey(), (Boolean) entry.getValue());
            } else {
                getLogger().warning(" Node " + entry.getKey() + " for player " + player.getName() + " is non-Boolean");
            }
        }

        player.recalculatePermissions();
    }

    private Map<String, Object> calculatePlayerPermissions(String player, String world) {
        if (getNode("users." + player) == null) {
            return calculateGroupPermissions("default", world);
        }

        Map<String, Object> perms = getNode("users." + player + ".permissions") == null ? new HashMap<String, Object>() : getNode("users." + player + ".permissions").getValues(false);

        if (getNode("users." + player + ".worlds." + world) != null) {
            for (Map.Entry<String, Object> entry : getNode("users." + player + ".worlds." + world).getValues(false).entrySet()) {
                // No containskey; world overrides non-world
                perms.put(entry.getKey(), entry.getValue());
            }
        }

        for (String group : getNode("users." + player).getStringList("groups")) {
            for (Map.Entry<String, Object> entry : calculateGroupPermissions(group, world).entrySet()) {
                if (!perms.containsKey(entry.getKey())) { // User overrides group
                    perms.put(entry.getKey(), entry.getValue());
                }
            }
        }

        return perms;
    }

    private Map<String, Object> calculateGroupPermissions(String group, String world) {
        if (getNode("groups." + group) == null) {
            return new HashMap<String, Object>();
        }

        Map<String, Object> perms = getNode("groups." + group + ".permissions") == null ? new HashMap<String, Object>() : getNode("groups." + group + ".permissions").getValues(false);

        if (getNode("groups." + group + ".worlds." + world) != null) {
            for (Map.Entry<String, Object> entry : getNode("groups." + group + ".worlds." + world).getValues(false).entrySet()) {
                // No containskey; world overrides non-world
                perms.put(entry.getKey(), entry.getValue());
            }
        }

        for (String parent : getNode("groups." + group).getStringList("inheritance")) {
            for (Map.Entry<String, Object> entry : calculateGroupPermissions(parent, world).entrySet()) {
                if (!perms.containsKey(entry.getKey())) { // Children override permissions
                    perms.put(entry.getKey(), entry.getValue());
                }
            }
        }

        return perms;
    }
}
