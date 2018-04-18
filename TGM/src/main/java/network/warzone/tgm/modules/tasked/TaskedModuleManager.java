package network.warzone.tgm.modules.tasked;

import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import org.bukkit.Bukkit;

import java.util.HashSet;
import java.util.Set;

public class TaskedModuleManager extends MatchModule {

    private Set<MatchModule> taskedModules = new HashSet<>();

    private int runnableId = -1;

    @Override
    public void load(Match match) {
        match.getModules().stream().filter(module -> module instanceof TaskedModule).forEach(module -> taskedModules.add(module)); // Cache values so it doesn't constantly search

        runnableId = Bukkit.getScheduler().runTaskTimer(TGM.get(), () -> taskedModules.forEach(matchModule -> {
            ((TaskedModule) matchModule).tick();
        }), 0L, 0L).getTaskId();
    }

    @Override
    public void unload() {
        Bukkit.getScheduler().cancelTask(runnableId);
    }
}
