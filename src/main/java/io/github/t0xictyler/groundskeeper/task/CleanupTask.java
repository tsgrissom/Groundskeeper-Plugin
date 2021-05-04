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

    public static class CleanupWarnTask extends BukkitRunnable {



        @Override
        public void run() {
            Bukkit.broadcastMessage("&e&lGroundskeeper &6> &3Clearing ground items in &e%d &3seconds...");
        }
    }

    @NonNull @Getter
    private final GroundskeeperPlugin plugin;

    @Override
    public void run() {
        for (World w : Bukkit.getWorlds()) {
            new CleanupWorldTask(plugin, w).run();
        }
    }
}
