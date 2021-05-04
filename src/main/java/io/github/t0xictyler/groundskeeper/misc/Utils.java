package io.github.t0xictyler.groundskeeper.misc;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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

    public static boolean equalsAny(String str, String... strings) {
        for (String s : strings) {
            if (str.equalsIgnoreCase(s))
                return true;
        }

        return false;
    }

    public static String normalizeEnumName(String name) {
        String[] split = name.split(Pattern.quote("_"));
        StringBuilder sb = new StringBuilder();

        for (String s : split) {
            sb.append(s.charAt(0)).append(s.substring(1).toLowerCase()).append(" ");
        }

        return sb.toString().trim();
    }
}
