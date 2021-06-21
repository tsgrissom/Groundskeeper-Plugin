# Groundskeeper

Groundskeeper is a Spigot plugin which serves to replace plugins like ClearLagg which clear ground items.

### Commands

* `/gk` - Central command
* `/gk reload` - Reload the plugin
* `/gk version` - Get plugin version
* `/gk force` - Force Groundskeeper to clean up
* `/gk global` - Modify global task settings
* `/gk protected` - List protected materials
* `/gk protect <material>` - Protect a material
* `/gk unprotect <material>` - Unprotect a material
* `/gk debug` - Toggle debugger

### Permissions

* `groundskeeper.*`
* `groundskeeper.command` - Whether the player should be allowed to use /gk commands.
* `groundskeeper.reload` - Whether the player should be able to use /gk reload.
* `groundskeeper.notify` - Whether to send notifications to the player. Default is true. Negate this in your permission manager if you want.