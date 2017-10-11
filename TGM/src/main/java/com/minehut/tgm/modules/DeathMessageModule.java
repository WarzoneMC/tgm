package com.minehut.tgm.modules;

import com.minehut.tgm.damage.grave.event.PlayerDeathByPlayerEvent;
import com.minehut.tgm.damage.grave.event.PlayerDeathEvent;
import com.minehut.tgm.match.Match;
import com.minehut.tgm.match.MatchModule;
import com.minehut.tgm.modules.team.MatchTeam;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import static org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class DeathMessageModule extends MatchModule implements Listener {

    private DeathModule deathModule;

    public void load(Match match) {
        deathModule = match.getModule(DeathModule.class);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTGMDeath(PlayerDeathEvent event) {
        DeathModule module = deathModule.getPlayer(event.getPlayer());

        if (module.getPlayerTeam().isSpectator()) return; //stupid spectators

        String message;
        ItemStack weapon = module.getItem();
        DamageCause cause = module.getCause();

        MatchTeam playerTeam = module.getPlayerTeam();
        MatchTeam killerTeam = module.getKillerTeam();

        if (event instanceof PlayerDeathByPlayerEvent && module.getKillerName() != null) {
            if (cause.equals(DamageCause.FALL)) {
                if (weapon != null && weapon.getType().equals(Material.BOW))
                    message = playerTeam.getColor() + module.getPlayerName() + ChatColor.GRAY + " was shot off a high place by " +
                            killerTeam.getColor() + module.getKillerName() + ChatColor.GRAY;
                else
                    message = playerTeam.getColor() + module.getPlayerName() + ChatColor.GRAY + " was thrown off a high place by " +
                            killerTeam.getColor() + module.getKillerName() + ChatColor.GRAY + " using " +
                            itemToString(weapon);
            } else if (cause.equals(DamageCause.VOID)) {
                if (weapon != null && weapon.getType().equals(Material.BOW))
                    message = playerTeam.getColor() + module.getPlayerName() + ChatColor.GRAY + " was shot into the void by " +
                            killerTeam.getColor() + module.getKillerName() + ChatColor.GRAY;
                else
                    if (!module.getPlayerName().equals(module.getKillerName())) {
                        message = playerTeam.getColor() + module.getPlayerName() + ChatColor.GRAY + " was thrown into the void by " +
                                killerTeam.getColor() + module.getKillerName() + ChatColor.GRAY + " using " +
                                itemToString(weapon);
                    } else {
                        message = playerTeam.getColor() + module.getPlayerName() + ChatColor.GRAY + " fell into the void";
                    }
            } else if (cause.equals(DamageCause.PROJECTILE)) {
                int distance = ((Double) module.getKillerLocation().distance(module.getPlayerLocation())).intValue();
                message = playerTeam.getColor() + module.getPlayerName() + ChatColor.GRAY + " was shot by " +
                        killerTeam.getColor() + module.getKillerName() + ChatColor.GRAY + " from " + distance + " blocks";
            } else {
                if (!module.getPlayerName().equals(module.getKillerName())) {
                    message = playerTeam.getColor() + module.getPlayerName() + ChatColor.GRAY + " was killed by " +
                            killerTeam.getColor() + module.getKillerName() + ChatColor.GRAY + " using " +
                            (cause.equals(DamageCause.ENTITY_ATTACK) ? itemToString(weapon) : "the the environment");
                } else {
                    message = playerTeam.getColor() + module.getPlayerName() + ChatColor.GRAY + " died to the environment";
                }
            }

            if (module.getKiller() != null) module.getKiller().playSound(module.getKiller().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 3, 1.4f);
        } else {
            if (cause.equals(DamageCause.FALL)) {
                message = playerTeam.getColor() + module.getPlayerName() + ChatColor.GRAY + " fell from a high place";
            } else if (cause.equals(DamageCause.VOID)) {
                message = playerTeam.getColor() + module.getPlayerName() + ChatColor.GRAY + " fell into the void";
            } else {
                message = playerTeam.getColor() + module.getPlayerName() + ChatColor.GRAY + " died to the environment";
            }
        }

        module.getPlayer().getWorld().playSound(module.getPlayerLocation(), Sound.ENTITY_IRONGOLEM_DEATH, 2, 2);

        if (message.length() > 0) {
            broadcastDeathMessage(event.getPlayer(), module.getKiller(), message);

            module.setKiller(null);
            module.setKillerName(null);
        }

    }

    private String itemToString(ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR)) {
            return "their hands";
        }
        StringBuilder stringBuilder = new StringBuilder();
        if (item.hasItemMeta() && item.getItemMeta().hasEnchants()) stringBuilder.append("Enchanted ");
        String materialName = item.getType().toString();
        for (String word : materialName.split("_")) {
            word = word.toLowerCase();
            word = word.substring(0, 1).toUpperCase() + word.substring(1) + " ";
            stringBuilder.append(word);
        }
        return stringBuilder.toString().trim();
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
    public void onBukkitDeath(org.bukkit.event.entity.PlayerDeathEvent event) {
        event.setDeathMessage("");
    }


}
