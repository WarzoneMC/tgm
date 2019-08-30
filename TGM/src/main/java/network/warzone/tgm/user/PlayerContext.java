package network.warzone.tgm.user;

import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import network.warzone.tgm.TGM;
import network.warzone.tgm.util.Ranks;
import network.warzone.warzoneapi.models.Rank;
import network.warzone.warzoneapi.models.UserProfile;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by luke on 4/27/17.
 */
public class PlayerContext {
    @Getter private Player player;
    @Getter private UserProfile userProfile;

    public PlayerContext(Player player, UserProfile userProfile) {
        this.player = player;
        this.userProfile = userProfile;
    }

    public String getDisplayName() {
        return TGM.get().getNickManager().nickNames.getOrDefault(player.getUniqueId(), player.getName());
    }

    public String getOriginalName() {
        return TGM.get().getNickManager().originalNames.get(player.getUniqueId());
    }

    public String getPrefix() {
        if (isNicked()) {
            Rank r = TGM.get().getNickManager().ranks.get(player.getUniqueId());
            if (r != null) {
                return r.getPrefix();
            } else {
                return null;
            }
        }
        return userProfile.getPrefix();
    }

    public int getLevel() {
        if (isNicked()) {
            return TGM.get().getNickManager().levels.getOrDefault(player.getUniqueId(), 1);
        }
        return userProfile.getLevel();
    }

    public boolean isNicked() {
        return TGM.get().getNickManager().nickNames.containsKey(player.getUniqueId());
    }

    public String getLevelString() {
        int level = getLevel();

        if (level < 10) {
            return ChatColor.GRAY + "[" + level + "]";
        }
        else if (level < 20) {
            return ChatColor.DARK_AQUA + "[" + level + "]";
        }
        else if (level < 30) {
            return ChatColor.BLUE + "[" + level + "]";
        }
        else if (level < 40) {
            return ChatColor.LIGHT_PURPLE + "[" + level + "]";
        }
        else if (level < 60) {
            return ChatColor.DARK_PURPLE + "[" + level + "]";
        }
        else if (level < 80) {
            return ChatColor.DARK_RED + "[" + level + "]";
        }
        else if (level < 100) {
            return ChatColor.RED + "[" + level + "]";
        }
        else if (level < 120) {
            return ChatColor.GOLD + "[" + level + "]";
        }
        else if (level < 140) {
            return ChatColor.YELLOW + "[" + level + "]";
        }
        else if (level < 160) {
            return ChatColor.GREEN + "[" + level + "]";
        } else {
            return ChatColor.DARK_GREEN + "[" + level + "]";
        }
    }

    public void updateRank(Rank r) {
        updateRank(r, false);
    }

    public void updateRank(Rank r, boolean forceUpdate) {
        List<String> oldPermissions = new ArrayList<>();
        boolean update = false;
        for (Rank rank : getUserProfile().getRanksLoaded()) {
            oldPermissions.addAll(rank.getPermissions());
            if (rank.getId().equals(r.getId())) {
                rank.set(r);
                update = true;
            }
        }
        if (update || forceUpdate) {
            oldPermissions.addAll(r.getPermissions());
            Ranks.removePermissions(player, oldPermissions);
            getUserProfile().getRanksLoaded().forEach(rank -> Ranks.addPermissions(player, rank.getPermissions()));
        }

    }
}
