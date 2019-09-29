package network.warzone.tgm.gametype;

import network.warzone.tgm.match.MatchManifest;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.modules.*;
import network.warzone.tgm.modules.border.WorldBorderModule;
import network.warzone.tgm.modules.countdown.CycleCountdown;
import network.warzone.tgm.modules.countdown.StartCountdown;
import network.warzone.tgm.modules.death.DeathMessageModule;
import network.warzone.tgm.modules.death.DeathModule;
import network.warzone.tgm.modules.respawn.RespawnModule;
import network.warzone.tgm.modules.filter.FilterManagerModule;
import network.warzone.tgm.modules.infection.InfectionModule;
import network.warzone.tgm.modules.killstreak.KillstreakModule;
import network.warzone.tgm.modules.kit.KitLoaderModule;
import network.warzone.tgm.modules.knockback.KnockbackModule;
import network.warzone.tgm.modules.points.PointsModule;
import network.warzone.tgm.modules.portal.PortalLoaderModule;
import network.warzone.tgm.modules.region.RegionManagerModule;
import network.warzone.tgm.modules.reports.ReportsModule;
import network.warzone.tgm.modules.scoreboard.ScoreboardManagerModule;
import network.warzone.tgm.modules.tasked.TaskedModuleManager;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.modules.time.TimeModule;
import network.warzone.tgm.modules.visibility.VisibilityModule;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Draem on 7/31/2017.
 */
public class InfectionManifest extends MatchManifest {

    @Override
    public List<MatchModule> allocateGameModules() {
        return new ArrayList<MatchModule>(){
            {
                add(new InfectionModule());
            }
        };
    }

    @Override
    public List<MatchModule> allocateCoreModules() {
        List<MatchModule> modules = super.allocateCoreModules();
        List<MatchModule> toRemove = new ArrayList<>();
        for (MatchModule module : modules) {
            if (module instanceof DeathMessageModule) toRemove.add(module);
        }
        modules.removeAll(toRemove);
        return modules;
    }

}
