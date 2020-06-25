package network.warzone.tgm.modules.ctw;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.modules.ItemRemoveModule;
import network.warzone.tgm.modules.region.Region;
import network.warzone.tgm.modules.region.RegionManagerModule;
import network.warzone.tgm.modules.scoreboard.ScoreboardInitEvent;
import network.warzone.tgm.modules.scoreboard.ScoreboardManagerModule;
import network.warzone.tgm.modules.scoreboard.SimpleScoreboard;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.modules.team.TeamUpdateEvent;
import network.warzone.tgm.modules.time.TimeModule;
import network.warzone.tgm.modules.wool.WoolObjective;
import network.warzone.tgm.modules.wool.WoolObjectiveService;
import network.warzone.tgm.modules.wool.WoolStatus;
import network.warzone.tgm.user.PlayerContext;
import network.warzone.tgm.util.ColorConverter;
import network.warzone.tgm.util.FireworkUtil;
import network.warzone.tgm.util.Strings;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class CTWModule extends MatchModule implements Listener {

    private static final String SYMBOL_WOOL_INCOMPLETE = "\u2b1c";  // ⬜
    private static final String SYMBOL_WOOL_TOUCHED = "\u2592";     // ▒
    private static final String SYMBOL_WOOL_COMPLETE = "\u2b1b";    // ⬛

    private final List<WoolObjective> wools = new ArrayList<>();
    private final HashMap<WoolObjective, List<Integer>> woolScoreboardLines = new HashMap<>();
    private final HashMap<String, Integer> teamScoreboardLines = new HashMap<>();

    private boolean compactLayout = false;

    private TeamManagerModule teamManagerModule;
    private ScoreboardManagerModule scoreboardManagerModule;

    @Override
    public void load(Match match) {
        JsonObject dtmJson = match.getMapContainer().getMapInfo().getJsonObject().get("ctw").getAsJsonObject();
        this.teamManagerModule = match.getModule(TeamManagerModule.class);
        this.scoreboardManagerModule = match.getModule(ScoreboardManagerModule.class);

        for (JsonElement woolElement : dtmJson.getAsJsonArray("wools")) {
            JsonObject woolObject = woolElement.getAsJsonObject();

            String name = woolObject.get("name").getAsString();
            Region region = match.getModule(RegionManagerModule.class).getRegion(match, woolObject.get("region"));
            List<MatchTeam> teams = this.teamManagerModule.getTeams(woolObject.get("teams").getAsJsonArray());
            String woolColor = Strings.getTechnicalName(woolObject.get("woolcolor").getAsString()); //TODO 1.13 Temp fix
            ChatColor color = ChatColor.valueOf(Strings.getTechnicalName(woolObject.get("color").getAsString()));
            for (MatchTeam matchTeam : teams) {
                wools.add(new WoolObjective(name, Material.valueOf(woolColor.toUpperCase() + "_WOOL"), matchTeam, region, color));
            }

            teams.clear();
        }

        //wool services
        for (WoolObjective woolObjective : wools) {
            woolObjective.addService(new WoolObjectiveService() {
                @Override
                public void pickup(Player player, MatchTeam matchTeam, boolean firstTouch) {
                    if (firstTouch) {
                        updateOnScoreboard(woolObjective);

                        Bukkit.broadcastMessage(matchTeam.getColor() + player.getName() + ChatColor.WHITE +
                                " picked up " + woolObjective.getColor() + ChatColor.BOLD.toString() + woolObjective.getName());

                        for (MatchTeam otherTeam : teamManagerModule.getTeams()) {
                            for (PlayerContext playerContext : otherTeam.getMembers()) {
                                if (otherTeam.isSpectator() || otherTeam.equals(matchTeam)) {
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
                            " placed " + woolObjective.getColor() + ChatColor.BOLD.toString() + woolObjective.getName());

                    for (MatchTeam otherTeam : teamManagerModule.getTeams()) {
                        for (PlayerContext playerContext : otherTeam.getMembers()) {
                            if (otherTeam.isSpectator() || otherTeam.equals(matchTeam)) {
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
                             " dropped " + woolObjective.getColor() + ChatColor.BOLD.toString() + woolObjective.getName());
                 }

            });
        }

        ItemRemoveModule module = TGM.get().getModule(ItemRemoveModule.class);

        //load wools
        for (WoolObjective woolObjective : this.wools) {
            woolObjective.load();
            if (module != null) module.add(new ItemRemoveModule.ItemRemoveInfo(woolObjective.getBlock()).setPreventingItemSpawn(false));
        }

        if (this.wools.size() > 6) this.compactLayout = true;
        TGM.get().getModule(TimeModule.class).setTimeLimitService(this::getWinningTeam);
    }

    private MatchTeam getWinningTeam() {
        Map<MatchTeam, Integer> teamScores = new HashMap<>();
        Map.Entry<MatchTeam, Integer> highest = null;
        for (MatchTeam matchTeam : this.teamManagerModule.getTeams()) {
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
            int amount = (int) teamScores.entrySet().stream().filter(en -> entry.getValue().equals(en.getValue())).count();
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
        List<MatchTeam> teams = this.teamManagerModule.getTeams();
        int spaceCount = 1;
        int i = 2;
        if (!compactLayout) {
            for (MatchTeam matchTeam : teams) {
                if (matchTeam.isSpectator()) continue;

                for (WoolObjective woolObjective : wools) {
                    if (woolObjective.getOwner().equals(matchTeam)) {
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
                teamScoreboardLines.put(matchTeam.getId(), i++);

                if (teams.indexOf(matchTeam) < teams.size() - 1) {
                    event.getSimpleScoreboard().add(StringUtils.repeat(" ", spaceCount++), i++);
                }
            }
        } else {
            for (MatchTeam matchTeam : teams) {
                if (matchTeam.isSpectator()) continue;

                List<WoolObjective> wools = getTeamWoolObjectives(matchTeam);
                for (WoolObjective woolObjective : wools) {
                    if (woolObjective.getOwner().equals(matchTeam)) {
                        if (woolScoreboardLines.containsKey(woolObjective)) {
                            woolScoreboardLines.get(woolObjective).add(i);
                        } else {
                            List<Integer> list = new ArrayList<>();
                            list.add(i);
                            woolScoreboardLines.put(woolObjective, list);
                        }
                    }
                }
                event.getSimpleScoreboard().add(getScoreboardString(wools), i++);
                event.getSimpleScoreboard().add(getTeamScoreboardString(matchTeam), i);
                teamScoreboardLines.put(matchTeam.getId(), i++);

                if (teams.indexOf(matchTeam) < teams.size() - 1) {
                    event.getSimpleScoreboard().add(StringUtils.repeat(" ", spaceCount++), i++);
                }
            }
        }
    }

    public List<WoolObjective> getIncompleteWools(MatchTeam matchTeam) {
        List<WoolObjective> list = new ArrayList<>();
        for (WoolObjective woolObjective : wools) {
            if (woolObjective.getOwner().equals(matchTeam) && !woolObjective.isCompleted()) {
                list.add(woolObjective);
            }
        }
        return list;
    }

    @EventHandler
    public void onTeamUpdate(TeamUpdateEvent event) {
        Set<String> teamIds = this.teamScoreboardLines.keySet();
        Set<MatchTeam> teams = teamIds.stream().map(id -> this.teamManagerModule.getTeamById(id)).collect(Collectors.toSet());

        for (MatchTeam matchTeam : teams) {
            if (event.getMatchTeam().equals(matchTeam)) {
                int i = this.teamScoreboardLines.get(matchTeam.getId());
                for (SimpleScoreboard simpleScoreboard : this.scoreboardManagerModule.getScoreboards().values()) {
                    simpleScoreboard.add(getTeamScoreboardString(matchTeam), i);
                    simpleScoreboard.update();
                }
            }
        }
    }

    private void updateOnScoreboard(WoolObjective woolObjective) {
        if (!this.compactLayout) {
            for (int i : this.woolScoreboardLines.get(woolObjective)) {
                for (SimpleScoreboard simpleScoreboard : this.scoreboardManagerModule.getScoreboards().values()) {
                    simpleScoreboard.add(getScoreboardString(woolObjective), i);
                    simpleScoreboard.update();
                }
            }
        } else {
            List<WoolObjective> woolObjectives = getTeamWoolObjectives(woolObjective.getOwner());
            for (int i : this.woolScoreboardLines.get(woolObjective)) {
                for (SimpleScoreboard simpleScoreboard : this.scoreboardManagerModule.getScoreboards().values()) {
                    simpleScoreboard.add(getScoreboardString(woolObjectives), i);
                    simpleScoreboard.update();
                }
            }
        }
    }

    private String getTeamScoreboardString(MatchTeam matchTeam) {
        return matchTeam.getColor() + matchTeam.getAlias();
    }

    private String getScoreboardString(WoolObjective woolObjective) {
        WoolStatus woolStatus = woolObjective.getStatus();
        if (woolStatus == WoolStatus.COMPLETED) {
            return "  " + woolObjective.getColor() + SYMBOL_WOOL_COMPLETE + ChatColor.WHITE + " " + woolObjective.getName();
        } else if (woolStatus == WoolStatus.TOUCHED) {
            return "  " + woolObjective.getColor() + SYMBOL_WOOL_TOUCHED + ChatColor.WHITE + " " + woolObjective.getName();
        } else {
            return "  " + woolObjective.getColor() + SYMBOL_WOOL_INCOMPLETE + ChatColor.WHITE + " " + woolObjective.getName();
        }
    }

    private String getScoreboardString(List<WoolObjective> woolObjectives) {
        StringBuilder result = new StringBuilder();
        for (WoolObjective woolObjective : woolObjectives) {
            WoolStatus woolStatus = woolObjective.getStatus();
            if (woolStatus == WoolStatus.COMPLETED) {
                result.append("  ").append(woolObjective.getColor()).append(SYMBOL_WOOL_COMPLETE);
            } else if (woolStatus == WoolStatus.TOUCHED) {
                result.append("  ").append(woolObjective.getColor()).append(SYMBOL_WOOL_TOUCHED);
            } else {
                result.append("  ").append(woolObjective.getColor()).append(SYMBOL_WOOL_INCOMPLETE);
            }
        }
        return result.toString();
    }

    @Override
    public void unload() {
        for (WoolObjective woolObjective : this.wools) {
            woolObjective.unload();
        }

        this.wools.clear();
        this.woolScoreboardLines.clear();
        this.teamScoreboardLines.clear();
    }

    private List<WoolObjective> getTeamWoolObjectives(MatchTeam matchTeam) {
        return this.wools.stream().filter(wool -> wool.getOwner().equals(matchTeam)).collect(Collectors.toList());
    }
}
