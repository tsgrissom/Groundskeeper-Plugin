package io.github.t0xictyler.groundskeeper.command;

import io.github.t0xictyler.groundskeeper.GroundskeeperController;
import io.github.t0xictyler.groundskeeper.GroundskeeperPlugin;
import io.github.t0xictyler.groundskeeper.misc.Utils;
import io.github.t0xictyler.groundskeeper.task.CleanupTask;
import io.github.t0xictyler.groundskeeper.task.CleanupWorldTask;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public class GroundskeeperCommand implements TabExecutor {

    @NonNull
    private final GroundskeeperPlugin plugin;

    public GroundskeeperController getController() {
        return plugin.getController();
    }

    public List<String> getHelp() {
        return Utils.color(
                "&8&m----------------------------------------",
                " &e&lGroundskeeper Help",
                "&8&m----------------------------------------",
                " &6> &7/gk ? &8- &3View this help info",
                " &6> &7/gk reload &8- &3Reload the plugin",
                " &6> &7/gk version &8- &3View plugin version",
                " &6> &7/gk force &8- &3Force the ground to be cleared"
        );
    }

    public boolean hasFlag(String[] args, String shortArg, String longArg) {
        for (String a : args) {
            if (a.equalsIgnoreCase(String.format("-%s", shortArg)) || a.equalsIgnoreCase(String.format("--%s", longArg)))
                return true;
        }

        return false;
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
            sender.sendMessage(String.format("Groundskeeper v%s", plugin.getDescription().getVersion()));
        } else if (arg1.equalsIgnoreCase("force")) {
            boolean clearProtected = hasFlag(args, "cp", "clear-protected");

            if (sender instanceof Player) {
                Player p = (Player) sender;

                if (hasFlag(args, "a", "all")) {
                    new CleanupTask(plugin, clearProtected).run();
                } else {
                    new CleanupWorldTask(plugin, p.getWorld(), clearProtected).run();
                }
            } else {
                new CleanupTask(plugin, clearProtected).run();
            }
        } else if (arg1.equalsIgnoreCase("protect")) {

        } else if (arg1.equalsIgnoreCase("unprotect")) {

        } else if (arg1.equalsIgnoreCase("global")) {
            if (args.length == 1) {
                GroundskeeperController controller = getController();
                String enabled = controller.isGlobalTaskEnabled() ? Utils.color("&aYes") : Utils.color("&cNo");
                String interval = Utils.color(String.format("&b%ds", controller.getGlobalTaskInterval()));
                List<String> global = Utils.color(
                        "",
                        " &e&lGlobal Task",
                        "&7Enabled? " + enabled,
                        "&7Interval: " + interval
                );

                global.forEach(sender::sendMessage);
            } else {
                String arg2 = args[1];

                if (arg2.equalsIgnoreCase("interval")) {
                    if (args.length == 2) {
                        sender.sendMessage(" &4&lX &cUsage&8: &e/gk global interval <time in seconds>");
                    } else {
                        try {
                            int i = Integer.parseInt(args[2]);

                            sender.sendMessage("Parsed interval " + i);
                        } catch (NumberFormatException ignored) {
                            sender.sendMessage(" &4&lX &cPlease enter a valid time in seconds.");
                        }
                    }
                } else if (arg2.equalsIgnoreCase("toggle")) {
                    sender.sendMessage("Toggle global task");
                }
            }
        } else {
            sender.sendMessage(Utils.color(" &4&lX &cUnknown sub-command! Do &e/gk ? &cfor help."));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> tab = new ArrayList<>();
        List<String> subs = Arrays.asList("help", "version", "force", "reload", "global", "protect", "unprotect");
        List<String> globalSubs = Arrays.asList("interval", "toggle");

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
