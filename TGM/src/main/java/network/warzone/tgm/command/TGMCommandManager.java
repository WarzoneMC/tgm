package network.warzone.tgm.command;

import cl.bgmp.bukkit.util.BukkitCommandsManager;
import org.bukkit.command.CommandSender;

/**
 * Created by Jorge on 02/03/2021
 */
public class TGMCommandManager extends BukkitCommandsManager {

    @Override
    public boolean hasPermission(CommandSender sender, String perm) {
        return sender.isOp() || super.hasPermission(sender, perm);
    }

}
