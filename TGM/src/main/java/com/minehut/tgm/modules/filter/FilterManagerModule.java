package com.minehut.tgm.modules.filter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minehut.tgm.TGM;
import com.minehut.tgm.match.Match;
import com.minehut.tgm.match.MatchModule;
import com.minehut.tgm.modules.filter.evaluate.AllowFilterEvaluator;
import com.minehut.tgm.modules.filter.evaluate.DenyFilterEvaluator;
import com.minehut.tgm.modules.filter.evaluate.FilterEvaluator;
import com.minehut.tgm.modules.filter.type.BlockExplodeFilterType;
import com.minehut.tgm.modules.filter.type.BuildFilterType;
import com.minehut.tgm.modules.filter.type.EnterFilterType;
import com.minehut.tgm.modules.filter.type.FilterType;
import com.minehut.tgm.modules.region.Region;
import com.minehut.tgm.modules.region.RegionManagerModule;
import com.minehut.tgm.modules.team.MatchTeam;
import com.minehut.tgm.modules.team.TeamManagerModule;
import com.minehut.tgm.util.Parser;
import com.sk89q.minecraft.util.commands.ChatColor;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

public class FilterManagerModule extends MatchModule {
    private List<FilterType> filterTypes = new ArrayList<>();

    @Override
    public void load(Match match) {
        if (match.getMapContainer().getMapInfo().getJsonObject().has("filters")) {
            for (JsonElement filterElement : match.getMapContainer().getMapInfo().getJsonObject().getAsJsonArray("filters")) {
                JsonObject filterJson = filterElement.getAsJsonObject();
                for (FilterType filterType : initFilter(match, filterJson)) {
                    filterTypes.add(filterType);
                    if (filterType instanceof Listener) {
                        TGM.get().registerEvents((Listener) filterType);
                    }
                }
            }
        }
    }

    @Override
    public void unload() {
        for (FilterType filterType : filterTypes) {
            if (filterType instanceof Listener) {
                HandlerList.unregisterAll((Listener) filterType);
            }
        }
    }

    private List<FilterType> initFilter(Match match, JsonObject jsonObject) {
        List<FilterType> filterTypes = new ArrayList<>();

        String type = jsonObject.get("type").getAsString().toLowerCase();

        if (type.equals("build")) {
            List<MatchTeam> matchTeams = Parser.getTeamsFromElement(match.getModule(TeamManagerModule.class), jsonObject.get("teams"));
            List<Region> regions = new ArrayList<>();

            for (JsonElement regionElement : jsonObject.getAsJsonArray("regions")) {
                Region region = match.getModule(RegionManagerModule.class).getRegion(match, regionElement);
                if (region != null) {
                    regions.add(region);
                }
            }

            FilterEvaluator filterEvaluator = initEvaluator(match, jsonObject);
            String message = ChatColor.translateAlternateColorCodes('&', jsonObject.get("message").getAsString());

            filterTypes.add(new BuildFilterType(matchTeams, regions, filterEvaluator, message));
        } else if (type.equals("enter")) {
            List<MatchTeam> matchTeams = Parser.getTeamsFromElement(match.getModule(TeamManagerModule.class), jsonObject.get("teams"));
            List<Region> regions = new ArrayList<>();

            for (JsonElement regionElement : jsonObject.getAsJsonArray("regions")) {
                Region region = match.getModule(RegionManagerModule.class).getRegion(match, regionElement);
                if (region != null) {
                    regions.add(region);
                }
            }

            FilterEvaluator filterEvaluator = initEvaluator(match, jsonObject);
            String message = ChatColor.translateAlternateColorCodes('&', jsonObject.get("message").getAsString());

            filterTypes.add(new EnterFilterType(matchTeams, regions, filterEvaluator, message));
        } else if (type.equals("blockexplode")) {
            List<Region> regions = new ArrayList<>();
            for (JsonElement regionElement : jsonObject.getAsJsonArray("regions")) {
                Region region = match.getModule(RegionManagerModule.class).getRegion(match, regionElement);
                if (region != null) {
                    regions.add(region);
                }
            }

            FilterEvaluator filterEvaluator = initEvaluator(match, jsonObject);
            filterTypes.add(new BlockExplodeFilterType(regions, filterEvaluator));
        }

        return filterTypes;
    }

    private FilterEvaluator initEvaluator(Match match, JsonObject parent) {
        switch (parent.get("evaluate").getAsString()) {
            case "allow":
                return new AllowFilterEvaluator();
            case "deny":
                return new DenyFilterEvaluator();
            default:
                return null;
        }
    }
}
