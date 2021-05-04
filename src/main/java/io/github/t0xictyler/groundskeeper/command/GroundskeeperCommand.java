package io.github.t0xictyler.groundskeeper.command;

import io.github.t0xictyler.groundskeeper.misc.Utils;
import org.bukkit.command.*;

import java.util.List;

public class GroundskeeperCommand implements TabExecutor {

    public List<String> getHelp() {
        return Utils.color(
                "&8&m----------------------------------------",
                " &e&lGroundskeeper Help",
                "&8&m----------------------------------------"
        );
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        getHelp().forEach(sender::sendMessage);

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return null;
    }
}
