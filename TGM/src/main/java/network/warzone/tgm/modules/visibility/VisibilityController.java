package network.warzone.tgm.modules.visibility;

import org.bukkit.entity.Player;

import java.util.ArrayList;

public interface VisibilityController {
    boolean canSee(Player eyes, Player target);
    void addVanishedPlayer(Player vanisher);
    void removeVanishedPlayer(Player vanisher);
    ArrayList<Player> getVanished();
}
