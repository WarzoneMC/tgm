package network.warzone.tgm.modules.infection;

import com.google.gson.JsonObject;
import lombok.Getter;
import network.warzone.tgm.TGM;
import network.warzone.tgm.api.ApiManager;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.match.MatchStatus;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamChangeEvent;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.modules.time.TimeModule;
import network.warzone.tgm.user.PlayerContext;
import network.warzone.tgm.util.Players;
import network.warzone.warzoneapi.models.Death;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

/**
 * Created by Draem on 7/31/2017.
 */
@Getter
public class InfectionModule extends MatchModule implements Listener {

    private Match match;
    private TeamManagerModule teamManager;

    private int length;

    @Override
    public void load(Match match) {
        TGM.registerEvents(this);
        JsonObject json = match.getMapContainer().getMapInfo().getJsonObject().get("infection").getAsJsonObject();
        length = json.get("length").getAsInt();
        teamManager = match.getModule(TeamManagerModule.class);
        this.match = match;

        TGM.get().getModule(TimeModule.class).setTimeLimitService(this::getWinningTeam);
    }

    @Override
    public void enable() {
        int players = teamManager.getTeamById("humans").getMembers().size();
        int zombies = ((int) (players * (5 / 100.0F)) == 0 ? 1 : (int) (players * (5 / 100.0F))) - teamManager.getTeamById("infected").getMembers().size();
        if (zombies > 0) {
            for (int i = 0; i < zombies; i++) {
                PlayerContext player = teamManager.getTeamById("humans").getMembers().get(new Random().nextInt(teamManager.getTeamById("humans").getMembers().size()));
                broadcastMessage(String.format("&2&l%s &7has been infected!", player.getPlayer().getName()));

                infect(player.getPlayer());
                freeze(player.getPlayer());
            }
        }

        for (MatchTeam team : teamManager.getTeams()) {
            team.getMembers().forEach(playerContext -> playerContext.getPlayer().setGameMode(GameMode.ADVENTURE));
        }

        match.getModule(InfectedTimeLimit.class).startCountdown(length);
    }

    private MatchTeam getWinningTeam() {
        return teamManager.getTeamByAlias("humans");
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
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        Player killer = (Player) event.getDamager();

        if (!TGM.get().getMatchManager().getMatch().getMatchStatus().equals(MatchStatus.MID) || teamManager.getTeam(player).isSpectator() ||
                player.getHealth() - event.getFinalDamage() >= 0.5) return;

        event.setDamage(0);

        MatchTeam humans = teamManager.getTeamById("humans");
        MatchTeam infected = teamManager.getTeamById("infected");

        if (humans.containsPlayer(player)) {
            if (event.getDamager() instanceof Player) {
                broadcastMessage(String.format("%s%s &7has been infected by %s%s",
                        teamManager.getTeam(player).getColor(),
                        event.getEntity().getName(),
                        teamManager.getTeam(killer).getColor(),
                        killer.getName()));
            } else {
                broadcastMessage(String.format("%s%s &7has been taken by the environment",
                        teamManager.getTeam(player).getColor(),
                        event.getEntity().getName()));
            }

            infect(player);
        } else if (infected.containsPlayer(player)) {
            if (event.getDamager() instanceof Player) {
                broadcastMessage(String.format("%s%s &7has been slain by %s%s",
                        teamManager.getTeam(player).getColor(),
                        event.getEntity().getName(),
                        teamManager.getTeam(killer).getColor(),
                        killer.getName()
                ));
            }
            refresh(TGM.get().getPlayerManager().getPlayerContext(player), teamManager.getTeam(player));
        }


        if (TGM.get().getApiManager().isStatsDisabled())
            return;

        PlayerContext playerContext = TGM.get().getPlayerManager().getPlayerContext(player);

        String playerItem = "";
        if (player.getInventory().getItemInMainHand() != null) {
            playerItem = player.getInventory().getItemInMainHand().getType().toString();
        }

        String killerItem = "";
        String killerId = TGM.get().getPlayerManager().getPlayerContext(killer).getUserProfile().getId().toString();
        if (killer.getInventory().getItemInMainHand() != null) {
            killerItem = killer.getInventory().getItemInMainHand().getType().toString();
        }

        ApiManager api = TGM.get().getApiManager();
        Death death = new Death(playerContext.getUserProfile().getId().toString(), killerId, playerItem, killerItem,
                api.getMatchInProgress().getMap(), api.getMatchInProgress().getId());

        Bukkit.getScheduler().runTaskAsynchronously(TGM.get(), () -> TGM.get().getTeamClient().addKill(death));
    }

    private void refresh(PlayerContext playerContext, MatchTeam matchTeam) {
        Players.reset(playerContext.getPlayer(), true);

        Bukkit.getScheduler().runTaskLater(TGM.get(), () -> {
            matchTeam.getKits().forEach(kit -> kit.apply(playerContext.getPlayer(), matchTeam));
            playerContext.getPlayer().updateInventory();
            playerContext.getPlayer().teleport(matchTeam.getSpawnPoints().get(0).getLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
            playerContext.getPlayer().setGameMode(GameMode.ADVENTURE);
            playerContext.getPlayer().addPotionEffects(Collections.singleton(new PotionEffect(PotionEffectType.JUMP, 10000, 2, true, false)));
        }, 1L);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if (teamManager.getTeam(event.getPlayer()).getId().equalsIgnoreCase("infected")) {
            event.getPlayer().addPotionEffects(Collections.singleton(new PotionEffect(PotionEffectType.JUMP, 10000, 2, true, false)));
        }
        event.getPlayer().setGameMode(GameMode.ADVENTURE);
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
        if (teamManager.getTeamById("humans").getMembers().size() > 0)
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lYou have been infected!"));
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
