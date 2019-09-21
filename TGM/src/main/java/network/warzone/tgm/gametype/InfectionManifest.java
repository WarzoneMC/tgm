package network.warzone.tgm.gametype;

import network.warzone.tgm.match.MatchManifest;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.modules.*;
import network.warzone.tgm.modules.border.WorldBorderModule;
import network.warzone.tgm.modules.countdown.CycleCountdown;
import network.warzone.tgm.modules.countdown.StartCountdown;
import network.warzone.tgm.modules.death.DeathModule;
import network.warzone.tgm.modules.death.RespawnModule;
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
        List<MatchModule> modules = new ArrayList<>();

        modules.add(new TeamJoinNotificationsModule());
        modules.add(new SpectatorModule());
        modules.add(new SpawnPointHandlerModule());
        modules.add(new SpawnPointLoaderModule());
        modules.add(new TeamManagerModule());
        modules.add(new VisibilityModule());
        modules.add(new TimeModule());
        modules.add(new TabListModule());
        modules.add(new MatchProgressNotifications());
        modules.add(new MatchResultModule());
        modules.add(new ScoreboardManagerModule());
        modules.add(new RegionManagerModule());
        modules.add(new TaskedModuleManager());
        modules.add(new StartCountdown());
        modules.add(new CycleCountdown());
        modules.add(new KitLoaderModule());
        modules.add(new DeathModule());
        modules.add(new FilterManagerModule());
        modules.add(new ChatModule());
        modules.add(new DisabledCommandsModule());
        modules.add(new PointsModule());
        modules.add(new LegacyDamageModule());
        modules.add(new EntityDamageModule());
        modules.add(new FireworkDamageModule());
        modules.add(new GameRuleModule());
        modules.add(new ItemRemoveModule());
        modules.add(new RegenModule());
        modules.add(new KillstreakModule());
        modules.add(new ReportsModule());
        modules.add(new StatsModule());
        modules.add(new PortalLoaderModule());
        modules.add(new WorldBorderModule());
        modules.add(new KnockbackModule());
        modules.add(new MapCommandsModule());
        modules.add(new RespawnModule());

        return modules;
    }

}
