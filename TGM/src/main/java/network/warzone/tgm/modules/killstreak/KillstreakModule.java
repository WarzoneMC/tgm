package network.warzone.tgm.modules.killstreak;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import network.warzone.tgm.damage.grave.event.PlayerDeathEvent;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.modules.DeathModule;
import network.warzone.tgm.util.ColorConverter;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

/**
 * Created by MatrixTunnel on 10/3/2017.
 */
@Getter
public class KillstreakModule extends MatchModule implements Listener {

    private DeathModule deathModule;

    private final Map<String, Integer> players = new HashMap<>(); // String is player's uuid
    private final List<Killstreak> killstreaks = new ArrayList<>();

    public KillstreakModule() {

    }

    @Override
    public void load(Match match) {
        deathModule = match.getModule(DeathModule.class);

        if (!match.getMapContainer().getMapInfo().getJsonObject().has("killstreaks")) { // Default
            killstreaks.addAll(Arrays.asList(
                new Killstreak()
                        .setCount(5)
                        .setMessage("%killercolor%%killername% &7is on a kill streak of &2&l%count%&r&7!")
                        .setCommands(Collections.singletonList(
                                "execute %killername% ~ ~ ~ playsound entity.zombie.death master @a ~ ~ ~ 3"
                        )),

                new Killstreak()
                        .setCount(10)
                        .setMessage("%killercolor%%killername% &7is on a kill streak of &6&l%count%&r&7!")
                        .setCommands(Arrays.asList(
                                "execute %killername% ~ ~ ~ playsound entity.wither.ambient master @a ~ ~ ~ 7",
                                "execute %killername% ~ ~ ~ summon fireworks_rocket ~ ~ ~ {LifeTime:0,FireworksItem:{id:fireworks,Count:1,tag:{Fireworks:{Explosions:[{Type:4,Colors:[I;16711680],FadeColors:[I;9371648]}]}}}}"
                        )),

                new Killstreak()
                        .setCount(25)
                        .setMessage("%killercolor%%killername% &7is on a kill streak of &c&l%count%&r&7!")
                        .setCommands(Arrays.asList(
                                "execute %killername% ~ ~ ~ playsound entity.enderdragon.growl master @a ~ ~ ~ 1000",
                                "execute %killername% ~ ~ ~ summon fireworks_rocket ~ ~ ~ {LifeTime:0,FireworksItem:{id:fireworks,Count:1,tag:{Fireworks:{Explosions:[{Type:0,Colors:[I;16711680],FadeColors:[I;9371648]}]}}}}"
                        )),

                new Killstreak()
                        .setCount(50)
                        .setMessage("%killercolor%%killername% &7is on a kill streak of &3&l%count%&r&7!")
                        .setCommands(Arrays.asList(
                                "execute %killername% ~ ~ ~ playsound entity.wither.spawn master @a ~ ~ ~ 1000 1.4", // 1.4 so it doesn't sound the same as the game end sound
                                "execute %killername% ~ ~ ~ summon fireworks_rocket ~ ~ ~ {LifeTime:0,FireworksItem:{id:fireworks,Count:1,tag:{Fireworks:{Explosions:[{Type:1,Colors:[I;16711680],FadeColors:[I;9371648]}]}}}}"
                        )),

                new Killstreak()
                        .setCount(100)
                        .setMessage("%killercolor%%killername% &7is on a kill streak of &5&l%count%&r&7!")
                        .setCommands(Arrays.asList(
                                "execute @a ~ ~ ~ playsound ui.toast.challenge_complete master @p ~ ~100 ~ 1000",
                                "execute %killername% ~ ~ ~ summon fireworks_rocket ~ ~ ~ {LifeTime:0,FireworksItem:{id:fireworks,Count:1,tag:{Fireworks:{Explosions:[{Type:2,Colors:[I;16766776],FadeColors:[I;16774912]}]}}}}"//,
                                //"ban %killername% &c&lDETECTED FOR KILL FARMING&r" // :facepalm:
                        ))
            ));
        } else { // If json contains killstreaks, read it
            for (JsonElement streakElement : match.getMapContainer().getMapInfo().getJsonObject().getAsJsonArray("killstreaks")) {
                Killstreak killstreak = new Killstreak();

                JsonObject streakJson = streakElement.getAsJsonObject();

                if (streakJson.has("count")) {
                    killstreak.setCount(streakJson.get("count").getAsInt());
                }

                if (streakJson.has("message")) {
                    killstreak.setMessage(streakJson.get("message").getAsString());
                }

                if (streakJson.has("commands")) {
                    killstreak.setCommands(new ArrayList<String>(){{
                        streakJson.getAsJsonArray("commands").forEach(jsonElement -> add(jsonElement.getAsString()));
                    }});
                }

                if (streakJson.has("repeat")) {
                    killstreak.setRepeat(streakJson.get("repeat").getAsBoolean());
                }

                killstreaks.add(killstreak);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST) // DeathMessageModule sets killer to null so this has to be first
    public void onKill(PlayerDeathEvent event) {
        DeathModule module = deathModule.getPlayer(event.getPlayer());

        if (module.getKiller() == null) {
            if (players.getOrDefault(module.getPlayer().getUniqueId().toString(), 0) >= 5) {
                Bukkit.broadcastMessage(ColorConverter.filterString(
                        module.getPlayerTeam().getColor().toString() + module.getPlayerName() + "&7" + (module.getPlayerName().endsWith("s") ? "'" : "'s") +
                                " kill streak of &c&l" + players.get(module.getPlayer().getUniqueId().toString() + "&r&7 was shutdown")
                ));
            }

            players.put(module.getPlayer().getUniqueId().toString(), 0);
            return;
        }

        if (module.getKillerTeam().isSpectator()) return; // Stupid spectators

        String killerUuid = module.getKiller().getUniqueId().toString();
        String killedUuid = module.getPlayer().getUniqueId().toString();

        players.put(killerUuid, players.getOrDefault(killerUuid, 0) + 1);

        if (players.get(killedUuid) != null && players.get(killedUuid) >= 5) {
            Bukkit.broadcastMessage(ColorConverter.filterString(
                    module.getKillerTeam().getColor().toString() + module.getKillerName() + " &7shutdown " +
                    module.getPlayerTeam().getColor().toString() + module.getPlayerName() + "&7" + (module.getPlayerName().endsWith("s") ? "'" : "'s") + " kill streak of &c&l" + players.get(killedUuid)
            ));

        }

        players.put(killedUuid, 0);

        killstreaks.forEach(killstreak -> {
            if (!killstreak.isRepeat() && players.get(killerUuid) == killstreak.getCount() || killstreak.isRepeat() && players.get(killerUuid) % killstreak.getCount() == 0) {
                Bukkit.broadcastMessage(ColorConverter.filterString(killstreak.getMessage())
                        .replace("%killername%", module.getKillerName())
                        .replace("%killercolor%", module.getKillerTeam().getColor().toString())
                        .replace("%killedname%", module.getPlayerName())
                        .replace("%count%", String.valueOf(killstreak.getCount()))
                );

                killstreak.getCommands().forEach(s -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ColorConverter.filterString(s)
                        .replace("%killername%", module.getKillerName())
                        .replace("%killercolor%", module.getKillerTeam().getColor().toString())
                        .replace("%killedname%", module.getPlayerName())
                        .replace("%count%", String.valueOf(killstreak.getCount())))
                );
            }
        });
    }

    public void unload() {
        players.clear();
        killstreaks.clear();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        players.remove(event.getPlayer().getUniqueId().toString());
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        players.remove(event.getPlayer().getUniqueId().toString());
    }
}
