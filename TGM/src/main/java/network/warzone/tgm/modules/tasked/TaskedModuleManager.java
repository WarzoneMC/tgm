package network.warzone.tgm.modules.tasked;

import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import org.bukkit.Bukkit;

public class TaskedModuleManager extends MatchModule {
    private int runnableId = -1;

    @Override
    public void load(Match match) {
        runnableId = Bukkit.getScheduler().scheduleSyncRepeatingTask(TGM.get(), new Runnable() {
            @Override
            public void run() {
                for (MatchModule matchModule : match.getModules()) {
                    if (matchModule instanceof TaskedModule) {
                        ((TaskedModule) matchModule).tick();
                    }
                }
            }
        }, 0L, 0L);
    }

    @Override
    public void unload() {
        Bukkit.getScheduler().cancelTask(runnableId);
    }
}
