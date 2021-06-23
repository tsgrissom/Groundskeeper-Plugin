package io.github.t0xictyler.groundskeeper.task;

import io.github.t0xictyler.groundskeeper.GroundskeeperPlugin;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

public class CleanupTask extends BukkitRunnable {

    @NonNull @Getter
    private final GroundskeeperPlugin plugin;

    private final boolean bypassProtection;

    public CleanupTask(GroundskeeperPlugin plugin, boolean bypassProtection) {
        this.plugin = plugin;
        this.bypassProtection = bypassProtection;
    }

    @Override
    public void run() {
        for (World w : Bukkit.getWorlds()) {
            new CleanupWorldTask(plugin, w, bypassProtection).run();
        }
    }
}
