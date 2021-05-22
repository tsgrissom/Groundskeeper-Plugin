package io.github.t0xictyler.groundskeeper;

import io.github.t0xictyler.groundskeeper.misc.Utils;
import io.github.t0xictyler.groundskeeper.task.CleanupTask;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GroundskeeperController {

    @NonNull @Getter
    private final GroundskeeperPlugin plugin;
    private int warningTaskId, globalTaskId;

    protected GroundskeeperController(GroundskeeperPlugin plugin) {
        this.plugin = plugin;

        load();
    }

    private void scheduleGlobalTask() {
        Bukkit.getLogger().info("Scheduling Groundskeeper global task...");

        if (isWarningGlobalTaskEnabled())
            this.warningTaskId = new CleanupTask.CleanupWarnTask(getPlugin().getController())
                    .runTaskLater(plugin, (getGlobalTaskInterval() - getWarningTiming()) * 20)
                    .getTaskId();

        this.globalTaskId = new CleanupTask(plugin, false)
                .runTaskTimer(plugin, 0, getGlobalTaskInterval() * 20)
                .getTaskId();
    }

    protected void load() {
        if (isGlobalTaskEnabled())
            scheduleGlobalTask();
    }

    protected void unload() {
        BukkitScheduler scheduler = Bukkit.getScheduler();

        if (warningTaskId > 0) {
            scheduler.cancelTask(warningTaskId);
        }

        if (globalTaskId > 0 ) {
            scheduler.cancelTask(globalTaskId);
        }
    }

    public void reload(CommandSender sender) {
        sender.sendMessage("Reloading plugin...");

        unload();
        getPlugin().reloadConfig();
        load();

        sender.sendMessage(Utils.color("&aSuccessfully reloaded Groundskeeper!"));
    }

    private ConfigurationSection getGlobalSection() {
        return getPlugin().getConfig().getConfigurationSection("global");
    }

    public boolean isGlobalTaskEnabled() {
        return getGlobalSection().getBoolean("enabled");
    }

    public long getGlobalTaskInterval() {
        return getGlobalSection().getLong("interval", 300);
    }

    public boolean isWarningGlobalTaskEnabled() {
        return getGlobalSection().getBoolean("warn", true);
    }

    public long getWarningTiming() {
        return getGlobalSection().getLong("warnBefore", 20);
    }

    public List<Material> getProtectedTypes() {
        FileConfiguration config = getPlugin().getConfig();

        if (!config.isSet("protected_types")) {
            return Arrays.asList(Material.DIAMOND, Material.DIAMOND_BLOCK, Material.EMERALD, Material.EMERALD_BLOCK);
        }

        List<String> strList = config.getStringList("protected_types");
        List<Material> protectedTypes = new ArrayList<>();

        for (String s : strList) {
            protectedTypes.add(Material.getMaterial(s));
        }

        return protectedTypes;
    }

    public void addProtectedType(CommandSender sender, Material material) {

    }

    public void removeProtectedType(CommandSender sender, Material material) {
        
    }
}
