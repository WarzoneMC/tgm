package network.warzone.tgm.modules.killstreak;

import org.bukkit.entity.Player;

import network.warzone.tgm.TGM;
import network.warzone.tgm.modules.team.TeamManagerModule;

public interface KillstreakAction {
    default void safeApply(Player killer) {
        if (ignoreAction(killer)) return;
        this.apply(killer);
    }

    void apply(Player killer);

    default boolean ignoreAction(Player killer) {
        return TGM.get().getModule(TeamManagerModule.class).getTeam(killer).isSpectator();
    }
}
