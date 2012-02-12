package com.platymuus.bukkit.permissions;

import java.util.*;
import org.bukkit.configuration.ConfigurationSection;

/**
 * A class representing the global and world nodes attached to a player or
 * group.
 */
public class PermissionInfo {

    private final PermissionsPlugin plugin;
    private final ConfigurationSection node;
    private final String groupType;

    protected PermissionInfo(PermissionsPlugin plugin, ConfigurationSection node, String groupType) {
        this.plugin = plugin;
        this.node = node;
        this.groupType = groupType;
    }

    /**
     * Gets the list of groups this group/player inherits permissions from.
     *
     * @return The list of groups.
     */
    public List<Group> getGroups() {
        ArrayList<Group> result = new ArrayList<Group>();

        for (String key : node.getStringList(groupType)) {
            Group group = plugin.getGroup(key);
            if (group != null) {
                result.add(group);
            }
        }

        return result;
    }

    /**
     * Gets a map of non-world-specific permission nodes to boolean values that
     * this group/player defines.
     *
     * @return The map of permissions.
     */
    public Map<String, Boolean> getPermissions() {
        HashMap<String, Boolean> result = new HashMap<String, Boolean>();
        for (String key : node.getConfigurationSection("permissions").getKeys(false)) {
            result.put(key, node.getConfigurationSection("permissions").getBoolean(key));
        }
        return result;
    }

    /**
     * Gets a list of worlds this group/player defines world-specific
     * permissions for.
     *
     * @return a set of all worlds present in this set of permissions
     */
    public Set<String> getWorlds() {
        if (node.getConfigurationSection("worlds") == null) {
            return new HashSet<String>();
        }
        return node.getConfigurationSection("worlds").getKeys(false);
    }

    /**
     * Gets a map of world-specific permission nodes to boolean values that this
     * group/player defines.
     *
     * @param world The world in which to get permissions for
     * @return The map of permissions.
     */
    public Map<String, Boolean> getWorldPermissions(String world) {
        HashMap<String, Boolean> result = new HashMap<String, Boolean>();
        if (node.getConfigurationSection("worlds." + world) != null) {
            for (String key : node.getConfigurationSection("worlds." + world).getKeys(false)) {
                result.put(key, node.getConfigurationSection("worlds." + world).getBoolean(key));
            }
        }
        return result;
    }
}
