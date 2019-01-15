package network.warzone.tgm.modules;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import network.warzone.tgm.match.*;
import network.warzone.tgm.player.event.TGMPlayerDeathEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

@ModuleData(load = ModuleLoadTime.EARLIER) @Getter
public class FirstBloodModule extends MatchModule implements Listener {
    private boolean enabled;
    private String msg;
    private Match currentMatch = null;
    private DeathModule deathModule = null;
    @Setter private boolean relevant = true;

    @Override
    public void load(Match match) {
        currentMatch = match;
        deathModule = currentMatch.getModule(DeathModule.class);
        JsonObject mapData = match.getMapContainer().getMapInfo().getJsonObject();
        if(mapData.has("firstBlood")) {
            JsonObject firstBloodOpts = mapData.get("firstBlood").getAsJsonObject();
            enabled = false;
            if(firstBloodOpts.has("enabled")) enabled = firstBloodOpts.get("enabled").getAsBoolean();
            msg = "%killer% &7has drew &6&lFIRST BLOOD!";
            if(firstBloodOpts.has("message")) msg = firstBloodOpts.get("message").getAsString();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onKill(TGMPlayerDeathEvent event) {
        DeathModule module = deathModule.getPlayer(event.getVictim());
        if(!relevant || module == null || !enabled || module.getKiller() == null || module.getKillerTeam() == null || module.getPlayerTeam() == null) return;
        if(currentMatch != null && currentMatch.getFirstBlood() == null) {
            currentMatch.setFirstBlood(module.getKiller());
            String realMsg = msg;
            realMsg = ChatColor.translateAlternateColorCodes('&', realMsg);
            realMsg = realMsg.replaceAll("%killer%", module.getKillerTeam().getColor() + module.getKillerName());
            realMsg = realMsg.replaceAll("%victim%", module.getPlayerTeam().getColor() + module.getPlayerName());
            for (Player player : Bukkit.getOnlinePlayers()) {
                Location location = player.getLocation().clone().add(0.0, 100.0, 0.0);
                player.getPlayer().sendMessage(realMsg);
                player.playSound(location, Sound.ENTITY_PLAYER_LEVELUP, 1000, 2);
            }
        }

    }
}
