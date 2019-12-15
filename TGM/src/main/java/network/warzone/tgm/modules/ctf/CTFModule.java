package network.warzone.tgm.modules.ctf;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.modules.base.MatchBase;
import network.warzone.tgm.modules.base.Redeemable;
import network.warzone.tgm.modules.flag.FlagSubscriber;
import network.warzone.tgm.modules.flag.MatchFlag;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.util.Parser;
import network.warzone.tgm.util.Strings;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yikes on 12/15/2019
 */
public class CTFModule extends MatchModule implements FlagSubscriber {
    private CTFObjective objective;

    private List<MatchBase> matchBases = new ArrayList<>();
    private List<MatchFlag> matchFlags = new ArrayList<>();

    @Override
    public void load(Match match) {
        JsonObject ctfJson = match.getMapContainer().getMapInfo().getJsonObject().get("ctf").getAsJsonObject();

        // Deserialize flags json into MatchFlag instances
        World world = match.getWorld();

        for (JsonElement flagElem : ctfJson.get("flags").getAsJsonArray()) {
            this.matchFlags.add(MatchFlag.deserialize(flagElem.getAsJsonObject(), this, world));
        }

        TeamManagerModule teamManagerModule = TGM.get().getModule(TeamManagerModule.class);
        for (JsonElement element : ctfJson.get("bases").getAsJsonArray()) {
            JsonObject baseObject = element.getAsJsonObject();
            Location baseLocation = Parser.convertLocation(world, baseObject.get("location"));
            MatchTeam matchTeam = teamManagerModule.getTeamById(baseObject.get("team").getAsString());
            List<MatchFlag> flags = new ArrayList<>();
            for (MatchFlag flag : matchFlags) {
                if (flag.getTeam().equals(matchTeam)) continue;
                flags.add(flag);
            }
            matchBases.add(new MatchBase(baseLocation, flags));
        }

        // CTF Objective
        this.objective = CTFObjective.valueOf(Strings.getTechnicalName(ctfJson.get("objective").getAsString()));
    }

    @Override
    public void pickup(MatchFlag flag, Player stealer) {

    }

    @Override
    public void drop(MatchFlag flag, Player stealer, Player attacker) {

    }

    @Override
    public void capture(MatchFlag flag, Player capturer) {

    }

    @Override
    public void disable() {
        for (MatchFlag matchFlag : matchFlags) matchFlag.unload();
        for (MatchBase matchBase : matchBases) matchBase.unload();
        matchFlags = null;
        matchBases = null;
    }
}
