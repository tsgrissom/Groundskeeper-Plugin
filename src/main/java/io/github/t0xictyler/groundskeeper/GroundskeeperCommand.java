package io.github.t0xictyler.groundskeeper;

import io.github.t0xictyler.groundskeeper.misc.Utils;
import io.github.t0xictyler.groundskeeper.task.CleanupTask;
import io.github.t0xictyler.groundskeeper.task.CleanupWorldTask;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.t0xictyler.groundskeeper.misc.Utils.color;
import static java.lang.String.format;

@RequiredArgsConstructor
public class GroundskeeperCommand implements TabExecutor {

    @NonNull
    private final GroundskeeperPlugin plugin;

    public GroundskeeperController getController() {
        return plugin.getController();
    }

    public List<String> getHelp() {
        String debugBoolStr = getController().isDebugging() ?
                "&aEnabled" : "&cDisabled";

        return color(
                " &8&m----------------------------------------",
                "&8| &eGroundskeeper &7Command Help",
                " &8&m----------------------------------------",
                format(" &6> &7/gk debug &8- &3Toggle debug mode &7(%s&7)", debugBoolStr),
                " &6> &7/gk force &8- &3Force the ground to be cleared",
                "   &9-a &8or &9--all",
                "      &7Force all worlds to be cleared",
                "   &9-cp &8or &9--clear-protected",
                "      &7Bypass protected types",
                " &6> &7/gk global &8- &3Modify global task settings",
                " &6> &7/gk load &8- &3Reload the plugin",
                " &6> &7/gk protect <material> &8- &3Protect a material",
                " &6> &7/gk protected &8- &3List protected materials",
                " &6> &7/gk unprotect <material> &8- &3Unprotect a material",
                " &6> &7/gk version &8- &3View plugin version"
        );
    }

    private boolean hasFlag(String[] args, String shortArg, String longArg) {
        for (String a : args) {
            if (a.equalsIgnoreCase(format("-%s", shortArg)) || a.equalsIgnoreCase(format("--%s", longArg)))
                return true;
        }

        return false;
    }

    private void sudo(CommandSender sender, String cmd) {
        if (sender instanceof Player) {
            ((Player) sender).performCommand(cmd);
        } else if (sender instanceof ConsoleCommandSender) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!Utils.equalsAny(label, "groundskeeper", "gk"))
            return true;

        if (!sender.hasPermission("groundskeeper.command")) {
            sender.sendMessage(color("&cYou are not allowed to do that!"));

            return true;
        }

        if (args.length == 0) {
            getHelp().forEach(sender::sendMessage);

            return true;
        }

        String arg1 = args[0];

        if (Utils.equalsAny(arg1, "reload", "load")) {
            plugin.getController().reload(sender);
        } else if (Utils.equalsAny(arg1, "help", "?")) {
            getHelp().forEach(sender::sendMessage);
        } else if (Utils.equalsAny(arg1, "version", "v", "info")) {
            sender.sendMessage(color(format(" &6> &eGroundskeeper &bv%s &7by &bT0xicTyler", plugin.getDescription().getVersion())));
            sender.sendMessage(color(" &6> &bDo &a/gk &bto get started"));
        } else if (arg1.equalsIgnoreCase("force")) {
            return forceCleanup(sender, args);
        } else if (arg1.equalsIgnoreCase("protected")) {
            return listProtected(sender);
        } else if (arg1.equalsIgnoreCase("protect")) {
            return addProtected(sender, args);
        } else if (arg1.equalsIgnoreCase("unprotect")) {
            return removeProtected(sender, args);
        } else if (Utils.equalsAny(arg1, "global", "g")) {
            return global(sender, args);
        } else if (Utils.equalsAny(arg1, "debug", "debugger")) {
            return debug(sender);
        } else {
            sender.sendMessage(color(format(" &4&lX &cUnknown sub-command \"%s\"! Do &e/gk ?&c for help.", arg1)));
        }

        return true;
    }

    public boolean debug(CommandSender sender) {
        GroundskeeperPlugin plugin = getController().getPlugin();
        boolean debug = !getController().isDebugging();

        plugin.getConfig().set("debug", debug);
        plugin.saveConfig();

        String boolStr = debug ? color("&aenabled") : color("&cdisabled");

        sender.sendMessage(color(format("&eGroundskeeper &6debugging has been %s", boolStr)));

        return true;
    }

    public boolean global(CommandSender sender, String[] args) {
        if (args.length == 1) {
            GroundskeeperController controller = getController();
            String enabled = controller.isGlobalTaskEnabled() ? color("&aYes") : color("&cNo");
            String interval = color(format("&b%ds", controller.getGlobalTaskInterval()));
            List<String> global = color(
                    "",
                    " &8&m----------------------------------------",
                    "&8| &eGroundskeeper &7Global Task Settings",
                    " &8&m----------------------------------------",
                    " &6> &7Enabled&8: " + enabled,
                    " &6> &7Interval&8: " + interval
            );

            global.forEach(sender::sendMessage);
        } else {
            String arg2 = args[1];

            if (arg2.equalsIgnoreCase("interval")) {
                if (args.length == 2) {
                    sender.sendMessage(color(" &4&lX &cUsage&8: &e/gk global interval <time in seconds>"));
                } else {
                    try {
                        int i = Integer.parseInt(args[2]);

                        plugin.getConfig().set("global.interval", i);
                        plugin.saveConfig();

                        getController().cancelTasks();
                        getController().scheduleGlobalTask();

                        sender.sendMessage(color(format("&eGroundskeeper &6global task will now repeat every &c%ds&6.", i)));
                    } catch (NumberFormatException ignored) {
                        sender.sendMessage(color(" &4&lX &cPlease enter a valid time in seconds."));
                    }
                }
            } else if (arg2.equalsIgnoreCase("toggle")) {
                if (getController().isGlobalTaskEnabled()) {
                    sudo(sender, "gk g disable");
                } else {
                    sudo(sender, "gk g enable");
                }
            } else if (arg2.equalsIgnoreCase("enable")) {
                if (getController().isGlobalTaskEnabled()) {
                    sender.sendMessage(color(" &4&lX &cGlobal task is already enabled"));
                    return true;
                }

                FileConfiguration conf = plugin.getConfig();

                conf.set("global.enabled", true);

                plugin.saveConfig();

                getController().scheduleGlobalTask();
                sender.sendMessage(color(format("&eGroundskeeper &6global task has been &aenabled&6. Will repeat every &c%ds&6.", getController().getGlobalTaskInterval())));
            } else if (arg2.equalsIgnoreCase("disable")) {
                if (!getController().isGlobalTaskEnabled()) {
                    sender.sendMessage(color(" &4&lX &cGlobal task is already disabled"));
                    return true;
                }

                FileConfiguration conf = plugin.getConfig();

                conf.set("global.enabled", false);

                plugin.saveConfig();

                getController().cancelTasks();
                sender.sendMessage(color("&eGroundskeeper &6global task has been &cdisabled&6."));
            }
        }

        return true;
    }

    public boolean forceCleanup(CommandSender sender, String[] args) {
        boolean clearProtected = hasFlag(args, "cp", "clear-protected");

        if (sender instanceof Player) {
            Player p = (Player) sender;

            if (hasFlag(args, "a", "all")) {
                sender.sendMessage(color("&6Forcing all worlds to be cleared in &c3 seconds&6..."));
                new CleanupTask(plugin, clearProtected).runTaskLater(plugin, 60);
            } else {
                World w = p.getWorld();

                sender.sendMessage(color(format("&6Forcing world &c\"%s\" &6to be cleared in &c3 seconds&6...", w.getName())));
                new CleanupWorldTask(plugin, w, clearProtected).runTaskLater(plugin, 60);
            }
        } else {
            sender.sendMessage("Forcing all worlds to be cleared in 3 seconds...");
            new CleanupTask(plugin, clearProtected).runTaskLater(plugin, 60);
        }

        return true;
    }

    public boolean addProtected(CommandSender sender, String[] args) {
        if (args.length == 1) {
            sender.sendMessage(color(" &4&lX &cPlease specify a material to protect"));
        } else {
            String arg2 = args[1];
            Material m = Material.getMaterial(arg2);

            if (m == null) {
                sender.sendMessage(color(format(" &4&lX &cUnknown material &4\"%s\"", arg2)));
            } else {
                getController().addProtectedType(sender, m);
            }
        }

        return true;
    }

    public boolean removeProtected(CommandSender sender, String[] args) {
        if (args.length == 1) {
            sender.sendMessage(color(" &4&lX &cPlease specify a material to unprotect"));
        } else {
            String arg2 = args[1];
            Material m = Material.getMaterial(arg2);

            if (m == null) {
                sender.sendMessage(color(format(" &4&lX &cUnknown material &4\"%s\"", arg2)));
            } else {
                getController().removeProtectedType(sender, m);
            }
        }

        return true;
    }

    public boolean listProtected(CommandSender sender) {
        Set<Material> protectedTypes = getController().getProtectedTypes();

        for (Material m : getController().getProtectedTypes()) {
            sender.sendMessage(color(format(" &6> &e%s", m.name())));
        }

        sender.sendMessage(color(format("&eGroundskeeper &6is protecting &c%d &6materials from being cleared", protectedTypes.size())));

        if (getController().isIntegratedWithMagic()) {
            sender.sendMessage(color("&dThe Magic integration is enabled. Groundskeeper will protect Magic wands from being cleared."));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> tab = new ArrayList<>();

        if (!Utils.equalsAny(label, "groundskeeper", "gk"))
            return tab;

        List<String> subs = Arrays.asList("help", "version", "debug", "force", "reload", "global", "protected", "protect", "unprotect");

        if (args.length == 0) {
            tab.addAll(subs);
        } else if (args.length == 1) {
            String arg1 = args[0];

            for (String s : subs) {
                if (StringUtils.startsWithIgnoreCase(s, arg1)) {
                    tab.add(s);
                }
            }
        } else if (args.length == 2) {
            String arg1 = args[0];
            String arg2 = args[1];

            if (arg1.equalsIgnoreCase("global")) {
                for (String s : Arrays.asList("interval", "toggle", "enable", "disable")) {
                    if (StringUtils.startsWithIgnoreCase(s, arg2)) {
                        tab.add(s);
                    }
                }
            } else if (arg1.equalsIgnoreCase("protect")) {
                for (String s : Arrays.stream(Material.values()).map(Enum::name).collect(Collectors.toList())) {
                    if (StringUtils.startsWithIgnoreCase(s, arg2)) {
                        tab.add(s);
                    }
                }
            } else if (arg1.equalsIgnoreCase("unprotect")) {
                for (String s : getController().getProtectedTypes().stream().map(Enum::name).collect(Collectors.toList())) {
                    if (StringUtils.startsWithIgnoreCase(s, arg2)) {
                        tab.add(s);
                    }
                }
            }
        }

        return tab;
    }
}
