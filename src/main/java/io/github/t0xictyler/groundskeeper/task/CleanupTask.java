package io.github.t0xictyler.groundskeeper.task;

import com.google.common.collect.ImmutableMap;
import io.github.t0xictyler.groundskeeper.GroundskeeperController;
import io.github.t0xictyler.groundskeeper.GroundskeeperPlugin;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;

public class CleanupTask extends BukkitRunnable {

    @RequiredArgsConstructor
    public static class CleanupWarnTask extends BukkitRunnable {

        @NonNull
        private final GroundskeeperPlugin plugin;

        @Override
        public void run() {
            GroundskeeperController controller = plugin.getController();
            long in = controller.getGlobalTaskInterval() - controller.getWarningTiming();
            Map<String, String> replace = ImmutableMap.of(
                    "%time%", String.valueOf(in)
            );

            controller.notify(controller.getMessage("warning", replace));
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
