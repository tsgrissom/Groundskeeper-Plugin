package io.github.t0xictyler.groundskeeper.command;

import io.github.t0xictyler.groundskeeper.GroundskeeperController;
import io.github.t0xictyler.groundskeeper.GroundskeeperPlugin;
import io.github.t0xictyler.groundskeeper.misc.Utils;
import io.github.t0xictyler.groundskeeper.task.CleanupTask;
import io.github.t0xictyler.groundskeeper.task.CleanupWorldTask;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class GroundskeeperCommand implements TabExecutor {

    @NonNull
    private final GroundskeeperPlugin plugin;

    public GroundskeeperController getController() {
        return plugin.getController();
    }

    public List<String> getHelp() {
        return Utils.color(
                " &8&m----------------------------------------",
                "&8| &eGroundskeeper Help",
                " &8&m----------------------------------------",
                " &6> &7/gk ? &8- &3View this help info",
                " &6> &7/gk reload &8- &3Reload the plugin",
                " &6> &7/gk version &8- &3View plugin version",
                " &6> &7/gk force &8- &3Force the ground to be cleared",
                "   &9-a &8or &9--all",
                "     &7Force all worlds to be cleared",
                "   &9-cp &8or &9--clear-protected",
                "     &7Bypass protected types",
                " &6> &7/gk global &8- &3Modify global task settings",
                " &6> &7/gk protected &8- &3List protected materials",
                " &6> &7/gk protect <material> &8- &3Protect a material",
                " &6> &7/gk unprotect <material> &8- &3Unprotect a material"
        );
    }

    private boolean hasFlag(String[] args, String shortArg, String longArg) {
        for (String a : args) {
            if (a.equalsIgnoreCase(String.format("-%s", shortArg)) || a.equalsIgnoreCase(String.format("--%s", longArg)))
                return true;
        }

        return false;
    }

    private void performCommand(CommandSender sender, String cmd) {
        if (sender instanceof Player) {
            ((Player) sender).performCommand(cmd);
        } else if (sender instanceof ConsoleCommandSender) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("groundskeeper.command")) {
            sender.sendMessage(Utils.color("&cYou are not allowed to do that!"));

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
        } else if (Utils.equalsAny(arg1, "version", "v")) {
            sender.sendMessage(Utils.color(String.format(" &6> &eGroundskeeper &bv%s &7by &bT0xicTyler", plugin.getDescription().getVersion())));
        } else if (arg1.equalsIgnoreCase("force")) {
            boolean clearProtected = hasFlag(args, "cp", "clear-protected");

            if (sender instanceof Player) {
                Player p = (Player) sender;

                if (hasFlag(args, "a", "all")) {
                    sender.sendMessage(Utils.color("&6Forcing all worlds to be cleared in &c3 seconds&6..."));
                    new CleanupTask(plugin, clearProtected).runTaskLater(plugin, 60);
                } else {
                    World w = p.getWorld();

                    sender.sendMessage(Utils.color(String.format("&6Forcing world &c\"%s\" &6to be cleared in &c3 seconds&6...", w.getName())));
                    new CleanupWorldTask(plugin, w, clearProtected).runTaskLater(plugin, 60);
                }
            } else {
                sender.sendMessage("Forcing all worlds to be cleared in 3 seconds...");
                new CleanupTask(plugin, clearProtected).runTaskLater(plugin, 60);
            }
        } else if (arg1.equalsIgnoreCase("protected")) {
            Set<Material> protectedTypes = getController().getProtectedTypes();

            for (Material m : getController().getProtectedTypes()) {
                sender.sendMessage(Utils.color(String.format(" &6> &e%s", m.name())));
            }

            sender.sendMessage(Utils.color(String.format("&eGroundskeeper &6is protecting &c%d &6materials from being cleared", protectedTypes.size())));

            if (getController().isIntegratedWithMagic()) {
                sender.sendMessage("&dGroundskeeper is protecting Magic wands");
            }
        } else if (arg1.equalsIgnoreCase("protect")) {
            if (args.length == 1) {
                sender.sendMessage(Utils.color(" &4&lX &cPlease specify a material to protect"));
            } else {
                String arg2 = args[1];
                Material m = Material.getMaterial(arg2);

                if (m == null) {
                    sender.sendMessage(Utils.color(String.format(" &4&lX &cUnknown material &4\"%s\"", arg2)));
                } else {
                    getController().addProtectedType(sender, m);
                }
            }
        } else if (arg1.equalsIgnoreCase("unprotect")) {
            if (args.length == 1) {
                sender.sendMessage(Utils.color(" &4&lX &cPlease specify a material to unprotect"));
            } else {
                String arg2 = args[1];
                Material m = Material.getMaterial(arg2);

                if (m == null) {
                    sender.sendMessage(Utils.color(String.format(" &4&lX &cUnknown material &4\"%s\"", arg2)));
                } else {
                    getController().removeProtectedType(sender, m);
                }
            }
        } else if (arg1.equalsIgnoreCase("global") || arg1.equalsIgnoreCase("g")) {
            if (args.length == 1) {
                GroundskeeperController controller = getController();
                String enabled = controller.isGlobalTaskEnabled() ? Utils.color("&aYes") : Utils.color("&cNo");
                String interval = Utils.color(String.format("&b%ds", controller.getGlobalTaskInterval()));
                List<String> global = Utils.color(
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
                        sender.sendMessage(Utils.color(" &4&lX &cUsage&8: &e/gk global interval <time in seconds>"));
                    } else {
                        try {
                            int i = Integer.parseInt(args[2]);

                            FileConfiguration conf = plugin.getConfig();

                            conf.set("global.interval", i);

                            plugin.saveConfig();

                            getController().cancelTasks();
                            getController().scheduleGlobalTask();

                            sender.sendMessage(Utils.color(String.format("&eGroundskeeper &6global task will now repeat every &c%ds&6.", i)));
                        } catch (NumberFormatException ignored) {
                            sender.sendMessage(Utils.color(" &4&lX &cPlease enter a valid time in seconds."));
                        }
                    }
                } else if (arg2.equalsIgnoreCase("toggle")) {
                    if (getController().isGlobalTaskEnabled()) {
                        performCommand(sender, "gk g disable");
                    } else {
                        performCommand(sender, "gk g enable");
                    }
                } else if (arg2.equalsIgnoreCase("enable")) {
                    if (getController().isGlobalTaskEnabled()) {
                        sender.sendMessage(Utils.color(" &4&lX &cGlobal task is already enabled"));
                        return true;
                    }

                    FileConfiguration conf = plugin.getConfig();

                    conf.set("global.enabled", true);

                    plugin.saveConfig();

                    getController().scheduleGlobalTask();
                    sender.sendMessage(Utils.color(String.format("&eGroundskeeper &6global task has been &aenabled&6. Will repeat every &c%ds&6.", getController().getGlobalTaskInterval())));
                } else if (arg2.equalsIgnoreCase("disable")) {
                    if (!getController().isGlobalTaskEnabled()) {
                        sender.sendMessage(Utils.color(" &4&lX &cGlobal task is already disabled"));
                        return true;
                    }

                    FileConfiguration conf = plugin.getConfig();

                    conf.set("global.enabled", false);

                    plugin.saveConfig();

                    getController().cancelTasks();
                    sender.sendMessage(Utils.color("&eGroundskeeper &6global task has been &cdisabled&6."));
                }
            }
        } else {
            sender.sendMessage(Utils.color(" &4&lX &cUnknown sub-command! Do &e/gk ?&c for help."));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> tab = new ArrayList<>();
        List<String> subs = Arrays.asList("help", "version", "force", "reload", "global", "protected", "protect", "unprotect");
        List<String> globalSubs = Arrays.asList("interval", "toggle");

        // TODO Finish tab completion

        if (args.length == 0) {
            tab.addAll(subs);
        } else if (args.length == 1) {
            String arg1 = args[0];

            for (String s : subs) {
                if (StringUtils.startsWithIgnoreCase(s, arg1)) {
                    tab.add(s);
                }
            }
        }

        return tab;
    }
}
