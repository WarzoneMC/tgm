package network.warzone.tgm.user;

import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import network.warzone.tgm.util.Ranks;
import network.warzone.warzoneapi.models.Rank;
import network.warzone.warzoneapi.models.UserProfile;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by luke on 4/27/17.Levels edited by PandaChan on 8/1/18.
 */
public class PlayerContext {
    @Getter private Player player;
    @Getter private UserProfile userProfile;

    public PlayerContext(Player player, UserProfile userProfile) {
        this.player = player;
        this.userProfile = userProfile;
    }

    public String getLevelString() {
        int level = userProfile.getLevel();

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
        } else {
            return ChatColor.GREEN + "[" + level + "]";
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
