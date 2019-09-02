package network.warzone.tgm.modules.killstreak;

import org.bukkit.entity.Player;

public interface KillstreakAction {
    void apply(Player killer);
}
