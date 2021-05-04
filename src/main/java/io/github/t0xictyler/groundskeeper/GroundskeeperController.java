package io.github.t0xictyler.groundskeeper;

import io.github.t0xictyler.groundskeeper.task.CleanupTask;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GroundskeeperController {

    @NonNull @Getter
    private final GroundskeeperPlugin plugin;

    public GroundskeeperController(GroundskeeperPlugin plugin) {
        this.plugin = plugin;

        new CleanupTask(plugin).runTaskTimer(plugin, 0, getGlobalTaskInterval() * 20);
    }

    public long getGlobalTaskInterval() {
        return getPlugin().getConfig().getLong("global_task_interval", 300);
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
}
