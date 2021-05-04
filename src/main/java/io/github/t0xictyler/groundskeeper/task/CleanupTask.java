package io.github.t0xictyler.groundskeeper.task;

import io.github.t0xictyler.groundskeeper.GroundskeeperPlugin;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

@RequiredArgsConstructor
public class CleanupTask extends BukkitRunnable {

    @NonNull @Getter
    private final GroundskeeperPlugin plugin;

    @Override
    public void run() {
        for (World w : Bukkit.getWorlds()) {
            new CleanupWorldTask(plugin, w).run();
        }
    }
}
