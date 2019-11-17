package network.warzone.tgm.modules.countdown;

import lombok.Getter;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamChangeEvent;
import network.warzone.tgm.user.PlayerContext;
import network.warzone.tgm.util.Placeholders;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Jorge on 10/20/2019
 */
@Getter
public class CustomCountdown extends BossBarCountdown {

    private int time; // seconds
    private String title;
    private BarColor color;
    private BarStyle style;
    private boolean visible;
    private boolean invert;
    private List<MatchTeam> teams;
    private List<String> onFinish; // commands

    public CustomCountdown(int time, String title, BarColor color, BarStyle style, boolean visible, boolean invert, List<MatchTeam> teams, List<String> onFinish) {
        this.time = time;
        this.title = title;
        this.color = color;
        this.style = style;
        this.visible = visible;
        this.invert = invert;
        this.teams = teams;
        this.onFinish = onFinish;
        this.bossBar = initBossBar();
    }

    public CustomCountdown(int time, String title) {
        this(time, title, BarColor.PURPLE, BarStyle.SOLID, true, false, Collections.emptyList(), Collections.emptyList());
    }

    public void start() {
        this.start(this.time);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onTeamChange(TeamChangeEvent event) {
        if (this.teams == null || this.teams.isEmpty() || event.isCancelled()) return;
        if (this.teams.contains(event.getTeam())) {
            if (!this.teams.contains(event.getOldTeam())) bossBar.addPlayer(event.getPlayerContext().getPlayer());
        } else {
            if (this.teams.contains(event.getOldTeam())) bossBar.removePlayer(event.getPlayerContext().getPlayer());
        }
    }

    @Override
    public BossBar initBossBar() {
        BossBar bossBar = Bukkit.createBossBar(getFormattedTitle(), this.color, this.style);
        bossBar.setVisible(false);
        return bossBar;
    }

    @Override
    protected void onStart() {
        addPlayers();
        getBossBar().setVisible(this.visible);
    }

    @Override
    protected void onTick() {
        getBossBar().setProgress(getProgress());
        if (getTimeLeft() % 20 == 0) {
            getBossBar().setTitle(getFormattedTitle());
        }
    }

    @Override
    protected void onFinish() {
        getBossBar().setVisible(false);
        getOnFinish().forEach(c -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), c));
    }

    @Override
    protected void onCancel() {
        getBossBar().setVisible(false);
    }

    private double getProgress() {
        double progress;
        if (!this.invert) progress = (getTimeMax() - getTimeLeft()) / getTimeMax();
        else progress = getTimeLeft() / getTimeMax();
        if (progress > 1) return 1.0;
        if (progress < 0) return 0.0;
        else return progress;
    }

    private String getFormattedTitle() {
        Map<String, String> placeholders = new HashMap<>();
        Placeholders.addPlaceholders(placeholders, this);
        return ChatColor.translateAlternateColorCodes('&', Placeholders.apply(this.title, placeholders));
    }


    private List<Player> getPlayers() {
        if (this.teams == null || this.teams.isEmpty()) return new ArrayList<>(Bukkit.getOnlinePlayers());
        else return this.teams.stream().flatMap(team -> team.getMembers().stream()).map(PlayerContext::getPlayer).collect(Collectors.toList());
    }

    private void addPlayers() {
        getPlayers().stream().filter(player -> !getBossBar().getPlayers().contains(player)).forEach(player -> getBossBar().addPlayer(player));
    }

    @Override
    public void start(double countdown) {
        setCancelled(false);

        setTimeMax(this.time * 20);
        setTimeLeft(countdown * 20);

        onStart();
    }

    public void onUpdate() {
        setTimeMax(this.time * 20);
        getBossBar().setTitle(getFormattedTitle());
        getBossBar().setColor(this.color);
        getBossBar().setStyle(this.style);
        if (!isCancelled()) getBossBar().setVisible(isVisible());
        getBossBar().getPlayers().forEach(player -> getBossBar().removePlayer(player));
        addPlayers();
    }

    public void setTime(int time) {
        this.time = time;
        onUpdate();
    }

    public void setTitle(String title) {
        this.title = title;
        onUpdate();
    }

    public void setColor(BarColor color) {
        this.color = color;
        onUpdate();
    }

    public void setStyle(BarStyle style) {
        this.style = style;
        onUpdate();
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        onUpdate();
    }

    public void setInvert(boolean invert) {
        this.invert = invert;
        onUpdate();
    }

    public void setTeams(List<MatchTeam> teams) {
        this.teams = teams;
        onUpdate();
    }

    public void setOnFinish(List<String> onFinish) {
        this.onFinish = onFinish;
        onUpdate();
    }
}
