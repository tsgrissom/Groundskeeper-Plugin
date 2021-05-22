package io.github.t0xictyler.groundskeeper.task;

import io.github.t0xictyler.groundskeeper.GroundskeeperController;
import io.github.t0xictyler.groundskeeper.GroundskeeperPlugin;
import io.github.t0xictyler.groundskeeper.misc.Utils;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

public class CleanupTask extends BukkitRunnable {

    @RequiredArgsConstructor
    public static class CleanupWarnTask extends BukkitRunnable {

        @NonNull
        private final GroundskeeperPlugin plugin;

        @Override
        public void run() {
            GroundskeeperController controller = plugin.getController();
            long in = controller.getGlobalTaskInterval() - controller.getWarningTiming();
            String message = String.format("&e&lGroundskeeper &6> &3Clearing ground items in &e%d &3seconds...", in);

            Bukkit.broadcastMessage(Utils.color(message));
        }
    }

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
