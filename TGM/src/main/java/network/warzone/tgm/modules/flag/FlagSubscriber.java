package network.warzone.tgm.modules.flag;

import org.bukkit.entity.Player;


public interface FlagSubscriber {
    void pickup(MatchFlag flag, Player stealer);
    void drop(MatchFlag flag, Player stealer, Player attacker);
    void capture(MatchFlag flag, Player capturer);
}
