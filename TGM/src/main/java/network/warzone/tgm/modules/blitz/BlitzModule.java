package network.warzone.tgm.modules.blitz;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.match.MatchStatus;
import network.warzone.tgm.modules.DeathModule;
import network.warzone.tgm.modules.SpawnPointHandlerModule;
import network.warzone.tgm.modules.scoreboard.ScoreboardInitEvent;
import network.warzone.tgm.modules.scoreboard.ScoreboardManagerModule;
import network.warzone.tgm.modules.scoreboard.SimpleScoreboard;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamChangeEvent;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.modules.time.TimeModule;
import network.warzone.tgm.player.event.TGMPlayerDeathEvent;
import network.warzone.tgm.user.PlayerContext;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Jorge on 10/7/2017.
 */
public class BlitzModule extends MatchModule implements Listener {

    private Map<MatchTeam, Integer> teamLives = new HashMap<>();
    private Map<UUID, Integer> playerLives = new HashMap<>();

    @Getter private final HashMap<MatchTeam, Integer> teamScoreboardLines = new HashMap<>();

    private int livesDisplayTaskId;

    private TeamManagerModule teamManagerModule;

    private String title = "";
    private String subtitle = ChatColor.GREEN + "Remaining lives:  " + ChatColor.YELLOW + "%lives%";
    private String actionbar = "";

    private Match match;

    @Override
    public void load(Match match) {
        this.match = match;
        this.teamManagerModule = TGM.get().getModule(TeamManagerModule.class);
        JsonObject mapInfo = match.getMapContainer().getMapInfo().getJsonObject();

        if (mapInfo.has("blitz")) {
            JsonObject blitz = mapInfo.getAsJsonObject("blitz");

            if (blitz.has("death-title")) title = blitz.get("death-title").getAsString();
            if (blitz.has("death-subtitle")) subtitle = blitz.get("death-subtitle").getAsString();
            if (blitz.has("actionbar")) actionbar = blitz.get("actionbar").getAsString();
            for (JsonElement teamElement : blitz.getAsJsonArray("lives")) {
                JsonObject teamObject = (JsonObject) teamElement;
                MatchTeam team = teamManagerModule.getTeamById(teamObject.get("team").getAsString());
                if (team == null || team.isSpectator()) continue;
                teamLives.put(team, teamObject.get("lives").getAsInt());
            }
        } else {
            for (MatchTeam team : teamManagerModule.getTeams()) {
                if (team.isSpectator()) continue;
                teamLives.put(team, 1);
            }
        }

        TGM.get().getModule(TimeModule.class).setTimeLimitService(this::getBiggestTeam);
    }

    private MatchTeam getBiggestTeam() {
        MatchTeam highest = null;
        for (MatchTeam team : teamManagerModule.getTeams()) {
            if (team.isSpectator()) continue;
            if (highest == null) {
                highest = team;
                continue;
            }
            if (getAlivePlayers(team).size() > getAlivePlayers(highest).size()) {
                highest = team;
            }
        }
        if (highest != null) {
            final MatchTeam team = highest;
            int amount = (int) teamManagerModule.getTeams().stream().filter(t -> getAlivePlayers(team) == getAlivePlayers(t) && !t.isSpectator()).count();
            if (amount > 1) return null;
            else return team;
        }
        return null;
    }

    @Override
    public void enable() {
        for (MatchTeam team : teamManagerModule.getTeams()) {
            if (team.isSpectator()) continue;
            for (PlayerContext player : team.getMembers()) {
                playerLives.put(player.getPlayer().getUniqueId(), teamLives.getOrDefault(team, 1));
                showLives(player.getPlayer());
            }
        }
        livesDisplayTaskId = Bukkit.getScheduler().runTaskTimer(TGM.get(), () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (teamManagerModule.getTeam(player).isSpectator()) return;
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', actionbar.replaceAll("%lives%", "" + getLives(player)).replaceAll("%player%", player.getName()))));
            }
        }, 2L, 2L).getTaskId();
    }

    @EventHandler
    public void onScoreboardInit(ScoreboardInitEvent event) {
        List<MatchTeam> teams = teamManagerModule.getTeams();

        SimpleScoreboard simpleScoreboard = event.getSimpleScoreboard();
        simpleScoreboard.setTitle(ChatColor.AQUA + "Players");

        int i = 0;
        for (MatchTeam matchTeam : teams) {
            if (matchTeam.isSpectator()) continue;
            simpleScoreboard.add(matchTeam.getColor() + getTeamScoreLine(matchTeam, getAlivePlayers(matchTeam).size()), i);
            teamScoreboardLines.put(matchTeam, i++);
            simpleScoreboard.add(matchTeam.getColor() + matchTeam.getAlias(), i++);
            if (teams.indexOf(matchTeam) < teams.size() - 1) {
                simpleScoreboard.add(matchTeam.getColor() + " ", i++);
            }
        }
    }

    public void updateScoreboardTeamLine(MatchTeam matchTeam, int size) {
        if (!teamScoreboardLines.containsKey(matchTeam)) return;
        for (SimpleScoreboard simpleScoreboard : TGM.get().getModule(ScoreboardManagerModule.class).getScoreboards().values()) {
            int line = teamScoreboardLines.get(matchTeam);
            simpleScoreboard.remove(line);
            simpleScoreboard.add(getTeamScoreLine(matchTeam, size), line);
            simpleScoreboard.update();
        }
    }

    private String getTeamScoreLine(MatchTeam matchTeam, int size) {
        return "  " + ChatColor.RESET + size + ChatColor.GRAY + " Alive";
    }

    private void showLives(Player player) {
        player.sendTitle(
                ChatColor.translateAlternateColorCodes('&', title.replaceAll("%lives%", "" + getLives(player)).replaceAll("%player%", player.getName())),
                ChatColor.translateAlternateColorCodes('&', subtitle.replaceAll("%lives%", "" + getLives(player)).replaceAll("%player%", player.getName())),
                10, 20, 10
        );
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (!TGM.get().getMatchManager().getMatch().getMatchStatus().equals(MatchStatus.MID) || teamManagerModule.getTeam(player).isSpectator())
            return;
        if (player.getHealth() - event.getFinalDamage() >= 0.5) return;

        event.setDamage(0);

        createDeath((Player) event.getEntity());
        removeLife(player);

        TGM.get().getModule(SpawnPointHandlerModule.class).spawnPlayer(TGM.get().getPlayerManager().getPlayerContext(player), teamManagerModule.getTeam(player), false);

        if (getLives(player) <= 0) {
            player.setGameMode(GameMode.SPECTATOR);
            player.setAllowFlight(true);
            player.setFlying(true);
            player.getInventory().clear();

            if (player.getLocation().getY() < 0) {
                player.teleport(teamManagerModule.getTeam(player).getSpawnPoints().get(0).getLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
            }

            MatchTeam team = teamManagerModule.getTeam(player);
            updateScoreboardTeamLine(team, getAlivePlayers(team).size());

            Bukkit.broadcastMessage(team.getColor() + player.getName() + ChatColor.RED + " has been eliminated!");
            player.sendTitle("", ChatColor.RED + "You have been eliminated.", 10, 20, 10);

        } else {
            showLives(player);
            player.teleport(teamManagerModule.getTeam(player).getSpawnPoints().get(0).getLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
        }

        if (lastTeamAlive()) {
            MatchTeam winnerTeam = teamManagerModule.getTeams().stream().filter(matchTeam -> !matchTeam.isSpectator()).filter(matchTeam -> getAlivePlayers(matchTeam).size() > 0).findFirst()
                    .orElseGet(() -> teamManagerModule.getTeams().get(1));

            TGM.get().getMatchManager().endMatch(winnerTeam);
        }
    }

    private void createDeath(Player player) {
        DeathModule deathModule = match.getModule(DeathModule.class).getPlayer(player);
        Bukkit.getPluginManager().callEvent(new TGMPlayerDeathEvent(player, deathModule.getKiller(), deathModule.getCause(), deathModule.getItem()));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent event) {
        if ((teamManagerModule.getTeam(event.getPlayer()) != null && teamManagerModule.getTeam(event.getPlayer()).isSpectator())) return;
        updateScoreboardTeamLine(teamManagerModule.getTeam(event.getPlayer()), getAlivePlayers(teamManagerModule.getTeam(event.getPlayer())).size() - 1);

        if (!TGM.get().getMatchManager().getMatch().getMatchStatus().equals(MatchStatus.MID)) return;

        if (lastTeamAlive()) {
            MatchTeam winnerTeam = teamManagerModule.getTeams().stream().filter(matchTeam -> !matchTeam.isSpectator()).filter(matchTeam -> getAlivePlayers(matchTeam).size() > 0).findFirst()
                    .orElseGet(() -> teamManagerModule.getTeams().get(1));

            TGM.get().getMatchManager().endMatch(winnerTeam);
        }
    }

    @EventHandler
    public void onTeamChange(TeamChangeEvent event) {
        if (event.getOldTeam() != null && !event.getOldTeam().isSpectator()) updateScoreboardTeamLine(event.getOldTeam(), getAlivePlayers(event.getOldTeam()).size());
        if (!event.getTeam().isSpectator()) updateScoreboardTeamLine(event.getTeam(), getAlivePlayers(event.getTeam()).size());

        playerLives.put(event.getPlayerContext().getPlayer().getUniqueId(), teamLives.getOrDefault(event.getTeam(), 1));
    }

    @Override
    public void unload() {
        Bukkit.getScheduler().cancelTask(livesDisplayTaskId);

        teamLives.clear();
        playerLives.clear();
        teamScoreboardLines.clear();
    }

    private void removeLife(Player player) {
        playerLives.put(player.getUniqueId(), (playerLives.getOrDefault(player.getUniqueId(), 0) > 0 ? playerLives.get(player.getUniqueId()) - 1 : 0));
    }

    private int getLives(Player player) {
        return playerLives.getOrDefault(player.getUniqueId(), 0);
    }

    private boolean lastTeamAlive() {
        List<MatchTeam> aliveTeams = new ArrayList<>();

        for (MatchTeam team : teamManagerModule.getTeams()) {
            if (team.isSpectator()) continue;
            if (getAlivePlayers(team).size() > 0)
                aliveTeams.add(team);
        }

        return aliveTeams.size() <= 1;
    }

    private List<PlayerContext> getAlivePlayers(MatchTeam matchTeam) {
        return matchTeam.getMembers().stream().filter(playerContext -> playerContext.getPlayer().getGameMode() != GameMode.SPECTATOR).collect(Collectors.toList());
    }

}
