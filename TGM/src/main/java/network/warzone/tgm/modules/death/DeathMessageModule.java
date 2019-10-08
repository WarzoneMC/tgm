package network.warzone.tgm.modules.death;

import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.player.event.TGMPlayerDeathEvent;
import network.warzone.tgm.util.itemstack.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import static org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class DeathMessageModule extends MatchModule implements Listener {

    private DeathModule deathModule;

    public void load(Match match) {
        deathModule = match.getModule(DeathModule.class);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTGMDeath(TGMPlayerDeathEvent event) {
        DeathInfo deathInfo = deathModule.getPlayer(event.getVictim());

        if (deathInfo.playerTeam.isSpectator()) return; //stupid spectators

        String message;
        ItemStack weapon = deathInfo.item;
        DamageCause cause = deathInfo.cause;

        MatchTeam playerTeam = deathInfo.playerTeam;
        MatchTeam killerTeam = deathInfo.killerTeam;

        if (deathInfo.killer != null && deathInfo.killerName != null) {
            if (cause.equals(DamageCause.FALL)) {
                if (weapon != null && weapon.getType().equals(Material.BOW))
                    message = playerTeam.getColor() + deathInfo.playerName + ChatColor.GRAY + " was shot off a high place by " +
                            killerTeam.getColor() + deathInfo.killerName + ChatColor.GRAY;
                else
                    message = playerTeam.getColor() + deathInfo.playerName + ChatColor.GRAY + " was thrown off a high place by " +
                            killerTeam.getColor() + deathInfo.killerName + ChatColor.GRAY + " using " +
                            ItemUtils.itemToString(weapon);
            } else if (cause.equals(DamageCause.VOID)) {
                if (weapon != null && (weapon.getType().equals(Material.BOW) || weapon.getType().equals(Material.TRIDENT)))
                    message = playerTeam.getColor() + deathInfo.playerName + ChatColor.GRAY + " was shot into the void by " +
                            killerTeam.getColor() + deathInfo.killerName + ChatColor.GRAY;
                else
                    if (!deathInfo.playerName.equals(deathInfo.killerName)) {
                        message = playerTeam.getColor() + deathInfo.playerName + ChatColor.GRAY + " was thrown into the void by " +
                                killerTeam.getColor() + deathInfo.killerName + ChatColor.GRAY + " using " +
                                ItemUtils.itemToString(weapon);
                    } else {
                        message = playerTeam.getColor() + deathInfo.playerName + ChatColor.GRAY + " fell into the void";
                    }
            } else if (cause.equals(DamageCause.PROJECTILE)) {
                int distance = ((Double) deathInfo.killerLocation.distance(deathInfo.playerLocation)).intValue();
                message = playerTeam.getColor() + deathInfo.playerName + ChatColor.GRAY + (weapon.getType() == Material.TRIDENT ? " forked " : " was shot by ") +
                        killerTeam.getColor() + deathInfo.killerName + ChatColor.GRAY + " from " + distance + (distance == 1 ? " block" : " blocks");
            } else if (cause.equals(DamageCause.FIRE) || cause.equals(DamageCause.FIRE_TICK)) {
                if (!deathInfo.playerName.equals(deathInfo.killerName)) {
                    message = playerTeam.getColor() + deathInfo.playerName + ChatColor.GRAY + " was burned to death by " + killerTeam.getColor() + deathInfo.killerName +ChatColor.GRAY;
                } else {
                    message = playerTeam.getColor() + deathInfo.playerName + ChatColor.GRAY + " burned to death";
                }
            } else {
                if (!deathInfo.playerName.equals(deathInfo.killerName)) {
                    message = playerTeam.getColor() + deathInfo.playerName + ChatColor.GRAY + " was killed by " +
                            killerTeam.getColor() + deathInfo.killerName + ChatColor.GRAY + " using " +
                            (cause.equals(DamageCause.ENTITY_ATTACK) ? ItemUtils.itemToString(weapon) : "the environment");
                } else {
                    message = playerTeam.getColor() + deathInfo.playerName + ChatColor.GRAY + " died to the environment";
                }
            }

            if (deathInfo.killer != null) deathInfo.killer.playSound(deathInfo.killer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 3, 1.4f);
        } else {
            if (cause.equals(DamageCause.FALL)) {
                message = playerTeam.getColor() + deathInfo.playerName + ChatColor.GRAY + " fell from a high place";
            } else if (cause.equals(DamageCause.VOID)) {
                message = playerTeam.getColor() + deathInfo.playerName + ChatColor.GRAY + " fell into the void";
            } else if (cause.equals(DamageCause.FIRE) || cause.equals(DamageCause.FIRE_TICK)) {
                message = playerTeam.getColor() + deathInfo.playerName + ChatColor.GRAY + " burned to death";
            } else {
                message = playerTeam.getColor() + deathInfo.playerName + ChatColor.GRAY + " died to the environment";
            }
        }

        deathInfo.player.getWorld().playSound(deathInfo.playerLocation, Sound.ENTITY_IRON_GOLEM_DEATH, 2, 2);

        if (message.length() > 0) {
            broadcastDeathMessage(deathInfo.player, deathInfo.killer, message);

            deathInfo.killer = null;
            deathInfo.killerName = null;
        }

    }

    private void broadcastDeathMessage(Player dead, Player killer, String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {

            /* TODO make look better and also fix
            //bold messages when the player is involved
            if (dead == player || (killer != null && killer == player)) {
                message = message.replaceAll(dead.getName() + ChatColor.GRAY, ChatColor.BOLD + dead.getName() + ChatColor.GRAY + ChatColor.BOLD);
                if (killer != null) {
                    if (message.contains(killer.getName() + ChatColor.GRAY)) {
                        message = message.replaceAll(killer.getName() + ChatColor.GRAY, ChatColor.BOLD + killer.getName() + ChatColor.GRAY + ChatColor.BOLD);
                    } else {
                        message = message.replaceAll(killer.getName(), ChatColor.BOLD + killer.getName());
                    }
                }
            }
            */

            player.getPlayer().sendMessage(message);
        }
    }


    @EventHandler
    public void onBukkitDeath(PlayerDeathEvent event) {
        event.setDeathMessage("");
    }


}
