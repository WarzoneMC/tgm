package network.warzone.tgm.map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;

/**
 * Created by luke on 4/27/17.
 */
@AllArgsConstructor @Getter
public class ParsedTeam {
    private String id;
    private String alias;
    private ChatColor teamColor;
    private GameMode teamGamemode;
    private int max;
    private int min;
    private boolean friendlyFire;
}
