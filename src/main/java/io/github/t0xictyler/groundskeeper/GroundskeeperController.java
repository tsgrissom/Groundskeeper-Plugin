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
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.*;
import java.util.stream.Collectors;

public class GroundskeeperController {

    @NonNull @Getter
    private final GroundskeeperPlugin plugin;
    @Getter
    private MagicAPI magicAPI;

    private int globalTaskId;

    @Getter
    private List<Material> protectedTypes;

    protected GroundskeeperController(GroundskeeperPlugin plugin) {
        this.plugin = plugin;
        this.protectedTypes = new ArrayList<>();

        load();
    }

    public void scheduleGlobalTask() {
        Bukkit.getLogger().info("Scheduling Groundskeeper global task...");

        this.globalTaskId = new CleanupTask(plugin, false)
                .runTaskTimer(plugin, getGlobalTaskInterval() * 20, getGlobalTaskInterval() * 20)
                .getTaskId();
    }

    public void cancelTasks() {
        BukkitScheduler scheduler = Bukkit.getScheduler();

        if (globalTaskId > 0 )
            scheduler.cancelTask(globalTaskId);
    }

    private void loadProtectedTypes() {
        if (protectedTypes == null)
            protectedTypes = new ArrayList<>();

        List<String> strList = plugin.getConfig().getStringList("protectedTypes");

        Collections.sort(strList);

        for (String s : strList) {
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

    public boolean isDebugging() {
        return getConfig().getBoolean("debug", false);
    }

    public void sendDebug(String message) {
        if (isDebugging()) {
            message = Utils.color(message);

            Bukkit.getConsoleSender().sendMessage(String.format("[GK] DEBUG: %s", message));

            for (Player p : Bukkit.getOnlinePlayers())
                if (p.hasPermission("groundskeeper.debug"))
                    p.sendMessage(Utils.color(String.format("&e&lGK &8Debug &6> &7%s", message)));
        }
    }

    private FileConfiguration getConfig() {
        return getPlugin().getConfig();
    }

    private ConfigurationSection getGlobalSection() {
        return getConfig().getConfigurationSection("global");
    }

    public boolean isGlobalTaskEnabled() {
        return getGlobalSection().getBoolean("enabled", true);
    }

    public long getGlobalTaskInterval() {
        return getGlobalSection().getLong("interval", 60);
    }

    private boolean shouldIntegrateWithMagic() {
        return getConfig().getBoolean("integrations.magic", true);
    }

    public boolean isIntegratedWithMagic() {
        return shouldIntegrateWithMagic() && this.magicAPI != null;
    }

    public String getMessage(String key) {
        ConfigurationSection cs = getConfig().getConfigurationSection("messages");

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

        getConfig().set("protectedTypes", getProtectedTypes().stream().map(Enum::name).collect(Collectors.toList()));
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

        getConfig().set("protectedTypes", getProtectedTypes().stream().map(Enum::name).collect(Collectors.toList()));
        getPlugin().saveConfig();
        getProtectedTypes().clear();
        loadProtectedTypes();

        sender.sendMessage(Utils.color(String.format("&6Material &c\"%s\" &6will no longer be protected from being cleared", material.name())));
    }
    
    public void notify(String message) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("groundskeeper.notify")) {
                p.sendMessage(message);
            }
        }

        Bukkit.getLogger().info(String.format("[GK] %s", message));
    }
}
