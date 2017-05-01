package com.minehut.tgm.modules.tasked;

import com.minehut.tgm.TGM;
import com.minehut.tgm.match.Match;
import com.minehut.tgm.match.MatchModule;
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
