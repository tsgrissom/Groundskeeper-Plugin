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

import java.util.*;

public class GroundskeeperController {

    @NonNull @Getter
    private final GroundskeeperPlugin plugin;
    private int warningTaskId, globalTaskId;

    @Getter
    private Set<Material> protectedTypes;

    protected GroundskeeperController(GroundskeeperPlugin plugin) {
        this.plugin = plugin;
        this.protectedTypes = new HashSet<>();

        load();
    }

    private void scheduleGlobalTask() {
        Bukkit.getLogger().info("Scheduling Groundskeeper global task...");

        if (isWarningGlobalTaskEnabled())
            this.warningTaskId = new CleanupTask.CleanupWarnTask(plugin)
                    .runTaskLater(plugin, (getGlobalTaskInterval() - getWarningTiming()) * 20)
                    .getTaskId();

        this.globalTaskId = new CleanupTask(plugin, false)
                .runTaskTimer(plugin, 0, getGlobalTaskInterval() * 20)
                .getTaskId();
    }

    private void loadProtectedTypes() {
        if (protectedTypes == null) {
            protectedTypes = new HashSet<>();
        }

        for (String s : plugin.getConfig().getStringList("protectedTypes")) {
            Material m = Material.getMaterial(s);

            if (m == null) {
                Bukkit.getLogger().warning(String.format("Unknown Material \"%s\" for protected type", s));
            } else {
                protectedTypes.add(m);
            }
        }

        Bukkit.getLogger().info(String.format("Loaded %d protected types", getProtectedTypes().size()));
    }

    protected void load() {
        if (isGlobalTaskEnabled())
            scheduleGlobalTask();

        loadProtectedTypes();
    }

    protected void unload() {
        BukkitScheduler scheduler = Bukkit.getScheduler();

        if (warningTaskId > 0) {
            scheduler.cancelTask(warningTaskId);
        }

        if (globalTaskId > 0 ) {
            scheduler.cancelTask(globalTaskId);
        }

        protectedTypes.clear();
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
        return getGlobalSection().getLong("interval", 60);
    }

    public boolean isWarningGlobalTaskEnabled() {
        return getGlobalSection().getBoolean("warn", true);
    }

    public long getWarningTiming() {
        return getGlobalSection().getLong("warnBefore", 20);
    }

    public List<Material> getProtectedTypesList() {
        FileConfiguration config = getPlugin().getConfig();

        List<String> strList = config.getStringList("protectedTypes");
        List<Material> protectedTypes = new ArrayList<>();

        for (String s : strList) {
            protectedTypes.add(Material.getMaterial(s));
        }

        return protectedTypes;
    }

    public void addProtectedType(CommandSender sender, Material material) {
        if (getProtectedTypes().contains(material)) {
            sender.sendMessage(Utils.color(String.format(" &4&lX &cMaterial &4\"%s\" &cis already protected", material.name())));

            return;
        }

        // TODO Add protected type to config and refresh HashSet

        sender.sendMessage(Utils.color(String.format("&6Material &c\"%s\" &6will now be protected from being cleared", material.name())));
    }

    public void removeProtectedType(CommandSender sender, Material material) {
        if (!getProtectedTypes().contains(material)) {
            sender.sendMessage(Utils.color(String.format(" &4&lX &cMaterial &4\"%s\" &cis not protected", material.name())));

            return;
        }

        // TODO Remove protected type from config and refresh HashSet

        sender.sendMessage(Utils.color(String.format("&6Material &c\"%s\" &6will no longer be protected from being cleared", material.name())));
    }
}
