package network.warzone.tgm.modules;

import com.google.gson.JsonObject;
import lombok.Getter;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.player.event.PlayerLevelUpEvent;
import network.warzone.tgm.player.event.PlayerXPEvent;
import network.warzone.tgm.util.Levels;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Created by Jorge on 10/6/2017.
 */
@Getter
public class StatsModule extends MatchModule implements Listener{
    private int xpBarTaskId;

    private boolean statsDisabled = false;
    private boolean notifyDisable = true;
    private boolean showLevel = true;

    @Override
    public void load(Match match) {

        if (match.getMapContainer().getMapInfo().getJsonObject().has("stats")) {
            JsonObject statsObj = (JsonObject) match.getMapContainer().getMapInfo().getJsonObject().get("stats");
            if (statsObj.has("disable")) statsDisabled = statsObj.get("disable").getAsBoolean();
            if (statsObj.has("notifydisable")) notifyDisable = statsObj.get("notifydisable").getAsBoolean();
            if (statsObj.has("showlevel")) showLevel = statsObj.get("showlevel").getAsBoolean();
        }
        if (showLevel) xpBarTaskId = Bukkit.getScheduler().runTaskTimer(TGM.get(), () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.setLevel(TGM.get().getPlayerManager().getPlayerContext(player).getUserProfile().getLevel());
                player.setExp((float) Levels.getLevelProgress(player) / 100);
            }
        }, 2, 2).getTaskId();
        if (statsDisabled && notifyDisable) Bukkit.getOnlinePlayers().forEach(this::notifyDisable);
    }

    private void notifyDisable(Player player) {
        player.sendMessage(ChatColor.RED + "Stat tracking on this map has been disabled.");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        if (statsDisabled && notifyDisable) notifyDisable(event.getPlayer());
    }

    @Override
    public void unload() {
        if (isShowLevel()) Bukkit.getScheduler().cancelTask(xpBarTaskId);
    }

    @EventHandler
    public void onPlayerXP(PlayerXPEvent event) {
        if (isStatsDisabled()) return;
        if (Levels.calculateLevel(event.getFromXP()) < Levels.calculateLevel(event.getToXP())) {
            Bukkit.getPluginManager().callEvent(new PlayerLevelUpEvent(event.getPlayerContext(), event.getPlayerContext().getUserProfile().getLevel() - 1, event.getPlayerContext().getUserProfile().getLevel()));
        }
    }

    @EventHandler
    public void onPlayerLevelUp(PlayerLevelUpEvent event) {
        if (isStatsDisabled()) return;
        Player player = event.getPlayerContext().getPlayer();
        player.sendMessage(ChatColor.AQUA + "" + ChatColor.STRIKETHROUGH + "----------------------------------------");
        player.sendMessage(ChatColor.GREEN +  "" +  ChatColor.BOLD + " Level up!" + ChatColor.GREEN + " You are now level " + ChatColor.RED + event.getToLevel());
        player.sendMessage(ChatColor.AQUA + "" + ChatColor.STRIKETHROUGH + "----------------------------------------");
        player.playSound(player.getLocation().clone().add(0.0, 100.0, 0.0), Sound.ENTITY_PLAYER_LEVELUP, 1000, 1);
    }

}
