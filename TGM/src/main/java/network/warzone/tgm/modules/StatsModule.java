package network.warzone.tgm.modules;

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import com.google.gson.JsonObject;
import lombok.Getter;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.player.event.PlayerLevelUpEvent;
import network.warzone.tgm.player.event.PlayerXPEvent;
import network.warzone.tgm.user.PlayerContext;
import network.warzone.warzoneapi.Levels;
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
        if (statsDisabled && notifyDisable) Bukkit.getOnlinePlayers().forEach(this::notifyDisable);
    }

    private void notifyDisable(Player player) {
        player.sendMessage(ChatColor.RED + "Stat tracking on this map has been disabled.");
    }

    public boolean shouldSetExperience() {
        return !statsDisabled && showLevel;
    }

    public void setTGMLevel(PlayerContext context) {
        if (!shouldSetExperience()) return;
        context.getPlayer().setLevel(context.getUserProfile().getLevel());
        context.getPlayer().setExp((float) Levels.getLevelProgress(context.getUserProfile()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        if (statsDisabled && notifyDisable) notifyDisable(event.getPlayer());
    }

    @EventHandler
    public void onExpPickup(PlayerPickupExperienceEvent event) {
        if (!shouldSetExperience()) return;
        event.setCancelled(true);
        event.getExperienceOrb().remove();
    }

    @EventHandler
    public void onPlayerXP(PlayerXPEvent event) {
        if (Levels.calculateLevel(event.getFromXP()) < Levels.calculateLevel(event.getToXP())) {
            Bukkit.getPluginManager().callEvent(new PlayerLevelUpEvent(event.getPlayerContext(), event.getPlayerContext().getUserProfile().getLevel() - 1, event.getPlayerContext().getUserProfile().getLevel()));
        }
        if (!shouldSetExperience()) return;
        Player player = event.getPlayerContext().getPlayer();
        player.setExp((float) Levels.getLevelProgress(event.getPlayerContext().getUserProfile()));
    }

    @EventHandler
    public void onPlayerLevelUp(PlayerLevelUpEvent event) {
        Player player = event.getPlayerContext().getPlayer();
        player.sendMessage(ChatColor.AQUA + "" + ChatColor.STRIKETHROUGH + "----------------------------------------");
        player.sendMessage(ChatColor.GREEN +  "" +  ChatColor.BOLD + " Level up!" + ChatColor.GREEN + " You are now level " + ChatColor.RED + event.getToLevel());
        player.sendMessage(ChatColor.AQUA + "" + ChatColor.STRIKETHROUGH + "----------------------------------------");
        player.playSound(player.getLocation().clone().add(0.0, 100.0, 0.0), Sound.ENTITY_PLAYER_LEVELUP, 1000, 1);
        if (!shouldSetExperience()) return;
        player.setLevel(event.getPlayerContext().getUserProfile().getLevel());
    }

}
