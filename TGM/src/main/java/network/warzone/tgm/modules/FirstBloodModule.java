package network.warzone.tgm.modules;

import com.google.gson.JsonObject;
import lombok.Getter;
import network.warzone.tgm.TGM;
import network.warzone.tgm.map.MapContainer;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchLoadEvent;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.player.event.TGMPlayerDeathEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

@Getter
public class FirstBloodModule extends MatchModule implements Listener {
    private boolean enabled;
    private String msg;
    private Match currentMatch = null;
    private DeathModule deathModule = null;

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

    @EventHandler
    public void onKill(TGMPlayerDeathEvent event) {
        DeathModule module = deathModule.getPlayer(event.getVictim());
        if(module == null || !enabled) return;
        if(currentMatch != null) {
            if(currentMatch.getFirstBlood() == null && module.getKiller() != null && module.getKillerTeam() != null && module.getPlayerTeam() != null) {
                currentMatch.setFirstBlood(module.getKiller());
                if (!module.getPlayerTeam().isSpectator() && !module.getKillerTeam().isSpectator()) {
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

    }
}
