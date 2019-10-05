package network.warzone.tgm.match;

import com.google.gson.JsonObject;
import network.warzone.tgm.map.MapInfo;
import network.warzone.tgm.modules.*;
import network.warzone.tgm.modules.border.WorldBorderModule;
import network.warzone.tgm.modules.countdown.CycleCountdown;
import network.warzone.tgm.modules.countdown.StartCountdown;
import network.warzone.tgm.modules.damage.DamageControlModule;
import network.warzone.tgm.modules.damage.FireworkDamageModule;
import network.warzone.tgm.modules.death.DeathMessageModule;
import network.warzone.tgm.modules.death.DeathModule;
import network.warzone.tgm.modules.respawn.RespawnModule;
import network.warzone.tgm.modules.filter.FilterManagerModule;
import network.warzone.tgm.modules.killstreak.KillstreakModule;
import network.warzone.tgm.modules.kit.KitLoaderModule;
import network.warzone.tgm.modules.kit.classes.GameClassModule;
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
 * Created by luke on 4/27/17.
 */
public abstract class MatchManifest {

    /**
     * Determines which modules to load based on the
     * given gametype.
     * @return
     */
    public abstract List<MatchModule> allocateGameModules();

    /**
     * Core set of modules that nearly all games will use.
     * Match Manifests still have the option to override these
     * if needed.
     * @return
     */
    public List<MatchModule> allocateCoreModules(JsonObject mapJson) {
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
        modules.add(new DeathMessageModule());
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
        modules.add(new DamageControlModule());
        modules.add(new RespawnModule());

        if (mapJson.has("classes") && ((mapJson.get("classes").isJsonPrimitive() && mapJson.get("classes").getAsJsonPrimitive().isBoolean() && mapJson.get("classes").getAsBoolean()) || mapJson.get("classes").isJsonArray())) {
            modules.add(new GameClassModule());
        }
        return modules;
    }
}
