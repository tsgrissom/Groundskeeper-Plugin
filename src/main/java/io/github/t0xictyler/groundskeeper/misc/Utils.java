package io.github.t0xictyler.groundskeeper.misc;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static List<String> color(String... strings) {
        List<String> l = new ArrayList<>();

        for (String s : strings) {
            l.add(color(s));
        }

        return l;
    }
}
