package io.github.t0xictyler.groundskeeper.task;

import io.github.t0xictyler.groundskeeper.misc.GKWorld;
import io.github.t0xictyler.groundskeeper.GroundskeeperPlugin;
import io.github.t0xictyler.groundskeeper.misc.Utils;
import javafx.util.Pair;
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

    private final boolean bypassProtection;

    public boolean shouldBypassProtection() { return this.bypassProtection; }

    public GKWorld getWorld() {
        return new GKWorld(getPlugin().getController(), getWorldName());
    }

    public CleanupWorldTask(GroundskeeperPlugin plugin, String worldName, boolean bypassProtection) {
        World w = Bukkit.getWorld(worldName);

        if (w == null) {
            throw new NullPointerException(String.format("Unknown world '%s'", worldName));
        }

        this.plugin = plugin;
        this.worldName = worldName;
        this.bypassProtection = bypassProtection;
    }

    public CleanupWorldTask(GroundskeeperPlugin plugin, World world, boolean bypassProtection) {
        this(plugin, world.getName(), bypassProtection);
    }

    @Override
    public void run() {
        Pair<Integer, Integer> cleared = getWorld().clearGroundEntities(bypassProtection);
        int totalCleared = cleared.getKey();
        int stacksCleared = cleared.getValue();
        String message = String.format("&e&lGroundskeeper &7&o<%s> &6Cleared &c%d &6ground items", getWorldName(), totalCleared);

        Bukkit.broadcastMessage(Utils.color(message));
    }
}
