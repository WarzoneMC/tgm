package network.warzone.tgm.modules.flag;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import java.util.List;

public interface FlagSubscriber {
    void pickup(MatchFlag flag, Player stealer, List<PotionEffect> effects);
    void drop(MatchFlag flag, Player stealer, Player attacker, List<PotionEffect> effects);
    void capture(MatchFlag flag, Player capturer, List<PotionEffect> effects);
}
