package io.github.t0xictyler.groundskeeper;

import io.github.t0xictyler.groundskeeper.command.GroundskeeperCommand;
import io.github.t0xictyler.groundskeeper.task.CleanupTask;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.command.Command;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.permission.ChildPermission;
import org.bukkit.plugin.java.annotation.permission.Permission;
import org.bukkit.plugin.java.annotation.permission.Permissions;
import org.bukkit.plugin.java.annotation.plugin.Description;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.Website;
import org.bukkit.plugin.java.annotation.plugin.author.Author;
import org.bukkit.plugin.java.annotation.plugin.author.Authors;

@Plugin(name = "Groundskeeper", version = "1.0.0")
@Authors({
        @Author("T0xicTyler")
})
@Description("Smart entity cleaner")
@Website("https://github.com/T0xicTyler/Groundskeeper")
@Commands({
        @Command(name = "groundskeeper", aliases = "gk")
})
@Permissions({
        @Permission(name = "groundskeeper.*", children = {
                @ChildPermission(name = "groundskeeper.command")
        }),
        @Permission(name = "groundskeeper.command")
})
public class GroundskeeperPlugin extends JavaPlugin {

    @Getter
    private GroundskeeperPlugin plugin;
    @Getter
    private GroundskeeperController controller;

    @Override
    public void onEnable() {
        plugin = this;

        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        controller = new GroundskeeperController(this);

        PluginCommand cmd = getCommand("groundskeeper");

        if (cmd != null) {
            GroundskeeperCommand command = new GroundskeeperCommand();

            cmd.setExecutor(command);
            cmd.setTabCompleter(command);
        }
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
    }
}
