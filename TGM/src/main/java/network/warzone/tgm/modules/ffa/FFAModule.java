package network.warzone.tgm.modules.ffa;

import com.google.gson.JsonObject;
import lombok.Getter;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.match.MatchStatus;
import network.warzone.tgm.modules.respawn.RespawnModule;
import network.warzone.tgm.modules.scoreboard.ScoreboardInitEvent;
import network.warzone.tgm.modules.scoreboard.ScoreboardManagerModule;
import network.warzone.tgm.modules.scoreboard.SimpleScoreboard;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamChangeEvent;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.modules.time.TimeModule;
import network.warzone.tgm.player.event.PlayerJoinTeamAttemptEvent;
import network.warzone.tgm.player.event.TGMPlayerDeathEvent;
import network.warzone.tgm.player.event.TGMPlayerRespawnEvent;
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
import org.bukkit.scoreboard.Team;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Jorge on 3/2/2018.
 */
public class FFAModule extends MatchModule implements Listener {

    private WeakReference<Match> match;
    private TeamManagerModule teamManagerModule;
    private ScoreboardManagerModule scoreboardManagerModule;
    @Getter private Map<String, Integer> scores = new HashMap<>();

    private int killLimit = 25; // Default: 25 kills

    private Map<Integer, String> playerScoreboardLines = new HashMap<>();

    private MatchTeam playersTeam;

    private String title;

    @Getter private boolean blitzMode;
    private int lives;
    private Map<String, Integer> playerLives = new HashMap<>(); // Username, lives

    @Override
    public void load(Match match) {
        this.match = new WeakReference<Match>(match);
        this.teamManagerModule = TGM.get().getModule(TeamManagerModule.class);
        this.scoreboardManagerModule = TGM.get().getModule(ScoreboardManagerModule.class);
        this.playersTeam = this.teamManagerModule.getTeams().get(1);
        if (match.getMapContainer().getMapInfo().getJsonObject().has("ffa")) {
            JsonObject ffaObj = match.getMapContainer().getMapInfo().getJsonObject().get("ffa").getAsJsonObject();
            if (ffaObj.has("killLimit")) this.killLimit = ffaObj.get("killLimit").getAsInt();
            if (ffaObj.has("title")) this.title = ffaObj.get("title").getAsString();
            if (ffaObj.has("lives")) {
                this.blitzMode = true;
                this.lives = ffaObj.get("lives").getAsInt();
            }
        }
        if (this.title == null) {
            if (!this.blitzMode) this.title = "&bFFA - " + this.killLimit + " Kill" + (this.killLimit != 1 ? "s" : "");
            else this.title = "&bFFA - " + this.lives + " Li" + (this.lives != 1 ? "ves" : "fe");
        }

        this.title = this.title.replace("%killLimit%", String.valueOf(this.killLimit)).replace("%lives%", String.valueOf(this.lives));

        TimeModule timeModule = match.getModule(TimeModule.class);
        timeModule.setTimeLimit(10*60);
        timeModule.setTimeLimitService(this::getWinner);
        if (this.blitzMode) TGM.get().getModule(RespawnModule.class).addRespawnService(this::isAlive);
    }

    @Override
    public void unload() {
        this.scores.clear();
        this.playerScoreboardLines.clear();
        this.playerLives.clear();
    }

    @Override
    public void enable() {
        this.playersTeam.setFriendlyFire(true);
        for (PlayerContext playerContext : this.playersTeam.getMembers()) {
            Player player = playerContext.getPlayer();
            this.scores.put(player.getName(), 0);
        }
        TGM.get().getPlayerManager().getPlayers().forEach(this::allowFriendlyFire);
    }

    private void allowFriendlyFire(PlayerContext context) throws NullPointerException {
        Team team = scoreboardManagerModule.getScoreboard(context.getPlayer()).getScoreboard().getTeam(this.playersTeam.getId());
        Objects.requireNonNull(team);
        team.setAllowFriendlyFire(true);
    }


    @EventHandler(ignoreCancelled = true)
    public void onJoinAttempt(PlayerJoinTeamAttemptEvent event) {
        if (!this.match.get().getMatchStatus().equals(MatchStatus.PRE) && this.blitzMode) {
            event.getPlayerContext().getPlayer().sendMessage(ChatColor.RED + "You can't pick a team after the match starts in this mode.");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onTeamChange(TeamChangeEvent event) {
        if (event.isCancelled()) return;
        if (event.getTeam().isSpectator()) {
            if (this.blitzMode && match.get().getMatchStatus().equals(MatchStatus.MID) && hasWinner()) {
                TGM.get().getMatchManager().endMatch(forceWinner(getAlivePlayers().get(0).getPlayer()));
            }
        } else {
            if (this.blitzMode && event.getTeam().equals(this.playersTeam)) {
                this.playerLives.put(event.getPlayerContext().getPlayer().getName(), this.lives);
            }
            else {
                this.scores.put(event.getPlayerContext().getPlayer().getName(), this.scores.getOrDefault(event.getPlayerContext().getPlayer().getName(), 0));
            }
        }
        refreshScoreboards();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent event) {
        if (this.blitzMode) {
            removeLives(event.getPlayer());
            if (this.match.get().getMatchStatus().equals(MatchStatus.MID) && hasWinner()) TGM.get().getMatchManager().endMatch(forceWinner(getAlivePlayers().get(0).getPlayer()));
            refreshScoreboards();
        }
    }

    @EventHandler
    public void onScoreboardInit(ScoreboardInitEvent event) {
        refreshScoreboard(event.getSimpleScoreboard());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        allowFriendlyFire(TGM.get().getPlayerManager().getPlayerContext(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDeath(TGMPlayerDeathEvent event) {
        if (this.blitzMode) {
            removeLife(event.getVictim());
            if (!isAlive(event.getVictim())) {
                MatchTeam team = this.teamManagerModule.getTeam(event.getVictim());
                Bukkit.broadcastMessage(team.getColor() + event.getVictim().getName() + ChatColor.RED + " has been eliminated!");
                event.getVictim().sendTitle("", ChatColor.RED + "You have been eliminated.", 10, 20, 10);
            }
        } else {
            if (event.getKiller() != null) {
                addPoint(event.getKiller());
                if (hasWinner()) {
                    MatchTeam winner = getWinner();
                    TGM.get().getMatchManager().endMatch(winner);
                    winner.getMembers().forEach(playerContext -> playerContext.getPlayer().setAllowFlight(true));
                }
            }
        }
        refreshScoreboards();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDeathHigh(TGMPlayerDeathEvent event) {
        if (this.blitzMode && this.match.get().getMatchStatus().equals(MatchStatus.MID) && hasWinner())
            TGM.get().getMatchManager().endMatch(forceWinner(getAlivePlayers().get(0).getPlayer()));
    }

    @EventHandler
    public void onRespawn(TGMPlayerRespawnEvent event) {
        if (this.blitzMode && this.match.get().getMatchStatus().equals(MatchStatus.MID) && getLives(event.getPlayer()) <= 0) {
            event.getPlayer().setGameMode(GameMode.SPECTATOR);
            event.getPlayer().setAllowFlight(true);
            event.getPlayer().setFlying(true);
        }
    }

    private void refreshScoreboards() {
        setPlayerScoreboardLines();
        for (SimpleScoreboard simpleScoreboard : this.scoreboardManagerModule.getScoreboards().values()) {
            refreshScoreboard(simpleScoreboard);
        }
    }

    private void refreshScoreboard(SimpleScoreboard simpleScoreboard) {
        simpleScoreboard.setTitle(ChatColor.translateAlternateColorCodes('&', title));
        int line = 2;
        for (int i : this.playerScoreboardLines.keySet()) {
            simpleScoreboard.add(this.playerScoreboardLines.get(i), i);
            if (i > line) line = i;
        }
        simpleScoreboard.update();
    }

    private void setPlayerScoreboardLines() {
        this.playerScoreboardLines.clear();
        List<String> lines = new ArrayList<>();

        List<Map.Entry<String, Integer>> a;
        if (this.blitzMode) a = new ArrayList<>(this.playerLives.entrySet());
        else a = new ArrayList<>(this.scores.entrySet());
        a.sort(Comparator.comparing(Map.Entry::getValue));
        Iterator<Map.Entry<String, Integer>> iterator = a.iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Integer> entry = iterator.next();
            String player = entry.getKey();
            int score = entry.getValue();

            if (lines.size() > 12) {
                lines.remove(0);
            }

            if (this.blitzMode) {
                if (!iterator.hasNext()) {
                    lines.add(ChatColor.YELLOW + player + ChatColor.GRAY + ": " + ChatColor.WHITE + score);
                } else {
                    lines.add(this.playersTeam.getColor() + player + "" + ChatColor.GRAY + ": " + ChatColor.WHITE + score);
                }
            }
            else {
                if (!iterator.hasNext()) {
                    lines.add(ChatColor.YELLOW + player + ChatColor.GRAY + ": " + ChatColor.RESET + score);
                } else {
                    lines.add(this.playersTeam.getColor() + player + ChatColor.GRAY + ": " + ChatColor.RESET + score);
                }
            }
        }
        int i = 2;
        for (String line : lines) {
            this.playerScoreboardLines.put(i, line);
            i++;
        }
    }

    private void addPoint(Player player) {
        this.scores.put(player.getName(), getScore(player) + 1);
    }

    private int getScore(Player player) {
        return getScore(player.getName());
    }

    private int getScore(String player) {
        return this.scores.getOrDefault(player, 0);
    }

    private int getLives(Player player) {
        return this.playerLives.getOrDefault(player.getName(), this.lives);
    }

    private boolean isAlive(Player player) {
        return getLives(player) > 0;
    }

    private void removeLife(Player player) {
        this.playerLives.put(player.getName(), getLives(player) - 1);
    }

    private void removeLives(Player player) {
        this.playerLives.remove(player.getName());
    }

    private List<PlayerContext> getAlivePlayers() {
        return this.playersTeam.getMembers().stream().filter(playerContext -> getLives(playerContext.getPlayer()) > 0).collect(Collectors.toList());
    }

    private boolean hasWinner() {
        if (this.blitzMode) {
            return getAlivePlayers().size() == 1;
        } else {
            for (int i : this.scores.values()) {
                if (i >= this.killLimit) {
                    return true;
                }
            }
        }
        return false;
    }

    private MatchTeam getWinner() {
        String player = null;
        int highest = 0;
        for (String name : this.scores.keySet()) {
            if (this.scores.get(name) > highest) {
                player = name;
                highest = this.scores.get(name);
            }
        }
        return setupTeam(player);
    }

    private MatchTeam forceWinner(Player player) {
        return setupTeam(player.getName());
    }

    private MatchTeam setupTeam(String player) {
        if (this.teamManagerModule.getTeamByAlias("winner") == null) {
            this.teamManagerModule.addTeam(new MatchTeam("winner", player, ChatColor.YELLOW, GameMode.SURVIVAL, false, 0, 1, true));
            TGM.get().getPlayerManager().getPlayers().forEach(playerContext ->
                this.scoreboardManagerModule.registerScoreboardTeam(
                        this.scoreboardManagerModule.getScoreboard(playerContext.getPlayer()),
                        this.teamManagerModule.getTeamByAlias("winner"),
                        playerContext
                )
            );
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

                if (oldTeam != null) {
                    Team old = simpleScoreboard.getScoreboard().getTeam(oldTeam.getId());
                    if (old != null) old.removeEntry(playerContext.getPlayer().getName());
                }

                Team to = simpleScoreboard.getScoreboard().getTeam(winnerTeam.getId());
                if (to != null) to.addEntry(playerContext.getPlayer().getName());
            }
        }
        return winnerTeam;
    }
}
