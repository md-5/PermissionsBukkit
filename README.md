PermissionsBukkit
=================

A plugin providing groups and other permissions configuration for Bukkit's built-in permissions architecture.

Sample configuration file and more info on how the configuration is laid out follows:

```yaml
# PermissionsBukkit configuration file
# 
# A permission node is a string like 'permissions.build', usually starting
# with the name of the plugin. Refer to a plugin's documentation for what
# permissions it cares about. Each node should be followed by true to grant
# that permission or false to revoke it, as in 'permissions.build: true'.
# Some plugins provide permission nodes that map to a group of permissions -
# for example, PermissionsBukkit has 'permissions.*', which automatically
# grants all admin permissions. You can also specify false for permissions
# of this type.
# 
# Users inherit permissions from the groups they are a part of. If a user is
# not specified here, or does not have a 'groups' node, they will be in the
# group 'default'. Permissions for individual users may also be specified by
# using a 'permissions' node with a list of permission nodes, which will
# override their group permissions. World permissions may be assigned to
# users with a 'worlds:' entry.
# 
# Groups can be assigned to players and all their permissions will also be
# assigned to those players. Groups can also inherit permissions from other
# groups. Like user permissions, groups may override the permissions of their
# parent group(s). Unlike users, groups do NOT automatically inherit from
# default. World permissions may be assigned to groups with a 'worlds:' entry.
#
# The cannot-build message is configurable. If it is left blank, no message
# will be displayed to the player if PermissionsBukkit prevents them from
# building, digging, or interacting with a block. Use '&' characters to
# signify color codes.

users:
    ConspiracyWizard:
        permissions:
            permissions.example: true
        groups:
        - admin
groups:
    default:
        permissions:
            permissions.build: false
    admin:
        permissions:
            permissions.*: true
        inheritance:
        - user
    user:
        permissions:
            permissions.build: true
        worlds:
            creative:
                coolplugin.item: true
        inheritance:
        - default
messages:
    build: '&cYou do not have permission to build here.'
```
