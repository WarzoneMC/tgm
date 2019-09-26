package network.warzone.tgm.modules.stats;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor @Getter
public class PlayerStatsCache {
    private int kills;
    private int deaths;
    private int xp;
}
