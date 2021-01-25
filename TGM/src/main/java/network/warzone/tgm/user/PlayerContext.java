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
package network.warzone.tgm.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import network.warzone.tgm.TGM;
import network.warzone.tgm.util.Ranks;
import network.warzone.warzoneapi.models.Rank;
import network.warzone.warzoneapi.models.UserProfile;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Created by luke on 4/27/17.
 */
public class PlayerContext {
    @Getter private Player player;
    private UserProfile userProfile;
    private static final List<PlayerLevel> levels = new ArrayList<>();

    static {


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

    public PlayerContext(Player player, UserProfile userProfile) {
        this.player = player;
        this.userProfile = userProfile;
    }

    public UserProfile getUserProfile() {
        return getUserProfile(false);
    }

    public UserProfile getUserProfile(boolean original) {
        if (isNicked() && !original) {
            return TGM.get().getNickManager().getNick(this).get().getProfile();
        } else {
            return userProfile;
        }
    }

    public String getDisplayName() {
        if (isNicked()) {
            return TGM.get().getNickManager().getNick(this).get().getName();
        }
        return player.getName();
    }

    public String getOriginalName() {
        if (isNicked()) {
            return TGM.get().getNickManager().getNick(this).get().getOriginalName();
        }
        return player.getName();
    }

    public boolean isNicked() {
        return TGM.get().getNickManager().isNicked(this);
    }

    public String getLevelString() {
        return getLevelString(false);
    }

    public String getLevelString(boolean original) {
        int level = getUserProfile(original).getLevel();
        for (PlayerLevel levelEntry : levels) {
            if (levelEntry.check.test(level)) {
                return "" + levelEntry.levelColor + "[" + level + "]"; 
            }
        }

        // will not be reached due to fallback entry
        return null;
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

    @AllArgsConstructor @Getter
    private static class PlayerLevel {
        private final Predicate<Integer> check;
        private final ChatColor levelColor;
    }
}
