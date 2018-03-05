package network.warzone.tgm.modules.ctw;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.modules.region.Region;
import network.warzone.tgm.modules.region.RegionManagerModule;
import network.warzone.tgm.modules.scoreboard.ScoreboardInitEvent;
import network.warzone.tgm.modules.scoreboard.ScoreboardManagerModule;
import network.warzone.tgm.modules.scoreboard.SimpleScoreboard;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.modules.team.TeamUpdateEvent;
import network.warzone.tgm.modules.time.TimeLimitService;
import network.warzone.tgm.modules.time.TimeModule;
import network.warzone.tgm.modules.wool.WoolObjective;
import network.warzone.tgm.modules.wool.WoolObjectiveService;
import network.warzone.tgm.modules.wool.WoolStatus;
import network.warzone.tgm.user.PlayerContext;
import network.warzone.tgm.util.ColorConverter;
import network.warzone.tgm.util.FireworkUtil;
import network.warzone.tgm.util.Parser;
import network.warzone.tgm.util.Strings;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.*;
import java.util.stream.Collectors;

public class CTWModule extends MatchModule implements Listener {
    public static final String SYMBOL_WOOL_INCOMPLETE = "\u2b1c";   // ⬜
    public static final String SYMBOL_WOOL_TOUCHED = "\u2592";      // ▒
    public static final String SYMBOL_WOOL_COMPLETE = "\u2b1b";     // ⬛

    @Getter
    private final List<WoolObjective> wools = new ArrayList<>();

    @Getter
    private final HashMap<WoolObjective, List<Integer>> woolScoreboardLines = new HashMap<>();

    @Getter
    private final HashMap<MatchTeam, Integer> teamScoreboardLines = new HashMap<>();

    @Override
    public void load(Match match) {
        JsonObject dtmJson = match.getMapContainer().getMapInfo().getJsonObject().get("ctw").getAsJsonObject();

        for (JsonElement monumentElement : dtmJson.getAsJsonArray("wools")) {
            JsonObject monumentJson = monumentElement.getAsJsonObject();

            String name = monumentJson.get("name").getAsString();
            Region region = match.getModule(RegionManagerModule.class).getRegion(match, monumentJson.get("region"));
            List<MatchTeam> teams = Parser.getTeamsFromElement(match.getModule(TeamManagerModule.class), monumentJson.get("teams"));
            DyeColor color = DyeColor.valueOf(Strings.getTechnicalName(monumentJson.get("color").getAsString()));

            for (MatchTeam matchTeam : teams) {
                wools.add(new WoolObjective(name, color, matchTeam, region));
            }
        }

        TeamManagerModule teamManagerModule = match.getModule(TeamManagerModule.class);

        //wool services
        for (WoolObjective woolObjective : wools) {
            woolObjective.addService(new WoolObjectiveService() {
                @Override
                public void pickup(Player player, MatchTeam matchTeam, boolean firstTouch) {
                    if (firstTouch) {
                        updateOnScoreboard(woolObjective);

                        Bukkit.broadcastMessage(matchTeam.getColor() + player.getName() + ChatColor.WHITE +
                                " picked up " + woolObjective.getChatColor() + ChatColor.BOLD.toString() + woolObjective.getName());

                        for (MatchTeam otherTeam : teamManagerModule.getTeams()) {
                            for (PlayerContext playerContext : otherTeam.getMembers()) {
                                if (otherTeam.isSpectator() || otherTeam == matchTeam) {
                                    playerContext.getPlayer().playSound(playerContext.getPlayer().getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 0.7f, 2f);
                                } else {
                                    playerContext.getPlayer().playSound(playerContext.getPlayer().getLocation(), Sound.ENTITY_BLAZE_DEATH, 0.8f, 0.8f);
                                }
                            }
                        }
                    }
                }

                @Override
                public void place(Player player, MatchTeam matchTeam, Block block) {
                    updateOnScoreboard(woolObjective);

                    Bukkit.broadcastMessage(matchTeam.getColor() + player.getName() + ChatColor.WHITE +
                            " placed " + woolObjective.getChatColor() + ChatColor.BOLD.toString() + woolObjective.getName());

                    for (MatchTeam otherTeam : teamManagerModule.getTeams()) {
                        for (PlayerContext playerContext : otherTeam.getMembers()) {
                            if (otherTeam.isSpectator() || otherTeam == matchTeam) {
                                playerContext.getPlayer().playSound(playerContext.getPlayer().getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 0.7f, 2f);
                            } else {
                                playerContext.getPlayer().playSound(playerContext.getPlayer().getLocation(), Sound.ENTITY_BLAZE_DEATH, 0.8f, 0.8f);
                            }
                        }
                    }

                    playFireworkEffect(matchTeam.getColor(), block.getLocation());

                    if (getIncompleteWools(matchTeam).isEmpty()) {
                        TGM.get().getMatchManager().endMatch(matchTeam);
                    }
                }

                 @Override
                 public void drop(Player player, MatchTeam matchTeam, boolean broadcast) {
                    updateOnScoreboard(woolObjective);

                     if (broadcast) Bukkit.broadcastMessage(matchTeam.getColor() + player.getName() + ChatColor.WHITE +
                             " dropped " + woolObjective.getChatColor() + ChatColor.BOLD.toString() + woolObjective.getName());
                 }

            });
        }

        //load wools
        for (WoolObjective woolObjective : wools) {
            woolObjective.load();
        }

        TGM.get().getModule(TimeModule.class).setTimeLimitService(new TimeLimitService() {
            @Override
            public MatchTeam getWinnerTeam() {
                return getWinningTeam();
            }
        });
    }

    private MatchTeam getWinningTeam() {
        Map<MatchTeam, Integer> teamScores = new HashMap<>();
        Map.Entry<MatchTeam, Integer> highest = null;
        for (MatchTeam matchTeam : TGM.get().getModule(TeamManagerModule.class).getTeams()) {
            if (matchTeam.isSpectator()) continue;
            int score = 0;
            for (WoolObjective woolObjective : this.wools) {
                if (!woolObjective.getOwner().equals(matchTeam)) continue;
                if (woolObjective.isCompleted()) score += 2;
                else if (!woolObjective.getTouches().isEmpty()) score += 1;
            }
            teamScores.put(matchTeam, score);

            if (highest == null) {
                highest = new AbstractMap.SimpleEntry<>(matchTeam, score);
                continue;
            }
            if (score > highest.getValue()) highest = new AbstractMap.SimpleEntry<>(matchTeam, score);
        }
        if (highest != null) {
            final Map.Entry<MatchTeam, Integer> entry = highest;
            int amount = teamScores.entrySet().stream().filter(en -> entry.getValue() == en.getValue()).collect(Collectors.toList()).size();
            if (amount > 1) return null;
            else return entry.getKey();
        }
        return null;
    }

    private void playFireworkEffect(ChatColor color, Location location) {
        FireworkUtil.spawnFirework(location, FireworkEffect.builder()
                .with(FireworkEffect.Type.BURST)
                .withFlicker()
                .trail(false)
                .withColor(ColorConverter.getColor(color))
                .build(), 0);
    }

    @EventHandler
    public void onScoreboardInit(ScoreboardInitEvent event) {
        List<MatchTeam> teams = TGM.get().getModule(TeamManagerModule.class).getTeams();

        int spaceCount = 1;
        int i = 0;
        for (MatchTeam matchTeam : teams) {
            if(matchTeam.isSpectator()) continue;

            for (WoolObjective woolObjective : wools) {
                if (woolObjective.getOwner() == matchTeam) {
                    if (woolScoreboardLines.containsKey(woolObjective)) {
                        woolScoreboardLines.get(woolObjective).add(i);
                    } else {
                        List<Integer> list = new ArrayList<>();
                        list.add(i);
                        woolScoreboardLines.put(woolObjective, list);
                    }

                    event.getSimpleScoreboard().add(getScoreboardString(woolObjective), i++);
                }
            }
            event.getSimpleScoreboard().add(getTeamScoreboardString(matchTeam), i);
            teamScoreboardLines.put(matchTeam, i++);

            if (teams.indexOf(matchTeam) < teams.size() - 1) {
                event.getSimpleScoreboard().add(StringUtils.repeat(" ", spaceCount++), i++);
            }
        }
    }

    public List<WoolObjective> getIncompleteWools(MatchTeam matchTeam) {
        List<WoolObjective> list = new ArrayList<>();
        for (WoolObjective woolObjective : wools) {
            if (woolObjective.getOwner() == matchTeam) {
                if (!woolObjective.isCompleted()) {
                    list.add(woolObjective);
                }
            }
        }
        return list;
    }

    @EventHandler
    public void onTeamUpdate(TeamUpdateEvent event) {
        for (MatchTeam matchTeam : teamScoreboardLines.keySet()) {
            if (event.getMatchTeam() == matchTeam) {
                int i = teamScoreboardLines.get(matchTeam);
                for (SimpleScoreboard simpleScoreboard : TGM.get().getModule(ScoreboardManagerModule.class).getScoreboards().values()) {
                    simpleScoreboard.add(getTeamScoreboardString(matchTeam), i);
                    simpleScoreboard.update();
                }
            }
        }
    }

    private void updateOnScoreboard(WoolObjective woolObjective) {
        ScoreboardManagerModule scoreboardManagerModule = TGM.get().getModule(ScoreboardManagerModule.class);

        for (int i : woolScoreboardLines.get(woolObjective)) {
            for (SimpleScoreboard simpleScoreboard : scoreboardManagerModule.getScoreboards().values()) {
                simpleScoreboard.add(getScoreboardString(woolObjective), i);
                simpleScoreboard.update();
            }
        }
    }

    private String getTeamScoreboardString(MatchTeam matchTeam) {
        return matchTeam.getColor() + matchTeam.getAlias();
    }

    private String getScoreboardString(WoolObjective woolObjective) {
        WoolStatus woolStatus = woolObjective.getStatus();
        if (woolStatus == WoolStatus.COMPLETED) {
            return "  " + woolObjective.getChatColor() + SYMBOL_WOOL_COMPLETE + ChatColor.WHITE + " " + woolObjective.getName();
        } else if (woolStatus == WoolStatus.TOUCHED) {
            return "  " + woolObjective.getChatColor() + SYMBOL_WOOL_TOUCHED + ChatColor.WHITE + " " + woolObjective.getName();
        } else {
            return "  " + woolObjective.getChatColor() + SYMBOL_WOOL_INCOMPLETE + ChatColor.WHITE + " " + woolObjective.getName();
        }
    }

    @Override
    public void unload() {
        for (WoolObjective woolObjective : wools) {
            woolObjective.unload();
        }
    }
}
