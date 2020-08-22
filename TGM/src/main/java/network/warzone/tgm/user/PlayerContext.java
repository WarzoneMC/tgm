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
        levels.add(new PlayerLevel((lvl) -> lvl < 10, ChatColor.of("#BBBAD3")));
        levels.add(new PlayerLevel((lvl) -> lvl < 20, ChatColor.of("#AAC1FF")));
        levels.add(new PlayerLevel((lvl) -> lvl < 30, ChatColor.of("#B1AAFF")));
        levels.add(new PlayerLevel((lvl) -> lvl < 40, ChatColor.of("#AAE1FF")));
        levels.add(new PlayerLevel((lvl) -> lvl < 50, ChatColor.of("#AAFFFF")));
        levels.add(new PlayerLevel((lvl) -> lvl < 60, ChatColor.of("#96FFBC")));
        levels.add(new PlayerLevel((lvl) -> lvl < 70, ChatColor.of("#ABFFA5")));
        levels.add(new PlayerLevel((lvl) -> lvl < 80, ChatColor.of("#D9FFAA")));
        levels.add(new PlayerLevel((lvl) -> lvl < 90, ChatColor.of("#FFFFAA")));
        levels.add(new PlayerLevel((lvl) -> lvl < 100, ChatColor.of("#FFD9AA")));
        levels.add(new PlayerLevel((lvl) -> lvl < 120, ChatColor.of("#FFAAAA")));
        levels.add(new PlayerLevel((lvl) -> lvl < 140, ChatColor.of("#FFAAC9")));
        levels.add(new PlayerLevel((lvl) -> lvl < 160, ChatColor.of("#FF96DA")));
        levels.add(new PlayerLevel((lvl) -> lvl < 180, ChatColor.of("#FF66FF")));
        levels.add(new PlayerLevel((lvl) -> lvl < 200, ChatColor.of("#E266FF")));
        levels.add(new PlayerLevel((lvl) -> lvl < 220, ChatColor.of("#C966FF")));
        // fallback
        levels.add(new PlayerLevel((lvl) -> true, ChatColor.of("#9E66FF")));
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
