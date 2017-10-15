package network.warzone.tgm.modules.portal;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.modules.region.Region;
import network.warzone.tgm.modules.region.RegionManagerModule;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.util.Parser;
import network.warzone.tgm.util.Strings;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class PortalLoaderModule extends MatchModule {

    @Override
    public void load(Match match) {
        if (match.getMapContainer().getMapInfo().getJsonObject().has("portals")) {
            for (JsonElement portalElement : match.getMapContainer().getMapInfo().getJsonObject().getAsJsonArray("portals")) {
                JsonObject json = portalElement.getAsJsonObject();

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

                PortalModule portalModule = new PortalModule(from, to, teams, sound);
                match.getModules().add(portalModule);
                TGM.registerEvents(portalModule);
            }
        }
    }
}
