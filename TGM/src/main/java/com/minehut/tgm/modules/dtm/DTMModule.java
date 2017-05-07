package com.minehut.tgm.modules.dtm;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minehut.tgm.TGM;
import com.minehut.tgm.match.Match;
import com.minehut.tgm.match.MatchModule;
import com.minehut.tgm.modules.monument.Monument;
import com.minehut.tgm.modules.monument.MonumentService;
import com.minehut.tgm.modules.region.Region;
import com.minehut.tgm.modules.region.RegionManagerModule;
import com.minehut.tgm.modules.scoreboard.ScoreboardInitEvent;
import com.minehut.tgm.modules.scoreboard.ScoreboardManagerModule;
import com.minehut.tgm.modules.scoreboard.SimpleScoreboard;
import com.minehut.tgm.modules.team.MatchTeam;
import com.minehut.tgm.modules.team.TeamManagerModule;
import com.minehut.tgm.util.Parser;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DTMModule extends MatchModule implements Listener {
    @Getter
    private final List<Monument> monuments = new ArrayList<>();

    @Getter
    private final HashMap<Monument, List<Integer>> monumentScoreboardLines = new HashMap<>();

    @Override
    public void load(Match match) {
        JsonObject dtmJson = match.getMapContainer().getMapInfo().getJsonObject().get("dtm").getAsJsonObject();

        for (JsonElement monumentElement : dtmJson.getAsJsonArray("monuments")) {
            JsonObject monumentJson = monumentElement.getAsJsonObject();

            String name = monumentJson.get("name").getAsString();
            Region region = match.getModule(RegionManagerModule.class).getRegion(match, monumentJson.get("region"));
            List<MatchTeam> teams = Parser.getTeamsFromElement(match.getModule(TeamManagerModule.class), monumentJson.get("teams"));
            List<Material> materials = Parser.getMaterialsFromElement(monumentJson.get("materials"));
            int health = monumentJson.get("health").getAsInt();

            monuments.add(new Monument(name, teams, region, materials, health, health));
        }

        //monument services
        for (Monument monument : monuments) {
            monument.addService(new MonumentService() {
                @Override
                public void damage(Player player) {
                    updateOnScoreboard(monument);
                }

                @Override
                public void destroy(Player player) {
                    updateOnScoreboard(monument);

                    TeamManagerModule teamManagerModule = TGM.get().getModule(TeamManagerModule.class);
                    MatchTeam matchTeam = teamManagerModule.getTeam(player);
                    if (getAliveMonuments(matchTeam).size() == 0) {

                        //find enemy team and set them as winners
                        for (MatchTeam otherTeam : teamManagerModule.getTeams()) {
                            if (!otherTeam.isSpectator() && otherTeam != matchTeam) {
                                TGM.get().getMatchManager().endMatch(otherTeam);
                            }
                        }
                    }
                }
            });
        }

        //load monuments
        for (Monument monument : monuments) {
            monument.load();
        }
    }

    @EventHandler
    public void onScoreboardInit(ScoreboardInitEvent event) {
        List<MatchTeam> teams = TGM.get().getModule(TeamManagerModule.class).getTeams();

        int i = 0;
        for (MatchTeam matchTeam : teams) {
            if(matchTeam.isSpectator()) continue;

            for (Monument monument : monuments) {
                if (monument.getOwners().contains(matchTeam)) {
                    if (monumentScoreboardLines.containsKey(monument)) {
                        monumentScoreboardLines.get(monument).add(i);
                    } else {
                        monumentScoreboardLines.put(monument, Arrays.asList(i));
                    }

                    event.getSimpleScoreboard().add(getScoreboardLine(monument), i);

                    i++;
                }
            }
            event.getSimpleScoreboard().add(matchTeam.getColor() + matchTeam.getAlias(), i);
            i++;
        }
    }

    private void updateOnScoreboard(Monument monument) {
        ScoreboardManagerModule scoreboardManagerModule = TGM.get().getModule(ScoreboardManagerModule.class);

        for (int i : monumentScoreboardLines.get(monument)) {
            for (SimpleScoreboard simpleScoreboard : scoreboardManagerModule.getScoreboards().values()) {
                simpleScoreboard.remove(i);
                simpleScoreboard.add(getScoreboardLine(monument), i);
                simpleScoreboard.update();
            }
        }
    }

    private String getScoreboardLine(Monument monument) {
        if (monument.isAlive()) {
            int percentage = monument.getHealthPercentage();

            if (percentage > 70) {
                return ChatColor.YELLOW.toString() + percentage + "% " + ChatColor.WHITE + monument.getName();
            } else if (percentage > 40) {
                return ChatColor.YELLOW.toString() + percentage + "% " + ChatColor.WHITE + monument.getName();
            } else {
                return ChatColor.RED.toString() + percentage + "% " + ChatColor.WHITE + monument.getName();
            }
        } else {
            return ChatColor.RED.toString() + ChatColor.STRIKETHROUGH + monument.getName();
        }
    }

    public List<Monument> getAliveMonuments(MatchTeam matchTeam) {
        List<Monument> alive = new ArrayList<>();
        for (Monument monument : monuments) {
            if (monument.isAlive() && monument.getOwners().contains(matchTeam)) {
                alive.add(monument);
            }
        }
        return alive;
    }

    @Override
    public void unload() {
        for (Monument monument : monuments) {
            monument.unload();
        }
    }
}
