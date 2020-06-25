package network.warzone.tgm.modules.filter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.modules.filter.evaluate.AllowFilterEvaluator;
import network.warzone.tgm.modules.filter.evaluate.DenyFilterEvaluator;
import network.warzone.tgm.modules.filter.evaluate.FilterEvaluator;
import network.warzone.tgm.modules.filter.type.*;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class FilterManagerModule extends MatchModule {

    private List<FilterType> filterTypes = new ArrayList<>();
    
    private WeakReference<Match> match;
    
    @Override
    public void load(Match match) {
        this.match = new WeakReference<Match>(match);
    }

    @Override
    public void enable() {
        if (match.get().getMapContainer().getMapInfo().getJsonObject().has("filters")) {
            for (JsonElement filterElement : match.get().getMapContainer().getMapInfo().getJsonObject().getAsJsonArray("filters")) {
                JsonObject filterJson = filterElement.getAsJsonObject();
                for (FilterType filterType : initFilter(match.get(), filterJson)) {
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

        if ("build".equals(type))               filterTypes.add(BuildFilterType.parse(match, jsonObject));
        else if ("enter".equals(type))          filterTypes.add(EnterFilterType.parse(match, jsonObject));
        else if ("usebow".equals(type))         filterTypes.add(UseBowFilterType.parse(match, jsonObject));
        else if ("useshear".equals(type))       filterTypes.add(UseShearFilterType.parse(match, jsonObject));
        else if ("leave".equals(type))          filterTypes.add(LeaveFilterType.parse(match, jsonObject));
        else if ("blockexplode".equals(type))   filterTypes.add(BlockExplodeFilterType.parse(match, jsonObject));
        else if ("blockplace".equals(type))     filterTypes.add(BlockPlaceFilterType.parse(match, jsonObject));
        else if ("blockbreak".equals(type))     filterTypes.add(BlockBreakFilterType.parse(match, jsonObject));
        else if ("voidbuild".equals(type))      filterTypes.add(VoidBuildFilterType.parse(match, jsonObject));

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
