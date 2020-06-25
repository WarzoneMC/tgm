package network.warzone.tgm.modules.koth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.modules.controlpoint.ControlPointDefinition;
import network.warzone.tgm.modules.controlpoint.ControlPointService;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.user.PlayerContext;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;

@AllArgsConstructor @Getter
public class KOTHControlPointService implements ControlPointService {

    private KOTHModule kothModule;
    private Match match;
    private ControlPointDefinition definition;

    @Override
    public void holding(MatchTeam matchTeam) {
        kothModule.incrementPoints(matchTeam, definition.getPointsPerTick());
    }

    @Override
    public void capturing(MatchTeam matchTeam, int progress, int maxProgress, boolean upward) {
        kothModule.updateScoreboardControlPointLine(definition);
    }

    @Override
    public void captured(MatchTeam matchTeam) {
        Bukkit.broadcastMessage(matchTeam.getColor() + ChatColor.BOLD.toString() + matchTeam.getAlias() + ChatColor.WHITE +
                " took control of " + ChatColor.AQUA + ChatColor.BOLD.toString() + definition.getName());

        kothModule.incrementPoints(matchTeam, definition.getPointsPerTick());
        kothModule.updateScoreboardControlPointLine(definition);

        for (MatchTeam team : match.getModule(TeamManagerModule.class).getTeams()) {
            for (PlayerContext playerContext : team.getMembers()) {
                if (team.equals(matchTeam) || team.isSpectator()) {
                    playerContext.getPlayer().playSound(playerContext.getPlayer().getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 0.7f, 2f);
                } else {
                    playerContext.getPlayer().playSound(playerContext.getPlayer().getLocation(), Sound.ENTITY_BLAZE_DEATH, 0.8f, 0.8f);
                }
            }
        }
    }

    @Override
    public void lost(MatchTeam matchTeam) {
        kothModule.updateScoreboardControlPointLine(definition);
        Bukkit.broadcastMessage(matchTeam.getColor() + ChatColor.BOLD.toString() + matchTeam.getAlias() + ChatColor.WHITE +
                " lost control of " + ChatColor.AQUA + ChatColor.BOLD.toString() + definition.getName());
    }
}
