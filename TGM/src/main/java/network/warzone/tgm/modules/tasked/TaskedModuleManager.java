package network.warzone.tgm.modules.tasked;

import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import org.bukkit.Bukkit;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TaskedModuleManager extends MatchModule {

    private Collection<MatchModule> taskedModules = new ConcurrentLinkedQueue<>();

    private int runnableId;

    @Override
    public void load(Match match) {
        match.getModules().stream().filter(module -> module instanceof TaskedModule).forEach(module -> taskedModules.add(module)); // Cache values so it doesn't constantly search

        runnableId = Bukkit.getScheduler().runTaskTimer(TGM.get(), () -> {
            for (MatchModule matchModule : taskedModules) {
                if (matchModule instanceof TaskedModule) {
                    ((TaskedModule) matchModule).tick();
                }
            }
        }, 1L, 1L).getTaskId();
    }

    @Override
    public void unload() {
        Bukkit.getScheduler().cancelTask(runnableId);
        taskedModules.clear();
    }
}
