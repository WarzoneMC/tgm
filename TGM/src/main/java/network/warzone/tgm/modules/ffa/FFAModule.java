package network.warzone.tgm.modules.ffa;

import com.google.gson.JsonObject;
import lombok.Getter;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.match.MatchStatus;
import network.warzone.tgm.modules.DeathModule;
import network.warzone.tgm.modules.scoreboard.ScoreboardInitEvent;
import network.warzone.tgm.modules.scoreboard.ScoreboardManagerModule;
import network.warzone.tgm.modules.scoreboard.SimpleScoreboard;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamChangeEvent;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.modules.time.TimeLimitService;
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
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scoreboard.Team;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Jorge on 3/2/2018.
 */
public class FFAModule extends MatchModule implements Listener {

    private Match match;
    private TeamManagerModule teamManagerModule;
    @Getter private Map<String, Integer> scores = new HashMap<>();

    private boolean timeLimitEnabled = true;
    private int timeLimit = 10*60; // Default: 10 minutes
    private int killLimit = 25; // Default: 25 kills

    private Map<Integer, String> playerScoreboardLines = new HashMap<>();

    private MatchTeam playersTeam;

    private String title;

    @Getter private boolean blitzMode;
    private int lives;
    private Map<String, Integer> playerLives = new HashMap<>(); // Username, lives

    @Override
    public void load(Match match) {
        this.match = match;
        this.teamManagerModule = TGM.get().getModule(TeamManagerModule.class);
        this.playersTeam = teamManagerModule.getTeams().get(1);
        if (match.getMapContainer().getMapInfo().getJsonObject().has("ffa")) {
            JsonObject ffaObj = match.getMapContainer().getMapInfo().getJsonObject().get("ffa").getAsJsonObject();
            if (ffaObj.has("timeLimit")) {
                if (ffaObj.get("timeLimit").getAsJsonObject().has("enabled")) timeLimitEnabled = ffaObj.get("timeLimit").getAsJsonObject().get("enabled").getAsBoolean();
                if (ffaObj.get("timeLimit").getAsJsonObject().has("limit")) timeLimit = ffaObj.get("timeLimit").getAsJsonObject().get("limit").getAsInt();
            }
            if (ffaObj.has("killLimit")) killLimit = ffaObj.get("killLimit").getAsInt();
            if (ffaObj.has("title")) title = ffaObj.get("title").getAsString();
            if (ffaObj.has("lives")) {
                blitzMode = true;
                lives = ffaObj.get("lives").getAsInt();
            }
        }
        if (title == null) {
            if (!blitzMode) title = "&bFFA - %killLimit% Kill" + (killLimit != 1 ? "s" : "");
            else title = "&bFFA - %lives% Li" + (killLimit != 1 ? "fes" : "ve");
        }

        title = title.replace("%killLimit%", String.valueOf(killLimit)).replace("%lives%", String.valueOf(lives));

        TimeModule timeModule = match.getModule(TimeModule.class);
        if (timeLimitEnabled) {
            timeModule.setTimeLimited(timeLimitEnabled);
            timeModule.setTimeLimit(timeLimit);
        }
        timeModule.setTimeLimitService(() -> getWinner());
    }

    @Override
    public void enable() {
        TGM.get().getPlayerManager().getPlayers().forEach(playerContext -> allowFriendlyFire(playerContext));
        for (PlayerContext playerContext : playersTeam.getMembers()) {
            Player player = playerContext.getPlayer();
            scores.put(player.getName(), 0);
        }
    }

    private void allowFriendlyFire(PlayerContext context) {
        Team team = TGM.get().getModule(ScoreboardManagerModule.class).getScoreboard(context.getPlayer()).getScoreboard().getTeam(this.playersTeam.getId());
        team.setAllowFriendlyFire(true);
    }

    @EventHandler
    public void onTeamChange(TeamChangeEvent event) {
        if (event.getTeam().isSpectator()) {
            if (blitzMode && match.getMatchStatus().equals(MatchStatus.MID) && hasWinner()) {
               TGM.get().getMatchManager().endMatch(forceWinner(getAlivePlayers().get(0).getPlayer()));
            }
        } else {
            if (blitzMode && event.getTeam().equals(playersTeam)) {
                playerLives.put(event.getPlayerContext().getPlayer().getName(), lives);
            }
            else {
                scores.put(event.getPlayerContext().getPlayer().getName(), scores.getOrDefault(event.getPlayerContext().getPlayer().getName(), 0));
            }
        }
        refreshScoreboards();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent event) {
        if (blitzMode) {
            removeLives(event.getPlayer());
            if (match.getMatchStatus().equals(MatchStatus.MID) && hasWinner()) TGM.get().getMatchManager().endMatch(forceWinner(getAlivePlayers().get(0).getPlayer()));
            refreshScoreboards();
        }
    }

    public void createDeath(Player player) {
        DeathModule deathModule = match.getModule(DeathModule.class).getPlayer(player);
        Bukkit.getPluginManager().callEvent(new TGMPlayerDeathEvent(player, deathModule.getKiller(), deathModule.getCause(), deathModule.getItem()));
    }

    @EventHandler
    public void onScoreboardInit(ScoreboardInitEvent event) {
        refreshScoreboard(event.getSimpleScoreboard());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        allowFriendlyFire(TGM.get().getPlayerManager().getPlayerContext(event.getPlayer()));
    }

    @EventHandler
    public void onDeath(TGMPlayerDeathEvent event) {
        if (blitzMode) {
            removeLife(event.getVictim());
            if (getLives(event.getVictim()) <= 0) {
                MatchTeam team = teamManagerModule.getTeam(event.getVictim());
                Bukkit.broadcastMessage(team.getColor() + event.getVictim().getName() + ChatColor.RED + " has been eliminated!");
                event.getVictim().sendTitle("", ChatColor.RED + "You have been eliminated.", 10, 20, 10);
            }
            if (match.getMatchStatus().equals(MatchStatus.MID) && hasWinner()) TGM.get().getMatchManager().endMatch(forceWinner(getAlivePlayers().get(0).getPlayer()));
        } else {
            if (event.getKiller() != null) {
                addPoints(event.getKiller(), 1);
                if (hasWinner()) {
                    MatchTeam winner = getWinner();
                    TGM.get().getMatchManager().endMatch(winner);
                    winner.getMembers().forEach(playerContext -> playerContext.getPlayer().setAllowFlight(true));
                }
            }
        }
        refreshScoreboards();
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if (blitzMode && match.getMatchStatus().equals(MatchStatus.MID) && getLives(event.getPlayer()) <= 0) {
            Bukkit.getScheduler().runTaskLater(TGM.get(), () -> {
                event.getPlayer().setGameMode(GameMode.SPECTATOR);
                event.getPlayer().setAllowFlight(true);
                event.getPlayer().setFlying(true);
            }, 1);
        }
    }

    public void refreshScoreboards() {
        setPlayerScoreboardLines();
        for (SimpleScoreboard simpleScoreboard : match.getModule(ScoreboardManagerModule.class).getScoreboards().values()) {
            refreshScoreboard(simpleScoreboard);
        }
    }

    public void refreshScoreboard(SimpleScoreboard simpleScoreboard) {
        simpleScoreboard.setTitle(ChatColor.translateAlternateColorCodes('&', title));
        int line = 0;
        for (int i : playerScoreboardLines.keySet()) {
            simpleScoreboard.add(playerScoreboardLines.get(i), i);
            if (i > line) line = i;
        }
        line++;
        simpleScoreboard.add(ChatColor.RESET + "", line++);
        simpleScoreboard.update();
    }

    public void setPlayerScoreboardLines() {
        playerScoreboardLines.clear();
        List<String> lines = new ArrayList<>();

        Object[] a;
        if (blitzMode) a = playerLives.entrySet().toArray();
        else a = scores.entrySet().toArray();

        Arrays.sort(a, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Map.Entry<String, Integer>) o1).getValue().compareTo(((Map.Entry<String, Integer>) o2).getValue());
            }
        });
        for (Object e : a) {
            Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) e;
            String player = entry.getKey();
            int score = entry.getValue();
            if (lines.size() > 14) {
                lines.remove(0);
            }
            if (blitzMode) lines.add(this.playersTeam.getColor() + player + "" + ChatColor.GRAY + ": " + ChatColor.RESET + score);
            else {
                if (player.equals(((Map.Entry<String, Integer>) a[a.length - 1]).getKey())) {
                    lines.add(ChatColor.YELLOW + player + ChatColor.GRAY + ": " + ChatColor.RESET + score);
                } else {
                    lines.add(this.playersTeam.getColor() + player + "" + ChatColor.GRAY + ": " + ChatColor.RESET + score);
                }
            }
        }
        int i = 0;
        for (String line : lines) {
            playerScoreboardLines.put(i, line);
            i++;
        }
    }

    public void addPoints(Player player, int points) {
        scores.put(player.getName(), getScore(player) + points);
    }

    public int getScore(Player player) {
        return getScore(player.getName());
    }

    public int getScore(String player) {
        return scores.getOrDefault(player, 0);
    }

    public int getLives(Player player) {
        return playerLives.getOrDefault(player.getName(), lives);
    }

    public void removeLife(Player player) {
        playerLives.put(player.getName(), getLives(player) - 1);
    }

    public void removeLives(Player player) {
        playerLives.remove(player);
    }

    public List<PlayerContext> getAlivePlayers() {
        return playersTeam.getMembers().stream().filter(playerContext -> getLives(playerContext.getPlayer()) > 0).collect(Collectors.toList());
    }

    private boolean hasWinner() {
        if (blitzMode) {
            return getAlivePlayers().size() == 1;
        } else {
            for (int i : scores.values()) {
                if (i >= killLimit) {
                    return true;
                }
            }
        }
        return false;
    }

    public MatchTeam getWinner() {
        String player = null;
        int highest = 0;
        for (String name : scores.keySet()) {
            if (scores.get(name) > highest) {
                player = name;
                highest = scores.get(name);
            }
        }
        return setupTeam(player);
    }

    public MatchTeam forceWinner(Player player) {
        return setupTeam(player.getName());
    }

    private MatchTeam setupTeam(String player) {
        ScoreboardManagerModule scoreboardManagerModule = match.getModule(ScoreboardManagerModule.class);

        if (this.teamManagerModule.getTeamByAlias("winner") == null) {
            this.teamManagerModule.addTeam(new MatchTeam("winner", player, ChatColor.YELLOW, false, 1, 1));
            TGM.get().getPlayerManager().getPlayers().forEach(playerContext -> {
                scoreboardManagerModule.registerScoreboardTeam(scoreboardManagerModule.getScoreboard(playerContext.getPlayer()), this.teamManagerModule.getTeamByAlias("winner"), playerContext);
            });
        }

        MatchTeam winnerTeam = this.teamManagerModule.getTeamByAlias("winner");
        winnerTeam.setAlias(player != null ? player : "None");

        if (player != null && Bukkit.getPlayer(player) != null) {
            PlayerContext playerContext = TGM.get().getPlayerManager().getPlayerContext(Bukkit.getPlayer(player));
            MatchTeam oldTeam = this.teamManagerModule.getTeam(playerContext.getPlayer());
            if (oldTeam != null) {
                oldTeam.removePlayer(playerContext);
            }
            winnerTeam.addPlayer(playerContext);
            for (PlayerContext context : TGM.get().getPlayerManager().getPlayers()) {
                SimpleScoreboard simpleScoreboard = scoreboardManagerModule.getScoreboard(context.getPlayer());

                Team old = simpleScoreboard.getScoreboard().getTeam(oldTeam.getId());
                old.removeEntry(playerContext.getPlayer().getName());

                Team to = simpleScoreboard.getScoreboard().getTeam(winnerTeam.getId());
                to.addEntry(playerContext.getPlayer().getName());
            }
        }
        return winnerTeam;
    }
}
