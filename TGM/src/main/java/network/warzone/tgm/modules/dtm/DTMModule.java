package network.warzone.tgm.modules.dtm;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.modules.monument.Monument;
import network.warzone.tgm.modules.monument.MonumentService;
import network.warzone.tgm.modules.region.Region;
import network.warzone.tgm.modules.region.RegionManagerModule;
import network.warzone.tgm.modules.scoreboard.ScoreboardInitEvent;
import network.warzone.tgm.modules.scoreboard.ScoreboardManagerModule;
import network.warzone.tgm.modules.scoreboard.SimpleScoreboard;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.modules.team.TeamUpdateEvent;
import network.warzone.tgm.modules.time.TimeModule;
import network.warzone.tgm.player.event.PlayerXPEvent;
import network.warzone.tgm.user.PlayerContext;
import network.warzone.tgm.util.ColorConverter;
import network.warzone.tgm.util.FireworkUtil;
import network.warzone.tgm.util.Parser;
import network.warzone.warzoneapi.models.DestroyWoolRequest;
import network.warzone.warzoneapi.models.UserProfile;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class DTMModule extends MatchModule implements Listener {

    @Getter private final List<Monument> monuments = new ArrayList<>();
    private final HashMap<Monument, List<Integer>> monumentScoreboardLines = new HashMap<>();
    private final HashMap<MatchTeam, Integer> teamScoreboardLines = new HashMap<>();

    @Override
    public void load(Match match) {
        JsonObject dtmJson = match.getMapContainer().getMapInfo().getJsonObject().get("dtm").getAsJsonObject();

        for (JsonElement monumentElement : dtmJson.getAsJsonArray("monuments")) {
            JsonObject monumentJson = monumentElement.getAsJsonObject();

            String name = monumentJson.get("name").getAsString();
            Region region = match.getModule(RegionManagerModule.class).getRegion(match, monumentJson.get("region"));
            List<MatchTeam> teams = Parser.getTeamsFromElement(match.getModule(TeamManagerModule.class), monumentJson.get("teams"));
            List<Material> materials = Parser.getMaterialsFromElement(monumentJson.get("materials"));
            int health = monumentJson.get("health").getAsInt();

            monuments.add(new Monument(name, teams, region, materials, health, health));
        }

        TeamManagerModule teamManagerModule = TGM.get().getModule(TeamManagerModule.class);

        //monument services
        for (Monument monument : monuments) {
            String unformattedName = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&' , (monument.getName())));

            monument.addService(new MonumentService() {
                @Override
                public void damage(Player player, Block block) {
                    updateOnScoreboard(monument);
                    block.setType(Material.AIR);

                    MatchTeam matchTeam = teamManagerModule.getTeam(player);
                    Bukkit.broadcastMessage(matchTeam.getColor() + player.getName() + ChatColor.WHITE + " damaged " + monument.getOwners().get(0).getColor() + ChatColor.BOLD + unformattedName);
                    playFireworkEffect(matchTeam.getColor(), block.getLocation());


                    //TODO
                    //for (PlayerContext playerContext : matchTeam.getMembers()) {
                    //    playerContext.getPlayer().playSound(monument.getRegion().getCenter(), Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.MASTER, 1000, 2);
                    //}

                    for (PlayerContext playerContext : monument.getOwners().get(0).getMembers()) {
                        playerContext.getPlayer().playSound(monument.getRegion().getCenter(), Sound.ENTITY_IRON_GOLEM_ATTACK, SoundCategory.MASTER, 1000, 1);
                    }

                    if (TGM.get().getApiManager().isStatsDisabled()) return;

                    PlayerContext playerContext = TGM.get().getPlayerManager().getPlayerContext(player);
                    playerContext.getUserProfile().addWoolDestroy();
                    Bukkit.getPluginManager().callEvent(new PlayerXPEvent(playerContext, UserProfile.XP_PER_WOOL_BREAK, playerContext.getUserProfile().getXP() - UserProfile.XP_PER_WOOL_BREAK, playerContext.getUserProfile().getXP()));
                    Bukkit.getScheduler().runTaskAsynchronously(TGM.get(), () -> TGM.get().getTeamClient().destroyWool(new DestroyWoolRequest(player.getUniqueId())));

                }

                @Override
                public void destroy(Player player, Block block) {
                    updateOnScoreboard(monument);
                    block.setType(Material.AIR);

                    MatchTeam matchTeam = teamManagerModule.getTeam(player);
                    Bukkit.broadcastMessage(matchTeam.getColor() + player.getName() + ChatColor.WHITE + " destroyed " + monument.getOwners().get(0).getColor() + ChatColor.BOLD + unformattedName);
                    playFireworkEffect(matchTeam.getColor(), block.getLocation());

                    for (MatchTeam owner : monument.getOwners()) {
                        if (getAliveMonuments(owner).isEmpty()) {
                            TGM.get().getMatchManager().endMatch(matchTeam);
                            break;
                        }
                    }

                    if (TGM.get().getApiManager().isStatsDisabled()) return;
                    PlayerContext playerContext = TGM.get().getPlayerManager().getPlayerContext(player);
                    playerContext.getUserProfile().addWoolDestroy();
                    Bukkit.getPluginManager().callEvent(new PlayerXPEvent(playerContext, UserProfile.XP_PER_WOOL_BREAK, playerContext.getUserProfile().getXP() - UserProfile.XP_PER_WOOL_BREAK, playerContext.getUserProfile().getXP()));
                    Bukkit.getScheduler().runTaskAsynchronously(TGM.get(), () -> TGM.get().getTeamClient().destroyWool(new DestroyWoolRequest(player.getUniqueId())));
                }
            });
        }

        //load monuments
        for (Monument monument : monuments) {
            monument.load();
        }
        TGM.get().getModule(TimeModule.class).setTimeLimitService(this::getHighestHealthTeam);
    }

    private void playFireworkEffect(ChatColor color, Location location) {
        FireworkUtil.spawnFirework(location, FireworkEffect.builder()
                .with(FireworkEffect.Type.BURST)
                .withFlicker()
                .withColor(ColorConverter.getColor(color))
                .build(), 0);

        // Play the sound for the player if they are too far to render the firework.
        //for (Player listener : Bukkit.getOnlinePlayers()) {
        //    if (listener.getLocation().distance(location) > 64) {
        //        listener.playSound(listener.getLocation(), Sound.ENTITY_FIREWORK_BLAST, 0.75f, 1f);
        //        listener.playSound(listener.getLocation(), Sound.ENTITY_FIREWORK_TWINKLE, 0.75f, 1f);
        //    }
        //}
    }

    @EventHandler
    public void onScoreboardInit(ScoreboardInitEvent event) {
        List<MatchTeam> teams = TGM.get().getModule(TeamManagerModule.class).getTeams();

        int spaceCount = 1;
        int i = 0;
        for (MatchTeam matchTeam : teams) {
            if(matchTeam.isSpectator()) continue;

            for (Monument monument : monuments) {
                if (monument.getOwners().contains(matchTeam)) {
                    if (monumentScoreboardLines.containsKey(monument)) {
                        monumentScoreboardLines.get(monument).add(i);
                    } else {
                        List<Integer> list = new ArrayList<>();
                        list.add(i);
                        monumentScoreboardLines.put(monument, list);
                    }

                    event.getSimpleScoreboard().add(getScoreboardString(monument), i++);
                }
            }
            event.getSimpleScoreboard().add(getTeamScoreboardString(matchTeam), i);
            teamScoreboardLines.put(matchTeam, i++);

            if (teams.indexOf(matchTeam) < teams.size() - 1) {
                event.getSimpleScoreboard().add(StringUtils.repeat(" ", spaceCount++), i++);
            }
        }
    }

    @EventHandler
    public void onTeamUpdate(TeamUpdateEvent event) {
        for (MatchTeam matchTeam : teamScoreboardLines.keySet()) {
            if (event.getMatchTeam() == matchTeam) {
                int i = teamScoreboardLines.get(matchTeam);

                for (SimpleScoreboard simpleScoreboard : TGM.get().getModule(ScoreboardManagerModule.class).getScoreboards().values()) {
                    simpleScoreboard.remove(i);
                    simpleScoreboard.add(getTeamScoreboardString(matchTeam), i);
                    simpleScoreboard.update();
                }
            }
        }
    }

    private void updateOnScoreboard(Monument monument) {
        ScoreboardManagerModule scoreboardManagerModule = TGM.get().getModule(ScoreboardManagerModule.class);

        for (int i : monumentScoreboardLines.get(monument)) {
            for (SimpleScoreboard simpleScoreboard : scoreboardManagerModule.getScoreboards().values()) {
                simpleScoreboard.remove(i);
                simpleScoreboard.add(getScoreboardString(monument), i);
                simpleScoreboard.update();
            }
        }
    }

    private MatchTeam getHighestHealthTeam() {
        Map<MatchTeam, Integer> teams = new HashMap<>(); // team, health
        for (Monument monument : monuments) {
            for (MatchTeam team : monument.getOwners()) {
                teams.put(team, teams.getOrDefault(team, 0) + monument.getHealth());
            }
        }

        MatchTeam highest = null;
        for (Map.Entry<MatchTeam, Integer> team : teams.entrySet()) {
            if (highest == null) {
                highest = team.getKey();
                continue;
            }
            if (teams.get(highest) < team.getValue()) {
                highest = team.getKey();
            }
        }

        if (highest != null) {
            final MatchTeam team = highest;
            int amount = teams.entrySet().stream().filter(entry -> teams.get(team) == entry.getValue()).collect(Collectors.toList()).size();
            if (amount > 1) return null;
            else return team;
        }
        return null;
    }

    private String getTeamScoreboardString(MatchTeam matchTeam) {
        return matchTeam.getColor() + matchTeam.getAlias();
    }

    private String getScoreboardString(Monument monument) {
        if (monument.isAlive()) {
            int percentage = monument.getHealthPercentage();

            if (percentage > 70) {
                return "  " + ChatColor.GREEN.toString() + percentage + "% " + ChatColor.WHITE + monument.getName();
            } else if (percentage > 40) {
                return "  " + ChatColor.YELLOW.toString() + percentage + "% " + ChatColor.WHITE + monument.getName();
            } else {
                return "  " + ChatColor.RED.toString() + percentage + "% " + ChatColor.WHITE + monument.getName();
            }
        } else {
            return "  " + ChatColor.STRIKETHROUGH + monument.getName();
        }
    }

    public List<Monument> getAliveMonuments(MatchTeam matchTeam) {
        List<Monument> alive = new ArrayList<>();
        for (Monument monument : monuments) {
            if (monument.isAlive() && monument.getOwners().contains(matchTeam)) {
                alive.add(monument);
            }
        }
        return alive;
    }

    @Override
    public void unload() {
        monuments.forEach(Monument::unload);

        monuments.clear();
    }
}
