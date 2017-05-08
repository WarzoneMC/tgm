package com.minehut.tgm.modules;

import com.minehut.tgm.TGM;
import com.minehut.tgm.damage.grave.event.PlayerDeathByPlayerEvent;
import com.minehut.tgm.damage.grave.event.PlayerDeathEvent;
import com.minehut.tgm.match.Match;
import com.minehut.tgm.match.MatchModule;
import com.minehut.tgm.modules.team.MatchTeam;
import com.minehut.tgm.modules.team.TeamManagerModule;
import com.minehut.tgm.user.PlayerContext;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DeathMessageModule extends MatchModule implements Listener {
    @Getter private TeamManagerModule teamManagerModule;

    public void load(Match match) {
        teamManagerModule = match.getModule(TeamManagerModule.class);
    }

    @EventHandler
    public void onTGMDeath(PlayerDeathEvent event) {
        MatchTeam matchTeam = teamManagerModule.getTeam(event.getPlayer());

        if(matchTeam.isSpectator()) return; //stupid spectators

        Player killer = null;
        String message = "";
        EntityDamageEvent.DamageCause cause = event.getEntity().getLastDamageCause().getCause();
        if (event instanceof PlayerDeathByPlayerEvent) {
            killer = ((PlayerDeathByPlayerEvent) event).getCause();
            MatchTeam killerTeam = teamManagerModule.getTeam(killer);
            ItemStack weapon = killer.getInventory().getItemInMainHand();
            if (cause.equals(EntityDamageEvent.DamageCause.FALL)){
                if (weapon.getType().equals(Material.BOW))
                    // Needs a better way to get distance between players
                    message = matchTeam.getColor() + event.getPlayer().getName() + ChatColor.GRAY + " was shot off a high place by " + killerTeam.getColor() + killer.getName() + ChatColor.GRAY;
                else
                    message = matchTeam.getColor() + event.getPlayer().getName() + ChatColor.GRAY + " was thrown off a high place by " + killerTeam.getColor() + killer.getName() + ChatColor.GRAY + " using " + itemToString(weapon);
            }
            else if (cause.equals(EntityDamageEvent.DamageCause.VOID)){
                if (weapon.getType().equals(Material.BOW))
                    // Needs a better way to get distance between players
                    message = matchTeam.getColor() + event.getPlayer().getName() + ChatColor.GRAY + " was shot into the void by " + killerTeam.getColor() + killer.getName() + ChatColor.GRAY;
                else
                    message = matchTeam.getColor() + event.getPlayer().getName() + ChatColor.GRAY + " was thrown into the void by " + killerTeam.getColor() + killer.getName() + ChatColor.GRAY + " using " + itemToString(weapon);
            }
            else if (cause.equals(EntityDamageEvent.DamageCause.PROJECTILE)){
                int distance = ((Double) killer.getLocation().distance(event.getPlayer().getLocation())).intValue();
                message = matchTeam.getColor() + event.getPlayer().getName() + ChatColor.GRAY + " was shot by " + killerTeam.getColor() + killer.getName() + ChatColor.GRAY + " from " + distance + " blocks";
            }
            else {
                message = matchTeam.getColor() + event.getPlayer().getName() + ChatColor.GRAY + " was killed by " + killerTeam.getColor() + killer.getName() + ChatColor.GRAY + " using " + itemToString(weapon);
            }

        } else {
            if (cause.equals(EntityDamageEvent.DamageCause.FALL)){
                message = matchTeam.getColor() + event.getPlayer().getName() + ChatColor.GRAY + " fell from a high place.";
            }
            else if (cause.equals(EntityDamageEvent.DamageCause.VOID)){
                message = matchTeam.getColor() + event.getPlayer().getName() + ChatColor.GRAY + " fell into the void.";
            }
            else {
                message = matchTeam.getColor() + event.getPlayer().getName() + ChatColor.GRAY + " died to the environment.";
            }
        }
        if (message.length() > 0) {
            broadcastDeathMessage(event.getPlayer(), killer, message);
        }

    }
    
    private String itemToString(ItemStack item){
        if (item == null || item.getType().equals(Material.AIR)){
            return "their hands";
        }
        StringBuilder stringBuilder = new StringBuilder();
        if (item.hasItemMeta() && item.getItemMeta().hasEnchants()) stringBuilder.append("Enchanted ");
        String materialName = item.getType().toString();
        for (String word : materialName.split("_")){
            word = word.toLowerCase();
            word = word.substring(0, 1).toUpperCase() + word.substring(1) + " ";
            stringBuilder.append(word);
        }
        return stringBuilder.toString().trim();
    }

    private void broadcastDeathMessage(Player dead, Player killer, String message) {
        for (PlayerContext playerContext : TGM.get().getPlayerManager().getPlayers()) {

            //bold messages when the player is involved
            boolean involved = dead == playerContext.getPlayer() || (killer != null && killer == playerContext.getPlayer());
            if (involved) {
                message.replaceAll(dead.getName() + ChatColor.GRAY, ChatColor.BOLD + dead.getName() + ChatColor.GRAY + ChatColor.BOLD);
                if (killer != null) {
                    if (message.contains(killer.getName() + ChatColor.GRAY)) {
                        message.replaceAll(killer.getName() + ChatColor.GRAY, ChatColor.BOLD + killer.getName() + ChatColor.GRAY);
                    } else {
                        message.replaceAll(killer.getName(), ChatColor.BOLD + killer.getName());
                    }
                }
            }

            playerContext.getPlayer().sendMessage(message);
        }
    }


    @EventHandler
    public void onBukkitDeath(org.bukkit.event.entity.PlayerDeathEvent event) {
        event.setDeathMessage("");
    }


}
