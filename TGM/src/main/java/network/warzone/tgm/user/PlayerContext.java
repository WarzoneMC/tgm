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
    private UserProfile userProfile;

    public PlayerContext(Player player, UserProfile userProfile) {
        this.player = player;
        this.userProfile = userProfile;
    }

    public UserProfile getUserProfile() {
        return getUserProfile(false);
    }

    public UserProfile getUserProfile(boolean original) {
        if (hasNickedStats() && isNicked() && !original) {
            return TGM.get().getNickManager().getStats().get(player.getUniqueId());
        } else {
            return userProfile;
        }
    }

    public String getDisplayName() {
        return TGM.get().getNickManager().getNickNames().getOrDefault(player.getUniqueId(), player.getName());
    }

    public String getOriginalName() {
        return TGM.get().getNickManager().getOriginalNames().getOrDefault(player.getUniqueId(), player.getName());
    }

    public boolean hasNickedStats() {
        return TGM.get().getNickManager().getStats().containsKey(player.getUniqueId());
    }

    public boolean isNicked() {
        return TGM.get().getNickManager().getNickNames().containsKey(player.getUniqueId());
    }

    public String getLevelString() {
        return getLevelString(false);
    }

    public String getLevelString(boolean original) {
        int level = getUserProfile(original).getLevel();

        if (level < 10) {
            return ChatColor.of("#BBBAD3") + "[" + level + "]";
        }
        else if (level < 20) {
            return ChatColor.of("#B1AAFF") + "[" + level + "]";
        }
        else if (level < 30) {
            return ChatColor.of("#AAC1FF") + "[" + level + "]";
        }
        else if (level < 40) {
            return ChatColor.of("#AAE1FF") + "[" + level + "]";
        }
        else if (level < 50) {
            return ChatColor.of("#AAFFFF") + "[" + level + "]";
        }
        else if (level < 60) {
            return ChatColor.of("#96FFBC) + "[" + level + "]";
        }
        else if (level < 70 {
            return ChatColor.of("#87FF87) + "[" + level + "]";
        }
        else if (level < 80 {
            return ChatColor.of("#D9FFAA) + "[" + level + "]";
        }
        else if (level < 90 {
            return ChatColor.of("#FFFFAA) + "[" + level + "]";
        }
        else if (level < 100
            return ChatColor.of("#FFD9AA) + "[" + level + "]";
        }
        else if (level < 120
            return ChatColor.of("#FFFFAA) + "[" + level + "]";
        }
        else if (level < 140
            return ChatColor.of("#FFAAC9) + "[" + level + "]";
        }
        else if (level < 160
            return ChatColor.of("#FF96DA) + "[" + level + "]";
        }
        else if (level < 180
            return ChatColor.of("#FFF66FF) + "[" + level + "]";
        }
        else if (level < 200
            return ChatColor.of("#E266FF) + "[" + level + "]";
        }
        else if (level < 220
            return ChatColor.of("#C966FF) + "[" + level + "]";                  
        } else {
            return ChatColor.of("#E266FF) + "[" + level + "]";
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
