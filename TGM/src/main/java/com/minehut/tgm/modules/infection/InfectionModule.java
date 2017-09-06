package com.minehut.tgm.modules.infection;

import com.google.gson.JsonObject;
import com.minehut.teamapi.models.Death;
import com.minehut.tgm.TGM;
import com.minehut.tgm.api.ApiManager;
import com.minehut.tgm.damage.tracker.event.PlayerDamageEvent;
import com.minehut.tgm.match.Match;
import com.minehut.tgm.match.MatchModule;
import com.minehut.tgm.match.MatchStatus;
import com.minehut.tgm.modules.team.MatchTeam;
import com.minehut.tgm.modules.team.TeamChangeEvent;
import com.minehut.tgm.modules.team.TeamManagerModule;
import com.minehut.tgm.user.PlayerContext;
import com.minehut.tgm.util.Players;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

/**
 * Created by Draem on 7/31/2017.
 */
public class InfectionModule extends MatchModule implements Listener {

    @Getter private TeamManagerModule teamManager;

    @Getter private int length;

    @Getter private Match match;

    @Override
    public void load(Match match) {
        TGM.registerEvents(this);
        JsonObject json = match.getMapContainer().getMapInfo().getJsonObject().get("infection").getAsJsonObject();
        length = json.get("length").getAsInt();
        teamManager = match.getModule(TeamManagerModule.class);
        this.match = match;
    }

    @Override
    public void enable() {
        int players = teamManager.getTeamById("humans").getMembers().size();
        int zombies = (int)(players * (5 / 100.0F)) == 0 ? 1 : (int)(players * (5 / 100.0F));

        for (int i = 0; i < zombies; i++) {
            PlayerContext player = teamManager.getTeamById("humans").getMembers().get(new Random().nextInt(teamManager.getTeamById("humans").getMembers().size()));
            broadcastMessage(String.format("&2&l%s &7has been infected!", player.getPlayer().getName()));

            infect(player.getPlayer());
            freeze(player.getPlayer());
        }

        for (MatchTeam team : teamManager.getTeams()) {
            team.getMembers().forEach(playerContext -> playerContext.getPlayer().setGameMode(GameMode.ADVENTURE));
        }

        match.getModule(InfectedTimeLimit.class).startCountdown(length);
    }

//    @EventHandler
//    public void onPlayerDeath(PlayerDeathEvent event) {
//        // If the player isn't on the spectator team, they must be either human or infected.
//        if (!teamManager.getTeam(event.getPlayer()).getId().equals("spectators")) {
//
//            // "infected" = zombie team; "humans" = human team;
//            if (teamManager.getTeam(event.getPlayer()).getId().equals("humans")) {
//                // Now to determine if the user got infected by a player, or died because of anything else (still infected).
//                if (event instanceof PlayerDeathByPlayerEvent) {
//                    broadcastMessage(String.format("%s%s &7has been infected by %s%s",
//                            teamManager.getTeam(event.getPlayer()).getColor(),
//                            event.getPlayer().getName(),
//                            teamManager.getTeam(((PlayerDeathByPlayerEvent) event).getCause()).getColor(),
//                            ((PlayerDeathByPlayerEvent) event).getCause().getName()));
//
//                } else {
//                    broadcastMessage(String.format("%s%s &7has been taken by the environment",
//                            teamManager.getTeam(event.getPlayer()).getColor(),
//                            event.getPlayer().getName()));
//                }
//
//                infect(event.getPlayer());
//            } else if (teamManager.getTeam(event.getPlayer()).getId().equalsIgnoreCase("infected")) {
//                if (event instanceof PlayerDeathByPlayerEvent) {
//                    if (teamManager.getTeam(((PlayerDeathByPlayerEvent) event).getCause()).getId().equalsIgnoreCase("infected")) {
//                        return;
//                    }
//                    broadcastMessage(String.format("%s%s &7has been slain by %s%s",
//                            teamManager.getTeam(event.getPlayer()).getColor(),
//                            event.getPlayer().getName(),
//                            teamManager.getTeam(((PlayerDeathByPlayerEvent) event).getCause()).getColor(),
//                            ((PlayerDeathByPlayerEvent) event).getCause().getName()
//                            ));
//                }
//            }
//
//        }
//    }

    @EventHandler
    public void onDamage(PlayerDamageEvent event) {
        if (event.getEntity().getHealth() - event.getDamage() < 1) {
            // DED :)
            // So if it's a kill blowing, I can cancel the event so they don't die, but instead just broadcast the message and infect them.

            // "humans" = humans; "infected" = infected;
            if (teamManager.getTeam(event.getEntity()).getId().equalsIgnoreCase("humans")) {

                if (event.getInfo().getResolvedDamager() instanceof Player) {
                    Player killer = (Player) event.getInfo().getResolvedDamager();

                    broadcastMessage(String.format("%s%s &7has been infected by %s%s",
                            teamManager.getTeam(event.getEntity()).getColor(),
                            event.getEntity().getName(),
                            teamManager.getTeam(killer).getColor(),
                            killer.getName()));
                } else {
                    broadcastMessage(String.format("%s%s &7has been taken by the environment",
                            teamManager.getTeam(event.getEntity()).getColor(),
                            event.getEntity().getName()));
                }

                infect(event.getEntity());
            } else if (teamManager.getTeam(event.getEntity()).getId().equalsIgnoreCase("infected")) {
                if (event.getInfo().getResolvedDamager() instanceof Player) {
                    Player killer = (Player) event.getInfo().getResolvedDamager();

                    if (teamManager.getTeam(killer).getId().equalsIgnoreCase("infected")) {
                        return;
                    }
                    broadcastMessage(String.format("%s%s &7has been slain by %s%s",
                            teamManager.getTeam(event.getEntity()).getColor(),
                            event.getEntity().getName(),
                            teamManager.getTeam(killer).getColor(),
                            killer.getName()
                            ));
                }
                refresh(TGM.get().getPlayerManager().getPlayerContext(event.getEntity()), teamManager.getTeam(event.getEntity()));
            }
            event.setDamage(0);

            if (TGM.get().getApiManager().isStatsDisabled()) {
                return;
            }

            PlayerContext player = TGM.get().getPlayerManager().getPlayerContext(event.getEntity()); //dead

            String playerItem = "";
            if (event.getEntity().getInventory().getItemInMainHand() != null) {
                playerItem = event.getEntity().getInventory().getItemInMainHand().getType().toString();
            }

            String killerItem = "";
            String killerId = null;
            if (event.getInfo().getResolvedDamager() instanceof Player) {
                killerId = TGM.get().getPlayerManager().getPlayerContext(((Player) event.getInfo().getResolvedDamager())).getUserProfile().getId().toString();
                if (event.getInfo().getResolvedDamager() != null) {
                    killerItem = ((Player) event.getInfo().getResolvedDamager()).getInventory().getItemInMainHand().getType().toString();
                }
            }

            ApiManager api = TGM.get().getApiManager();

            Death death = new Death(player.getUserProfile().getId().toString(), killerId, playerItem,
                    killerItem, api.getMatchInProgress().getMap(), api.getMatchInProgress().getId());
            Bukkit.getScheduler().runTaskAsynchronously(TGM.get(), () -> TGM.get().getTeamClient().addKill(death));
        }
    }

    private void refresh(PlayerContext playerContext, MatchTeam matchTeam) {
        Players.reset(playerContext.getPlayer(), true);

        matchTeam.getKits().get(0).apply(playerContext.getPlayer(), matchTeam);
        playerContext.getPlayer().updateInventory();
        playerContext.getPlayer().teleport(matchTeam.getSpawnPoints().get(0).getLocation());
        playerContext.getPlayer().setGameMode(GameMode.ADVENTURE);
        playerContext.getPlayer().addPotionEffects(Collections.singleton(new PotionEffect(PotionEffectType.JUMP, 10000, 2, true, false)));

    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if (teamManager.getTeam(event.getPlayer()).getId().equalsIgnoreCase("infected")) {
            event.getPlayer().addPotionEffects(Collections.singleton(new PotionEffect(PotionEffectType.JUMP, 10000, 2, true, false)));
        }
    }

    public void broadcastMessage(String msg) {
        Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg)));
    }

    @EventHandler
    public void onBukkitDeath(org.bukkit.event.entity.PlayerDeathEvent event) {
        event.setDeathMessage("");
    }

    @EventHandler
    public void onTeamChange(TeamChangeEvent event) {
        if (teamManager.getTeamById("humans").getMembers().size() == 0 && match.getMatchStatus().equals(MatchStatus.MID)) {
            TGM.get().getMatchManager().endMatch(teamManager.getTeamById("infected"));
        }
        event.getPlayerContext().getPlayer().setGameMode(GameMode.ADVENTURE);

        if (event.getTeam().getId().equalsIgnoreCase("infected")) {
            event.getPlayerContext().getPlayer().addPotionEffects(Collections.singleton(new PotionEffect(PotionEffectType.JUMP, 50000, 1, true, false)));
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        if (teamManager.getTeamById("infected").getMembers().size() == 0 && match.getMatchStatus().equals(MatchStatus.MID)) {
            PlayerContext player = teamManager.getTeamById("humans").getMembers().get(teamManager.getTeamById("humans").getMembers().size() - 1);
            broadcastMessage(String.format("&2&l%s &7has been infected!", player.getPlayer().getName()));

            infect(player.getPlayer());
        }
    }

    //TODO Remove effects and replace with new kit module
    public void infect(Player player) {
        player.getWorld().strikeLightningEffect(player.getLocation());

        teamManager.joinTeam(TGM.get().getPlayerManager().getPlayerContext(player), teamManager.getTeamById("infected"));
        if (teamManager.getTeamById("humans").getMembers().size() > 0) player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lYou have been infected!"));
        player.addPotionEffects(Collections.singleton(new PotionEffect(PotionEffectType.JUMP, 50000, 1, true, false)));
    }

    public void freeze(Player player) {
        player.addPotionEffects(Arrays.asList(
                new PotionEffect(PotionEffectType.SLOW, 10 * 20, 255, true, false),
                new PotionEffect(PotionEffectType.JUMP, 10 * 20, 128, true, false),
                new PotionEffect(PotionEffectType.BLINDNESS, 10 * 20, 255, true, false)
        ));

        Bukkit.getScheduler().runTaskLater(TGM.get(), () -> unfreeze(player), 10 * 20);
    }

    public void unfreeze(Player player) {
        player.removePotionEffect(PotionEffectType.JUMP);
        player.removePotionEffect(PotionEffectType.SLOW);
        player.removePotionEffect(PotionEffectType.BLINDNESS);

        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 50000, 1, true, false));
    }

}
