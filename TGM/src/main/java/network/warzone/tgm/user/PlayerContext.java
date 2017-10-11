package network.warzone.tgm.user;

import network.warzone.warzoneapi.models.UserProfile;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

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

    public String getLevelString() {
        int level = userProfile.getLevel();

        if (level < 10) {
            return ChatColor.GRAY + "[" + level + "]";
        }
        else if (level < 20) {
            return ChatColor.BLUE + "[" + level + "]";
        }
        else if (level < 30) {
            return ChatColor.DARK_GREEN + "[" + level + "]";
        }
        else if (level < 40) {
            return ChatColor.GREEN + "[" + level + "]";
        } else {
            return ChatColor.RED + "[" + level + "]";
        }
    }
}
