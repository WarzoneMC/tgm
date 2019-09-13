package network.warzone.tgm.modules.vote;

import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.match.MatchStatus;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashSet;
import java.util.Set;

public class VoteSkipModule extends MatchModule implements Listener {
    private Set<Player> skipVoters = new HashSet<>();
    private Match match;

    @Override
    public void load(Match match) {
        this.match = match;
    }

    public boolean addVote(Player player) {
        if(skipVoters.contains(player)) return false;
        else {
            skipVoters.add(player);
            checkVotes();
            return true;
        }
    }

    public int stillNeeds() {
        return Math.max(0, ((int) Math.ceil(Bukkit.getOnlinePlayers().size() * 0.75)) - skipVoters.size());
    }

    private void checkVotes() {
        if(match.getMatchStatus() != MatchStatus.MID)
        int stillNeeds = stillNeeds();
        if (stillNeeds == 0) {
            TGM.get().getMatchManager().endMatch(null);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        skipVoters.remove(event.getPlayer());
    }
}
