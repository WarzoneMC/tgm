package network.warzone.tgm.modules.killstreak;

import lombok.Getter;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.modules.death.DeathInfo;
import network.warzone.tgm.modules.death.DeathModule;
import network.warzone.tgm.modules.team.TeamChangeEvent;
import network.warzone.tgm.player.event.TGMPlayerDeathEvent;
import network.warzone.tgm.util.ColorConverter;
import org.bukkit.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.lang.ref.WeakReference;
import java.util.*;

/**
 * Created by MatrixTunnel on 10/3/2017.
 */
@Getter
public class KillstreakModule extends MatchModule implements Listener {

    private WeakReference<Match> match;

    private DeathModule deathModule;

    private final Map<String, Integer> players = new HashMap<>(); // String is player's uuid
    private final Set<Killstreak> killstreaks = new HashSet<>();

    @Override
    public void load(Match match) {
        this.match = new WeakReference<Match>(match);
        deathModule = match.getModule(DeathModule.class);
        this.addDefaults();
        if (match.getMapContainer().getMapInfo().getJsonObject().has("killstreaks")) {
            Killstreak[] parsedKillstreaks = TGM.get().getGson().fromJson(match.getMapContainer().getMapInfo().getJsonObject().getAsJsonArray("killstreaks"), Killstreak[].class);
            Collections.addAll(this.killstreaks, parsedKillstreaks);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST) // DeathMessageModule sets killer to null so this has to be first
    public void onKill(TGMPlayerDeathEvent event) {
        DeathInfo deathInfo = deathModule.getPlayer(event.getVictim());

        if (deathInfo.killer == null) {
            if (players.getOrDefault(deathInfo.player.getUniqueId().toString(), 0) >= 5) {
                Bukkit.broadcastMessage(ColorConverter.filterString(
                        deathInfo.playerTeam.getColor().toString() + deathInfo.playerName + "&7" + (deathInfo.playerName.endsWith("s") ? "'" : "'s") +
                                " kill streak of &c&l" + players.get(deathInfo.player.getUniqueId().toString()) + "&r&7 was shutdown"
                ));
            }

            players.put(deathInfo.player.getUniqueId().toString(), 0);
            return;
        }

        if (deathInfo.killerTeam.isSpectator()) return; // Stupid spectators

        String killerUuid = deathInfo.killer.getUniqueId().toString();
        String killedUuid = deathInfo.player.getUniqueId().toString();

        players.put(killerUuid, players.getOrDefault(killerUuid, 0) + 1);

        if (players.get(killedUuid) != null && players.get(killedUuid) >= 5) {
            Bukkit.broadcastMessage(ColorConverter.filterString(
                    deathInfo.killerTeam.getColor().toString() + deathInfo.killerName + " &7shutdown " +
                            deathInfo.playerTeam.getColor().toString() + deathInfo.playerName + "&7" + (deathInfo.playerName.endsWith("s") ? "'" : "'s") + " kill streak of &c&l" + players.get(killedUuid)
            ));

        }

        players.put(killedUuid, 0);

        killstreaks.forEach(killstreak -> {
            if (!killstreak.isRepeat() && players.get(killerUuid) == killstreak.getCount() || killstreak.isRepeat() && players.get(killerUuid) % killstreak.getCount() == 0) {
                if (killstreak.getMessage() != null && !killstreak.getMessage().isEmpty())
                    Bukkit.broadcastMessage(ColorConverter.filterString(killstreak.getMessage())
                            .replace("%killername%", deathInfo.killerName)
                            .replace("%killercolor%", deathInfo.killerTeam.getColor().toString())
                            .replace("%killedname%", deathInfo.playerName)
                            .replace("%count%", String.valueOf(killstreak.getCount()))
                    );

                killstreak.getActions().forEach(a -> a.apply(deathInfo.killer));

                killstreak.getCommands().forEach(s -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ColorConverter.filterString(s)
                        .replace("%killername%", deathInfo.killerName)
                        .replace("%killercolor%", deathInfo.killerTeam.getColor().toString())
                        .replace("%killedname%", deathInfo.playerName)
                        .replace("%count%", String.valueOf(killstreak.getCount())))
                );
            }
        });
    }

    public int getKillstreak(String uuid) {
        return players.getOrDefault(uuid, 0);
    }

    public void unload() {
        players.clear();
        killstreaks.clear();
    }

    @EventHandler
    public void onTeamChange(TeamChangeEvent event) {
        if (event.isCancelled()) return;
        if (players.containsKey(event.getPlayerContext().getPlayer().getUniqueId().toString()) && players.get(event.getPlayerContext().getPlayer().getUniqueId().toString()) >= 5) {
            int streakValue = players.get(event.getPlayerContext().getPlayer().getUniqueId().toString());
            event.getPlayerContext().getPlayer().sendMessage(ColorConverter.filterString("&7You have lost your kill streak of &c&l") + streakValue + ColorConverter.filterString(" &7for switching teams."));
            players.remove(event.getPlayerContext().getPlayer().getUniqueId().toString());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        players.remove(event.getPlayer().getUniqueId().toString());
    }

    private void addDefaults() {
        killstreaks.addAll(Arrays.asList( //TODO Add option to disable it
                new Killstreak()
                        .setCount(5)
                        .setMessage("%killercolor%%killername% &7is on a kill streak of &2&l%count%&r&7!")
                        .setActions(Collections.singletonList(
                                new SoundKillstreakAction(Sound.ENTITY_ZOMBIE_DEATH, SoundKillstreakAction.SoundTarget.EVERYONE, 3.0F, 1.0F)
                        )),

                new Killstreak()
                        .setCount(10)
                        .setMessage("%killercolor%%killername% &7is on a kill streak of &6&l%count%&r&7!")
                        .setActions(Arrays.asList(
                                new SoundKillstreakAction(Sound.ENTITY_WITHER_AMBIENT, SoundKillstreakAction.SoundTarget.EVERYONE, 7.0F, 1.0F),
                                new FireworkKillstreakAction(new Location(match.get().getWorld(), 0.0, 0.0, 0.0), FireworkEffect.builder().with(FireworkEffect.Type.CREEPER).withColor(Color.RED).withFade(Color.fromRGB(9371648)).build(), 0)
                        )),

                new Killstreak()
                        .setCount(25)
                        .setMessage("%killercolor%%killername% &7is on a kill streak of &c&l%count%&r&7!")
                        .setActions(Arrays.asList(
                                new SoundKillstreakAction(Sound.ENTITY_ENDER_DRAGON_GROWL, SoundKillstreakAction.SoundTarget.EVERYONE, 1000.0F, 1.0F),
                                new FireworkKillstreakAction(new Location(match.get().getWorld(), 0.0, 0.0, 0.0), FireworkEffect.builder().with(FireworkEffect.Type.BALL).withColor(Color.RED).withFade(Color.fromRGB(9371648)).build(), 0)
                        )),

                new Killstreak()
                        .setCount(50)
                        .setMessage("%killercolor%%killername% &7is on a kill streak of &3&l%count%&r&7!")
                        .setActions(Arrays.asList(
                                new SoundKillstreakAction(Sound.ENTITY_WITHER_SPAWN, SoundKillstreakAction.SoundTarget.EVERYONE, 1000.0F, 1.4F),
                                new FireworkKillstreakAction(new Location(match.get().getWorld(), 0.0, 0.0, 0.0), FireworkEffect.builder().with(FireworkEffect.Type.BALL_LARGE).withColor(Color.RED).withFade(Color.fromRGB(9371648)).build(), 0)
                        )),

                new Killstreak()
                        .setCount(100)
                        .setMessage("%killercolor%%killername% &7is on a kill streak of &5&l%count%&r&7!")
                        .setActions(Arrays.asList(
                                new SoundKillstreakAction(Sound.UI_TOAST_CHALLENGE_COMPLETE, SoundKillstreakAction.SoundTarget.PLAYER, 1000.0F, 1.0F),
                                new FireworkKillstreakAction(new Location(match.get().getWorld(), 0.0, 0.0, 0.0), FireworkEffect.builder().with(FireworkEffect.Type.STAR).withColor(Color.fromRGB(16766776)).withFade(Color.fromRGB(16774912)).build(), 0)
                        ))
        ));
    }
}
