package com.minehut.tgm.gametype;

import com.minehut.tgm.match.MatchManifest;
import com.minehut.tgm.match.MatchModule;
import com.minehut.tgm.modules.*;
import com.minehut.tgm.modules.countdown.CycleCountdown;
import com.minehut.tgm.modules.countdown.StartCountdown;
import com.minehut.tgm.modules.filter.FilterManagerModule;
import com.minehut.tgm.modules.infection.InfectionModule;
import com.minehut.tgm.modules.kit.KitLoaderModule;
import com.minehut.tgm.modules.points.PointsModule;
import com.minehut.tgm.modules.region.RegionManagerModule;
import com.minehut.tgm.modules.scoreboard.ScoreboardManagerModule;
import com.minehut.tgm.modules.tasked.TaskedModuleManager;
import com.minehut.tgm.modules.team.TeamManagerModule;
import com.minehut.tgm.modules.visibility.VisibilityModule;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Draem on 7/31/2017.
 */
public class InfectionManifest extends MatchManifest {

    @Override
    public List<MatchModule> allocateGameModules() {
        List<MatchModule> matchModules = new ArrayList<>();

        matchModules.add(new InfectionModule());
        return matchModules;
    }

    @Override
    public List<MatchModule> allocateCoreModules() {
        List<MatchModule> modules = new ArrayList<>();

        modules.add(new TeamJoinNotificationsModule());
        modules.add(new SpectatorModule());
        modules.add(new SpawnPointHandlerModule());
        modules.add(new SpawnPointLoaderModule());
        modules.add(new VisibilityModule());
        modules.add(new TimeModule());
        modules.add(new TabListModule());
        modules.add(new MatchProgressNotifications());
        modules.add(new MatchResultModule());
        modules.add(new TeamManagerModule());
        modules.add(new ScoreboardManagerModule());
        modules.add(new RegionManagerModule());
        modules.add(new TaskedModuleManager());
        modules.add(new StartCountdown());
        modules.add(new CycleCountdown());
        modules.add(new KitLoaderModule());
        modules.add(new DeathModule());
        //modules.add(new DeathMessageModule());
        modules.add(new FilterManagerModule());
        modules.add(new ChatModule());
        modules.add(new DisabledCommandsModule());
        modules.add(new PointsModule());
        modules.add(new LegacyDamageModule());
        modules.add(new FireworkDamageModule());
        modules.add(new GameRuleModule());
        modules.add(new ItemRemoveModule());

        return modules;
    }

}
