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

public class PortalLoaderModule extends MatchModule {

    @Override
    public void load(Match match) {
        if (match.getMapContainer().getMapInfo().getJsonObject().has("portals")) {
            for (JsonElement portalElement : match.getMapContainer().getMapInfo().getJsonObject().getAsJsonArray("portals")) {
                JsonObject json = portalElement.getAsJsonObject();

                Region from = TGM.get().getModule(RegionManagerModule.class).getRegion(match, json.get("from"));
                Location to = Parser.convertLocation(match.getWorld(), json.get("to"));

                MatchTeam team = null;
                if (json.has("team")) {
                    team = TGM.get().getModule(TeamManagerModule.class).getTeamById(json.get("team").getAsString());
                }

                boolean sound = true;
                if (json.has("sound")) {
                    sound = json.get("sound").getAsBoolean();
                }

                match.getModules().add(new PortalModule(from, to, team, sound));
            }
        }
    }
}
