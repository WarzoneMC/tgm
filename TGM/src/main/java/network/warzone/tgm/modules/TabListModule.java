package network.warzone.tgm.modules;

import lombok.Getter;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.match.MatchStatus;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.modules.time.TimeModule;
import network.warzone.tgm.util.Strings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class TabListModule extends MatchModule implements Listener {
    @Getter protected int runnableId = -1;

    @Getter private TeamManagerModule teamManagerModule;

    @Override
    public void load(Match match) {
        teamManagerModule = match.getModule(TeamManagerModule.class);

        refreshAllTabs();

        runnableId = Bukkit.getScheduler().scheduleSyncRepeatingTask(TGM.get(), this::refreshAllTabs, 10L, 10L);
    }

    @Override
    public void unload() {
        Bukkit.getScheduler().cancelTask(runnableId);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        refreshTab(event.getPlayer());
    }

    private void refreshTab(Player player) {
        MatchStatus matchStatus = TGM.get().getMatchManager().getMatch().getMatchStatus();

        ChatColor timeColor = ChatColor.GREEN;
        if (matchStatus == MatchStatus.PRE) {
            timeColor = ChatColor.GOLD;
        } else if (matchStatus == MatchStatus.POST) {
            timeColor = ChatColor.RED;
        }

        String header = ChatColor.WHITE + ChatColor.BOLD.toString() + TGM.get().getMatchManager().getMatch().getMapContainer().getMapInfo().getGametype().toString() +
                        ChatColor.DARK_GRAY + " - " + timeColor + Strings.formatTime(TGM.get().getMatchManager().getMatch().getModule(TimeModule.class).getTimeElapsed()) +
                        ChatColor.DARK_GRAY + " - " + ChatColor.WHITE + ChatColor.BOLD.toString() + ChatColor.translateAlternateColorCodes('&', TGM.get().getConfig().getString("server.tablist-name") == null ? "&f&lWARZONE" : TGM.get().getConfig().getString("server.tablist-name"));

        String footer = "";
        for (MatchTeam matchTeam : teamManagerModule.getTeams()) {
            if (matchTeam.isSpectator()) continue;
            footer += matchTeam.getColor() + matchTeam.getAlias() + ": " + ChatColor.WHITE + matchTeam.getMembers().size() + ChatColor.DARK_GRAY + "/" + ChatColor.GRAY + matchTeam.getMax();
            footer += ChatColor.DARK_GRAY + " - ";
        }
        footer += ChatColor.AQUA + "Spectators: " + ChatColor.WHITE + TGM.get().getModule(TeamManagerModule.class).getSpectators().getMembers().size();


        player.setPlayerListHeaderFooter(header, footer);
    }
    private void refreshAllTabs() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            refreshTab(player);
        }
    }
}
