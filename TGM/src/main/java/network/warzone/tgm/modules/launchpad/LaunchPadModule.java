package network.warzone.tgm.modules.launchpad;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import lombok.Getter;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.modules.region.Region;
import network.warzone.tgm.modules.region.RegionManagerModule;
import network.warzone.tgm.modules.tasked.TaskedModule;
import network.warzone.tgm.modules.tasked.TaskedModuleManager;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamChangeEvent;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.util.Parser;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Created by Jorge on 10/08/2019
 */
@Getter
public class LaunchPadModule extends MatchModule implements TaskedModule, Listener {

    private Region region;
    private int delay; // Ticks
    private Vector motion;
    private boolean directional;
    private List<MatchTeam> teams;

    private final HashMap<Player, Long> standingOnPadDate = new HashMap<>();

    private Match match;
    private final TeamManagerModule teamManagerModule;

    private LaunchPadModule(Region region, int delay, Vector motion, boolean directional, List<MatchTeam> teams) {
        this.region = region;
        this.delay = delay;
        this.motion = motion;
        this.directional = directional;
        this.teams = teams;
        this.teamManagerModule = TGM.get().getModule(TeamManagerModule.class);
    }

    @Override
    public void load(Match match) {
        this.match = match;
        if (isDelayed()) TGM.get().getModule(TaskedModuleManager.class).addTaskedModule(this);
        TGM.registerEvents(this);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isDifferentBlock(event)) return;
        if (region.contains(event.getTo().getBlock()) && !region.contains(event.getFrom().getBlock())) {
            if (!allowedTeam(event.getPlayer())) return;
            if (isDelayed()) standingOnPadDate.put(event.getPlayer(), getTime());
            else launch(event.getPlayer());
        } else if (region.contains(event.getFrom().getBlock())
                && !region.contains(event.getTo().getBlock())
                && allowedTeam(event.getPlayer())) {
            standingOnPadDate.remove(event.getPlayer());
        }
    }

    private boolean isDifferentBlock(PlayerMoveEvent event) {
        Block from = event.getFrom().getBlock();
        Block to = event.getTo().getBlock();
        return from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ();
    }

    @Override
    public void tick() {
        for (Map.Entry<Player, Long> entry : standingOnPadDate.entrySet()) {
            if (getTime() >= entry.getValue() + (delay * 50)) {
                launch(entry.getKey());
                standingOnPadDate.remove(entry.getKey());
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        removePlayer(event.getPlayer());
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        removePlayer(event.getPlayer());
    }

    @EventHandler
    public void onTeamChange(TeamChangeEvent event) {
        if (!event.isCancelled()) removePlayer(event.getPlayerContext().getPlayer());
    }

    private void removePlayer(Player player) {
        this.standingOnPadDate.remove(player);
    }

    private void launch(Player player) {
        if (this.directional) {
            Vector d = player.getEyeLocation().getDirection().clone().multiply(this.motion);
            player.setVelocity(d);
        } else {
            player.setVelocity(this.motion);
        }
    }

    private long getTime() {
        return new Date().getTime();
    }

    private boolean isDelayed() {
        return delay > 0;
    }

    private boolean allowedTeam(Player player) {
        if (this.teams == null || this.teams.isEmpty()) return true;
        MatchTeam team = this.teamManagerModule.getTeam(player);
        return team.isSpectator() || this.teams.contains(team);
    }

    public static LaunchPadModule deserialize(JsonObject jsonObject) {
        Preconditions.checkArgument(jsonObject.has("region"), "Launch Pad requires a region.");
        Preconditions.checkArgument(jsonObject.has("motion"), "Launch Pad requires a motion direction.");
        Region region = TGM.get().getModule(RegionManagerModule.class).getRegion(TGM.get().getMatchManager().getMatch(), jsonObject.get("region"));
        Vector motion = Parser.convertVector(jsonObject.get("motion"));
        int delay = 0;
        boolean directional = true;
        List<MatchTeam> teams = new ArrayList<>();
        if (jsonObject.has("delay")) delay = jsonObject.get("delay").getAsInt();
        if (jsonObject.has("directional")) directional = jsonObject.get("directional").getAsBoolean();
        if (jsonObject.has("teams")) {
            TeamManagerModule teamManagerModule = TGM.get().getModule(TeamManagerModule.class);
            teams.addAll(teamManagerModule.getTeams(jsonObject.getAsJsonArray("teams")));
        }
        return new LaunchPadModule(region, delay, motion, directional, teams);
    }

}
