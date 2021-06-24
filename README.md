# Groundskeeper

Groundskeeper is a Spigot plugin which serves to clear the ground of dropped items in a more intelligent way.

Groundskeeper will avoid removing more valuable items by default. It will 

### Plugin Integrations

* [NathanWolf's Magic plugin](https://www.spigotmc.org/resources/magic.1056/) - Groundskeeper will avoid clearing Magic wands from off the ground.

More integrations should be added as needed. Not sure what's a good idea to add.

### Configuration

[Default config](https://github.com/T0xicTyler/Groundskeeper/blob/main/src/main/resources/config.yml)

There's a bunch of configurable options in Groundskeeper. Click the link above to see the default annotated config file. Below you can read about the available options:

* `debug` - Whether the debugger should perform
* `global.enabled` - Whether to schedule the global cleanup task
* `global.interval` - How often to clear all worlds in seconds
* `protectedTypes` - Which [Materials](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html) to protect from being cleared
* `integrations.magic` - Whether to integrate with [NathanWolf's Magic](https://www.spigotmc.org/resources/magic.1056/)
* `messages` - This section contains user-facing messages

### Commands

> Perform the command `/gk ?` to receive command help

* `/gk debug` - Toggle debugger
* `/gk force` - Force Groundskeeper to clean up
* `/gk global` - Modify global task settings
* `/gk load` - Reload the plugin
* `/gk protect <material>` - Protect a material
* `/gk protected` - List protected materials
* `/gk unprotect <material>` - Unprotect a material
* `/gk version` - Get plugin version

### Permissions

* `groundskeeper.*`
* `groundskeeper.command` - Whether the player should be allowed to use /gk commands.
* `groundskeeper.reload` - Whether the player should be able to use /gk reload.
* `groundskeeper.notify` - Whether to send notifications to the player. Default is true. Negate this in your permission manager if you want.