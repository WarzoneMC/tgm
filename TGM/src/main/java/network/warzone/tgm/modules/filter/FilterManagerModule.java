package network.warzone.tgm.modules.filter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.md_5.bungee.api.ChatColor;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.modules.filter.evaluate.AllowFilterEvaluator;
import network.warzone.tgm.modules.filter.evaluate.DenyFilterEvaluator;
import network.warzone.tgm.modules.filter.evaluate.FilterEvaluator;
import network.warzone.tgm.modules.filter.type.*;
import network.warzone.tgm.modules.region.Region;
import network.warzone.tgm.modules.region.RegionManagerModule;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.util.Parser;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

public class FilterManagerModule extends MatchModule {

    private List<FilterType> filterTypes = new ArrayList<>();
    
    private Match match;
    
    @Override
    public void load(Match match) {
        this.match = match;
    }

    @Override
    public void enable() {
        if (match.getMapContainer().getMapInfo().getJsonObject().has("filters")) {
            for (JsonElement filterElement : match.getMapContainer().getMapInfo().getJsonObject().getAsJsonArray("filters")) {
                JsonObject filterJson = filterElement.getAsJsonObject();
                for (FilterType filterType : initFilter(match, filterJson)) {
                    filterTypes.add(filterType);
                    if (filterType instanceof Listener) {
                        TGM.registerEvents((Listener) filterType);
                    }
                }
            }
        }
    }

    @Override
    public void disable() {
        for (FilterType filterType : filterTypes) {
            if (filterType instanceof Listener) {
                HandlerList.unregisterAll((Listener) filterType);
            }
        }
        filterTypes.clear();
    }

    private List<FilterType> initFilter(Match match, JsonObject jsonObject) {
        List<FilterType> filterTypes = new ArrayList<>();

        String type = jsonObject.get("type").getAsString()
                .replace(" ", "")
                .replace("_", "")
                .replace("-", "")
                .toLowerCase();

        if (type.equals("build"))               filterTypes.add(BuildFilterType.parse(match, jsonObject));
        else if (type.equals("enter"))          filterTypes.add(EnterFilterType.parse(match, jsonObject));
        else if (type.equals("usebow"))         filterTypes.add(UseBowFilterType.parse(match, jsonObject));
        else if (type.equals("useshear"))       filterTypes.add(UseShearFilterType.parse(match, jsonObject));
        else if (type.equals("leave"))          filterTypes.add(LeaveFilterType.parse(match, jsonObject));
        else if (type.equals("blockexplode"))   filterTypes.add(BlockExplodeFilterType.parse(match, jsonObject));
        else if (type.equals("blockplace"))     filterTypes.add(BlockPlaceFilterType.parse(match, jsonObject));
        else if (type.equals("blockbreak"))     filterTypes.add(BlockBreakFilterType.parse(match, jsonObject));

        return filterTypes;
    }

    public static FilterEvaluator initEvaluator(Match match, JsonObject parent) {
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
