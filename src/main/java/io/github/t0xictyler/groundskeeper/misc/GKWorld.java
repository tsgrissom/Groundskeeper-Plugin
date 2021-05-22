package io.github.t0xictyler.groundskeeper.misc;

import io.github.t0xictyler.groundskeeper.GroundskeeperController;
import javafx.util.Pair;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public class GKWorld {

    @NonNull
    private final GroundskeeperController controller;
    @NonNull @Getter
    private final String worldName;

    public World getWorld() {
        return Bukkit.getWorld(getWorldName());
    }

    public GKWorld(GroundskeeperController controller, String worldName) {
        World w = Bukkit.getWorld(worldName);

        if (w == null) {
            throw new NullPointerException(String.format("Unknown world '%s'", worldName));
        }

        this.controller = controller;
        this.worldName = worldName;
    }

    public GKWorld(GroundskeeperController controller, World world) {
        this(controller, world.getName());
    }

    public Pair<Integer, Integer> clearGroundEntities(boolean bypassProtection) {
        int totalCount = 0, stackCount = 0;
        Set<Material> protectedTypes = controller.getProtectedTypes();

        for (Entity entity : getWorld().getEntities()) {
            if (!(entity instanceof Item)) {
                continue;
            }

            Item item = (Item) entity;
            ItemStack is = item.getItemStack();
            String name = Utils.normalizeEnumName(is.getType().name());

            if (protectedTypes.contains(is.getType()) && !bypassProtection) {
                Bukkit.getLogger().info(String.format("Protected %d %s", is.getAmount(), name));
                continue;
            }

            totalCount += is.getAmount();

            Bukkit.getLogger().info(String.format("Cleared %d %s", is.getAmount(), name));
            item.remove();
            stackCount++;
        }

        return new Pair<>(totalCount, stackCount);
    }
}
