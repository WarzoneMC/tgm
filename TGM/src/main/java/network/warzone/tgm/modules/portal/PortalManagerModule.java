package network.warzone.tgm.modules.portal;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.match.ModuleData;
import network.warzone.tgm.match.ModuleLoadTime;
import network.warzone.tgm.modules.region.Region;
import network.warzone.tgm.modules.region.RegionManagerModule;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.util.Parser;
import network.warzone.tgm.util.Strings;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

@ModuleData(load = ModuleLoadTime.EARLY) @Getter
public class PortalManagerModule extends MatchModule {
    private final HashSet<Portal> allPortals = new HashSet<>();
    private final HashMap<String, Portal> identifiablePortals = new HashMap<>();

    @Override
    public void load(Match match) {
        if (match.getMapContainer().getMapInfo().getJsonObject().has("portals")) {
            for (JsonElement portalElement : match.getMapContainer().getMapInfo().getJsonObject().getAsJsonArray("portals")) {
                getPortal(match, portalElement);
            }
        }
    }

    @Override
    public void unload() {
        allPortals.forEach(TGM::unregisterEvents);
        allPortals.clear();
        identifiablePortals.clear();
    }

    public Portal getPortal(Match match, JsonElement jsonElement) {
        if (jsonElement.isJsonPrimitive()) {
            return identifiablePortals.get(jsonElement.getAsString());
        } else {
            JsonObject json = jsonElement.getAsJsonObject();

            boolean active = true;
            if (json.has("active")) {
                active = json.get("active").getAsBoolean();
            }

            Portal.Type type = Portal.Type.ABSOLUTE;
            if (json.has("type")) {
                type = Portal.Type.valueOf(Strings.getTechnicalName(json.get("type").getAsString()));
            }
            Region from = TGM.get().getModule(RegionManagerModule.class).getRegion(match, json.get("from"));
            Location to = Parser.convertLocation(match.getWorld(), json.get("to"));

            List<MatchTeam> teams = new ArrayList<>();
            if (json.has("teams")) {
                for (JsonElement teamElement : json.getAsJsonArray("teams")) {
                    teams.add(TGM.get().getModule(TeamManagerModule.class).getTeamById(teamElement.getAsString()));
                }
            }

            boolean sound = true;
            if (json.has("sound")) {
                sound = json.get("sound").getAsBoolean();
            }

            Portal portal = new Portal(active, type, from, to, teams, sound);
            TGM.registerEvents(portal);
            allPortals.add(portal);
            if (json.has("id")) {
                identifiablePortals.put(json.get("id").getAsString(), portal);
            }

            return portal;
        }
    }
}
