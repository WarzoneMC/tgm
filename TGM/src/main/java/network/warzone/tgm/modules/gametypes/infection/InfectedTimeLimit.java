package network.warzone.tgm.modules.gametypes.infection;

import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.match.MatchStatus;
import network.warzone.tgm.modules.team.TeamManagerModule;
import org.bukkit.Bukkit;

/**
 * Created by Draem on 8/5/2017.
 */
public class InfectedTimeLimit extends MatchModule {

    private Match match;
    private TeamManagerModule teamManager;

    @Override
    public void load(Match match) {
        this.match = match;
        this.teamManager = match.getModule(TeamManagerModule.class);
    }

    public void startCountdown(int length) {
        Bukkit.getScheduler().runTaskLater(TGM.get(), () -> {
            if (match.getMatchStatus().equals(MatchStatus.MID) && teamManager.getTeamById("humans").getMembers().size() != 0) {
                TGM.get().getMatchManager().endMatch(teamManager.getTeamById("humans"));
            }
        }, length * 60 * 20);
    }

}
