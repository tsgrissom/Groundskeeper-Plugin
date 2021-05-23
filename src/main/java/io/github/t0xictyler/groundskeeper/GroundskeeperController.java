package io.github.t0xictyler.groundskeeper;

import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import io.github.t0xictyler.groundskeeper.misc.Utils;
import io.github.t0xictyler.groundskeeper.task.CleanupTask;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class GroundskeeperController {

    @NonNull @Getter
    private final GroundskeeperPlugin plugin;
    @Getter
    private MagicAPI magicAPI;

    private int warningTaskId, globalTaskId;

    @Getter
    private Set<Material> protectedTypes;

    protected GroundskeeperController(GroundskeeperPlugin plugin) {
        this.plugin = plugin;
        this.protectedTypes = new HashSet<>();

        load();
    }

    public void scheduleGlobalTask() {
        Bukkit.getLogger().info("Scheduling Groundskeeper global task...");

        if (isWarningGlobalTaskEnabled())
            this.warningTaskId = new CleanupTask.CleanupWarnTask(plugin)
                    .runTaskTimer(plugin, (getGlobalTaskInterval() - getWarningTiming()) * 20, (getGlobalTaskInterval() - getWarningTiming()) * 20)
                    .getTaskId();

        this.globalTaskId = new CleanupTask(plugin, false)
                .runTaskTimer(plugin, getGlobalTaskInterval() * 20, getGlobalTaskInterval() * 20)
                .getTaskId();
    }

    public void cancelTasks() {
        BukkitScheduler scheduler = Bukkit.getScheduler();

        if (warningTaskId > 0)
            scheduler.cancelTask(warningTaskId);

        if (globalTaskId > 0 )
            scheduler.cancelTask(globalTaskId);
    }

    private void loadProtectedTypes() {
        if (protectedTypes == null)
            protectedTypes = new HashSet<>();

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

    private void integrateWithMagic() {
        PluginManager pm = Bukkit.getPluginManager();
        boolean magicPresent = pm.isPluginEnabled("Magic");

        if (shouldIntegrateWithMagic()) {
            if (magicPresent) {
                this.magicAPI = (MagicAPI) pm.getPlugin("Magic");
                Bukkit.getLogger().info("Integrated with Magic plugin");
            } else {
                Bukkit.getLogger().info("Magic not found, skipping integration");
            }
        } else {
            if (magicPresent) {
                Bukkit.getLogger().info("Magic is present but the integration is disabled");
            } else {
                Bukkit.getLogger().info("Magic integration disabled");
            }
        }
    }

    protected void load() {
        integrateWithMagic();
        loadProtectedTypes();

        if (isGlobalTaskEnabled())
            scheduleGlobalTask();
    }

    protected void unload() {
        cancelTasks();

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
    private boolean shouldIntegrateWithMagic() {
        return getPlugin().getConfig().getBoolean("integrations.magic", true);
    }

    public boolean isIntegratedWithMagic() {
        return shouldIntegrateWithMagic() && this.magicAPI != null;
    }

    public String getMessage(String key) {
        ConfigurationSection cs = getPlugin().getConfig().getConfigurationSection("messages");

        if (cs == null)
            return Utils.color("&cGroundskeeper is improperly configured: No messages section.");

        if (!cs.isSet(key))
            return Utils.color(String.format("&cGroundskeeper is improperly configured: No message found for key &4\"%s\"", key));

        return Utils.color(cs.getString(key));
    }

    public String getMessage(String key, Map<String, String> replace) {
        String m = getMessage(key);

        for (Map.Entry<String, String> entry : replace.entrySet())
            m = m.replace(entry.getKey(), entry.getValue());

        return m;
    }

    public void addProtectedType(CommandSender sender, Material material) {
        if (getProtectedTypes().contains(material)) {
            sender.sendMessage(Utils.color(String.format(" &4&lX &cMaterial &4\"%s\" &cis already protected", material.name())));

            return;
        }

        getProtectedTypes().add(material);

        FileConfiguration conf = getPlugin().getConfig();

        conf.set("protectedTypes", getProtectedTypes().stream().map(Enum::name).collect(Collectors.toList()));
        getPlugin().saveConfig();
        getProtectedTypes().clear();
        loadProtectedTypes();

        sender.sendMessage(Utils.color(String.format("&6Material &c\"%s\" &6will now be protected from being cleared", material.name())));
    }

    public void removeProtectedType(CommandSender sender, Material material) {
        if (!getProtectedTypes().contains(material)) {
            sender.sendMessage(Utils.color(String.format(" &4&lX &cMaterial &4\"%s\" &cis not protected", material.name())));

            return;
        }

        getProtectedTypes().remove(material);

        FileConfiguration conf = getPlugin().getConfig();

        conf.set("protectedTypes", getProtectedTypes().stream().map(Enum::name).collect(Collectors.toList()));
        getPlugin().saveConfig();
        getProtectedTypes().clear();
        loadProtectedTypes();

        sender.sendMessage(Utils.color(String.format("&6Material &c\"%s\" &6will no longer be protected from being cleared", material.name())));
    }
}
