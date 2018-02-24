package network.warzone.tgm.util;

import lombok.Getter;
import network.warzone.tgm.TGM;
import network.warzone.warzoneapi.models.Rank;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Jorge on 2/23/2018.
 */
public class Ranks {

    @Getter private static Map<UUID, PermissionAttachment> attachments = new HashMap<>();

    public static void update(Rank rank) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            TGM.get().getPlayerManager().getPlayerContext(player).updateRank(rank);
        }
    }

    public static void createAttachment(Player player) {
        attachments.put(player.getUniqueId(), player.addAttachment(TGM.get()));
    }

    public static void addPermissions(Player player, List<String> permissions) {
        permissions.stream().forEach(permission -> addPermission(player, permission));
    }

    public static void addPermission(Player player, String permission) {
        if (attachments.containsKey(player.getUniqueId())) {
            attachments.get(player.getUniqueId()).setPermission(permission, true);
        }
    }

    public static void removePermissions(Player player, List<String> permissions) {
        permissions.stream().forEach(permission -> removePermission(player, permission));
    }

    public static void removePermission(Player player, String permission) {
        if (attachments.containsKey(player.getUniqueId())) {
            attachments.get(player.getUniqueId()).unsetPermission(permission);
        }
    }

    public static void removeAttachment(Player player) {
        if (attachments.containsKey(player.getUniqueId())) {
            attachments.remove(player.getUniqueId());
        }
    }

}
