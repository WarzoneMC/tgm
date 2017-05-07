package com.minehut.tgm.modules.ctw;

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
import com.minehut.tgm.modules.team.MatchTeam;
import com.minehut.tgm.modules.team.TeamManagerModule;
import com.minehut.tgm.modules.wool.WoolObjective;
import com.minehut.tgm.modules.wool.WoolObjectiveService;
import com.minehut.tgm.modules.wool.WoolStatus;
import com.minehut.tgm.util.Parser;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CTWModule extends MatchModule {
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

        for (JsonElement monumentElement : dtmJson.getAsJsonArray("monuments")) {
            JsonObject monumentJson = monumentElement.getAsJsonObject();

            String name = monumentJson.get("name").getAsString();
            Region region = match.getModule(RegionManagerModule.class).getRegion(match, monumentJson.get("region"));
            List<MatchTeam> teams = Parser.getTeamsFromElement(match.getModule(TeamManagerModule.class), monumentJson.get("teams"));
            byte color = DyeColor.valueOf(monumentJson.get("color").getAsString()).getDyeData();

            for (MatchTeam matchTeam : teams) {
                wools.add(new WoolObjective(name, color, matchTeam, region, false));
            }
        }

        TeamManagerModule teamManagerModule = TGM.get().getModule(TeamManagerModule.class);

        //monument services
        for (WoolObjective woolObjective : wools) {
            woolObjective.addService(new WoolObjectiveService() {
                @Override
                public void pickup(Player player, MatchTeam matchTeam) {

                }

                @Override
                public void place(Player player, MatchTeam matchTeam) {

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

                    event.getSimpleScoreboard().add(getScoreboardString(monument), i);

                    i++;
                }
            }
            event.getSimpleScoreboard().add(getTeamScoreboardString(matchTeam), i);
            teamScoreboardLines.put(matchTeam, i);
            i++;

            if (teams.indexOf(matchTeam) < teams.size() - 1) {
                event.getSimpleScoreboard().add(StringUtils.repeat(" ", spaceCount), i);
                i++; spaceCount++;
            }
        }
    }

    private String getScoreboardString(WoolObjective woolObjective) {
        WoolStatus woolStatus = woolObjective.getStatus();
        if (woolStatus == WoolStatus.COMPLETED) {

        }
    }

    private void updateOnScoreboard()
}
