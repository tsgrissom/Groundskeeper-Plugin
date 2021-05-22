package io.github.t0xictyler.groundskeeper.task;

import com.google.common.collect.ImmutableMap;
import io.github.t0xictyler.groundskeeper.GroundskeeperController;
import io.github.t0xictyler.groundskeeper.misc.GKWorld;
import io.github.t0xictyler.groundskeeper.GroundskeeperPlugin;
import javafx.util.Pair;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;

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
        GroundskeeperController controller = getPlugin().getController();
        Pair<Integer, Integer> cleared = getWorld().clearGroundEntities(shouldBypassProtection());
        int totalCleared = cleared.getKey();
        int stacksCleared = cleared.getValue();
        Map<String, String> replace = ImmutableMap.of(
                "%totalCleared%", String.valueOf(totalCleared),
                "%stacksCleared%", String.valueOf(stacksCleared),
                "%world%", getWorldName()
                );

        Bukkit.broadcastMessage(controller.getMessage("cleared", replace));
    }
}
