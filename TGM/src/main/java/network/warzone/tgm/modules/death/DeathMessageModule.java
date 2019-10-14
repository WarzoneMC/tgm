package network.warzone.tgm.modules.death;

import lombok.Getter;
import lombok.Setter;
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

import java.util.*;

import static org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class DeathMessageModule extends MatchModule implements Listener {

    private DeathModule deathModule;

    @Getter @Setter
    private DeathMessageEvaluator defaultDeathMessage = (d) -> {
        broadcastDeathMessage(d.player, d.killer, "%s%s%s died to the environment",
            d.playerTeam.getColor(),
            d.player.getName(),
            ChatColor.GRAY
        );
        return true;
    };

    public void load(Match match) {
        deathModule = match.getModule(DeathModule.class);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTGMDeath(TGMPlayerDeathEvent event) {
        DeathInfo deathInfo = deathModule.getPlayer(event.getVictim());

        if (deathInfo.playerTeam.isSpectator()) return; //stupid spectators

        for (DeathMessageEvaluator messageEvaluator : this.deathMessages.getOrDefault(event.getCause(), Collections.singletonList(defaultDeathMessage))) {
            if (messageEvaluator.evaluate(deathInfo)) break;
        }

        deathInfo.player.getWorld().playSound(deathInfo.playerLocation, Sound.ENTITY_IRON_GOLEM_DEATH, 2, 2);
        if (deathInfo.killer != null) deathInfo.killer.playSound(deathInfo.killer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 3, 1.4f);
    }

    public static void broadcastDeathMessage(Player dead, Player killer, String message, Object... args) {
        message = String.format(message, args);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.getPlayer().sendMessage(message);
        }
    }


    @EventHandler
    public void onBukkitDeath(PlayerDeathEvent event) {
        event.setDeathMessage("");
    }

    @Getter
    private Map<DamageCause, List<DeathMessageEvaluator>> deathMessages = new LinkedHashMap<DamageCause, List<DeathMessageEvaluator>>() {{
        put(DamageCause.ENTITY_ATTACK, Arrays.asList(
                (d) -> {
                    if (d.killer != null) return false;
                    broadcastDeathMessage(d.player, null, "%s%s%s died",
                            d.playerTeam.getColor(),
                            d.playerName,
                            ChatColor.GRAY
                    );
                    return true;
                },
                (d) -> {
                    broadcastDeathMessage(d.player, d.killer, "%s%s%s was killed by %s%s%s using %s",
                            d.playerTeam.getColor(),
                            d.playerName,
                            ChatColor.GRAY,
                            d.killerTeam.getColor(),
                            d.killerName,
                            ChatColor.GRAY,
                            (d.cause.equals(DamageCause.ENTITY_ATTACK) ? ItemUtils.itemToString(d.item) : "the environment")

                    );
                    return true;
                }
        ));
        put(DamageCause.ENTITY_SWEEP_ATTACK, get(DamageCause.ENTITY_ATTACK));
        put(DamageCause.FALL, Arrays.asList(
                (d) -> {
                    if (d.killer != null) return false;
                    broadcastDeathMessage(d.player, null, "%s%s%s fell from a high place",
                            d.playerTeam.getColor(),
                            d.playerName,
                            ChatColor.GRAY
                    );
                    return true;
                },
                (d) -> {
                    if (d.item == null || !d.item.getType().equals(Material.BOW)) return false;
                    broadcastDeathMessage(d.player, d.killer, "%s%s%s was shot off a high place by %s%s",
                            d.playerTeam.getColor(),
                            d.playerName,
                            ChatColor.GRAY,
                            d.killerTeam.getColor(),
                            d.killerName
                    );
                    return true;
                },
                (d) -> {
                    broadcastDeathMessage(d.player, d.killer, "%s%s%s was thrown off a high place by %s%s%s using %s",
                            d.playerTeam.getColor(),
                            d.playerName,
                            ChatColor.GRAY,
                            d.killerTeam.getColor(),
                            d.killerName,
                            ChatColor.GRAY,
                            ItemUtils.itemToString(d.item)
                    );
                    return true;
                }
        ));
        put(DamageCause.VOID, Arrays.asList(
                (d) -> {
                    if (d.killer != null) return false;
                    broadcastDeathMessage(d.player, null, "%s%s%s fell into the void",
                            d.playerTeam.getColor(),
                            d.playerName,
                            ChatColor.GRAY
                    );
                    return true;
                },
                (d) -> {
                    if (d.item == null || !d.item.getType().equals(Material.BOW)) return false;
                    broadcastDeathMessage(d.player, d.killer, "%s%s%s was shot into the void by %s%s",
                            d.playerTeam.getColor(),
                            d.playerName,
                            ChatColor.GRAY,
                            d.killerTeam.getColor(),
                            d.killerName
                    );
                    return true;
                },
                (d) -> {
                    broadcastDeathMessage(d.player, d.killer, "%s%s%s was thrown into the void by %s%s%s using %s",
                            d.playerTeam.getColor(),
                            d.playerName,
                            ChatColor.GRAY,
                            d.killerTeam.getColor(),
                            d.killerName,
                            ChatColor.GRAY,
                            ItemUtils.itemToString(d.item)
                    );
                    return true;
                }
        ));
        put(DamageCause.PROJECTILE, Arrays.asList(
                (d) -> {
                    if (d.killer != null) return false;
                    broadcastDeathMessage(d.player, null, "%s%s%s was %s to death",
                            d.playerTeam.getColor(),
                            d.playerName,
                            ChatColor.GRAY,
                            (d.item.getType() == Material.TRIDENT ? "forked" : "shot")
                    );
                    return true;
                },
                (d) -> {
                    if (d.item == null) return false;
                    int distance = ((Double) d.killerLocation.distance(d.playerLocation)).intValue();
                    broadcastDeathMessage(d.player, d.killer, "%s%s%s was %s by %s%s%s from %d%s",
                            d.playerTeam.getColor(),
                            d.playerName,
                            ChatColor.GRAY,
                            (d.item.getType() == Material.TRIDENT ? "forked" : "shot"),
                            d.killerTeam.getColor(),
                            d.killerName,
                            ChatColor.GRAY,
                            distance,
                            (distance == 1 ? " block" : " blocks")
                    );
                    return true;
                }
        ));
        put(DamageCause.FIRE, Arrays.asList(
                (d) -> {
                    if (d.killer != null) return false;
                    broadcastDeathMessage(d.player, null, "%s%s%s burned to death",
                            d.playerTeam.getColor(),
                            d.playerName,
                            ChatColor.GRAY
                    );
                    return true;
                },
                (d) -> {
                    broadcastDeathMessage(d.player, d.killer, "%s%s%s was burned to death by %s%s",
                            d.playerTeam.getColor(),
                            d.playerName,
                            ChatColor.GRAY,
                            d.killerTeam.getColor(),
                            d.killerName
                    );
                    return true;
                }
        ));
        put(DamageCause.FIRE_TICK, get(DamageCause.FIRE));
        put(DamageCause.SUFFOCATION, Arrays.asList(
                (d) -> {
                    broadcastDeathMessage(d.player, null, "%s%s%s suffocated to death",
                            d.playerTeam.getColor(),
                            d.playerName,
                            ChatColor.GRAY
                    );
                    return true;
                })
        );
    }};


}
