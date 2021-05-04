package io.github.t0xictyler.groundskeeper.task;

import io.github.t0xictyler.groundskeeper.misc.GKWorld;
import io.github.t0xictyler.groundskeeper.GroundskeeperPlugin;
import io.github.t0xictyler.groundskeeper.misc.Utils;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

public class CleanupWorldTask extends BukkitRunnable {

    @NonNull @Getter
    private final GroundskeeperPlugin plugin;

    @NonNull @Getter
    private final String worldName;

    public GKWorld getWorld() {
        return new GKWorld(getPlugin().getController(), getWorldName());
    }

    public CleanupWorldTask(GroundskeeperPlugin plugin, String worldName) {
        World w = Bukkit.getWorld(worldName);

        if (w == null) {
            throw new NullPointerException(String.format("Unknown world '%s'", worldName));
        }

        this.plugin = plugin;
        this.worldName = worldName;
    }

    public CleanupWorldTask(GroundskeeperPlugin plugin, World world) {
        this(plugin, world.getName());
    }

    @Override
    public void run() {
        int clearedGroundEntities = getWorld().clearGroundEntities();
        String message = String.format("&e&lGroundskeeper &7&o<%s> &6Cleared &c%d &6ground items", getWorldName(), clearedGroundEntities);

        Bukkit.broadcastMessage(Utils.color(message));
    }
}
