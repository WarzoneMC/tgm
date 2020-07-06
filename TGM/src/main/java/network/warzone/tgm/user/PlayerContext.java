package network.warzone.tgm.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import network.warzone.tgm.TGM;
import network.warzone.tgm.util.Plugins;
import network.warzone.tgm.util.Ranks;
import network.warzone.tgm.util.ChatUtil.ColorFallbackComponent;
import network.warzone.warzoneapi.models.Rank;
import network.warzone.warzoneapi.models.UserProfile;
import protocolsupport.api.ProtocolVersion;
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
        levels.add(new PlayerLevel((lvl) -> lvl < 10, ChatColor.of("#BBBAD3"), ChatColor.GRAY));
        levels.add(new PlayerLevel((lvl) -> lvl < 20, ChatColor.of("#AAC1FF"), ChatColor.DARK_AQUA));
        levels.add(new PlayerLevel((lvl) -> lvl < 30, ChatColor.of("#B1AAFF"), ChatColor.BLUE));
        levels.add(new PlayerLevel((lvl) -> lvl < 40, ChatColor.of("#AAE1FF"), ChatColor.LIGHT_PURPLE));
        levels.add(new PlayerLevel((lvl) -> lvl < 50, ChatColor.of("#AAFFFF"), ChatColor.AQUA));
        levels.add(new PlayerLevel((lvl) -> lvl < 60, ChatColor.of("#96FFBC"), ChatColor.DARK_PURPLE));
        levels.add(new PlayerLevel((lvl) -> lvl < 70, ChatColor.of("#ABFFA5"), ChatColor.GREEN));
        levels.add(new PlayerLevel((lvl) -> lvl < 80, ChatColor.of("#D9FFAA"), ChatColor.DARK_RED));
        levels.add(new PlayerLevel((lvl) -> lvl < 90, ChatColor.of("#FFFFAA"), ChatColor.YELLOW));
        levels.add(new PlayerLevel((lvl) -> lvl < 100, ChatColor.of("#FFD9AA"), ChatColor.RED));
        levels.add(new PlayerLevel((lvl) -> lvl < 120, ChatColor.of("#FFAAAA"), ChatColor.GOLD));
        levels.add(new PlayerLevel((lvl) -> lvl < 140, ChatColor.of("#FFAAC9"), ChatColor.YELLOW));
        levels.add(new PlayerLevel((lvl) -> lvl < 160, ChatColor.of("#FF96DA"), ChatColor.GREEN));
        levels.add(new PlayerLevel((lvl) -> lvl < 180, ChatColor.of("#FF66FF"), ChatColor.YELLOW));
        levels.add(new PlayerLevel((lvl) -> lvl < 200, ChatColor.of("#E266FF"), ChatColor.LIGHT_PURPLE));
        levels.add(new PlayerLevel((lvl) -> lvl < 220, ChatColor.of("#C966FF"), ChatColor.DARK_PURPLE));
        // fallback
        levels.add(new PlayerLevel((lvl) -> true, ChatColor.of("#9E66FF"), ChatColor.DARK_GREEN));
    }

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

    public ColorFallbackComponent getLevelString() {
        return getLevelString(false);
    }

    public ColorFallbackComponent getLevelString(boolean original) {
        int level = getUserProfile(original).getLevel();
        for (PlayerLevel levelEntry : levels) {
            if (levelEntry.check.test(level)) {
                return new ColorFallbackComponent(
                        "" + levelEntry.levelColor + "[" + level + "]" ,
                        "" + levelEntry.fallbackLevelColor + "[" + level + "]" );
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
        private final ChatColor fallbackLevelColor;

        public PlayerLevel(Predicate<Integer> check, ChatColor levelColor) {
            this(check, levelColor, ChatColor.GRAY);
        }
    }
}
