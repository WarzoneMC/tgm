package network.warzone.tgm.modules.dtm;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.modules.CraftingModule;
import network.warzone.tgm.modules.monument.Monument;
import network.warzone.tgm.modules.monument.MonumentService;
import network.warzone.tgm.modules.region.Region;
import network.warzone.tgm.modules.region.RegionManagerModule;
import network.warzone.tgm.modules.scoreboard.ScoreboardInitEvent;
import network.warzone.tgm.modules.scoreboard.ScoreboardManagerModule;
import network.warzone.tgm.modules.scoreboard.SimpleScoreboard;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.modules.team.event.TeamUpdateAliasEvent;
import network.warzone.tgm.modules.time.TimeModule;
import network.warzone.tgm.player.event.PlayerXPEvent;
import network.warzone.tgm.user.PlayerContext;
import network.warzone.tgm.util.ColorConverter;
import network.warzone.tgm.util.FireworkUtil;
import network.warzone.tgm.util.Parser;
import network.warzone.warzoneapi.models.DestroyWoolRequest;
import network.warzone.warzoneapi.models.UserProfile;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.stream.Collectors;

import static org.bukkit.SoundCategory.AMBIENT;

@Getter
public class DTMModule extends MatchModule implements Listener {

    private static final String SYMBOL_MONUMENT_INCOMPLETE = "\u2715"; // ✕
    private static final String SYMBOL_MONUMENT_COMPLETE = "\u2714"; // ✔

    @Getter private final List<Monument> monuments = new ArrayList<>();
    private final HashMap<Monument, List<Integer>> monumentScoreboardLines = new HashMap<>();
    private final HashMap<String, Integer> teamScoreboardLines = new HashMap<>();

    private TeamManagerModule teamManagerModule;
    private ScoreboardManagerModule scoreboardManagerModule;

    @Override
    public void load(Match match) {
        this.teamManagerModule = match.getModule(TeamManagerModule.class);
        this.scoreboardManagerModule = match.getModule(ScoreboardManagerModule.class);

        JsonObject dtmJson = match.getMapContainer().getMapInfo().getJsonObject().get("dtm").getAsJsonObject();

        for (JsonElement monumentElement : dtmJson.getAsJsonArray("monuments")) {
            JsonObject monumentJson = monumentElement.getAsJsonObject();

            String name = monumentJson.get("name").getAsString();
            Region region = match.getModule(RegionManagerModule.class).getRegion(match, monumentJson.get("region"));
            List<MatchTeam> teams = match.getModule(TeamManagerModule.class).getTeams(monumentJson.get("teams").getAsJsonArray());
            List<Material> materials = Parser.getMaterialsFromElement(monumentJson.get("materials"));
            int health = monumentJson.get("health").getAsInt();

            this.monuments.add(new Monument(new WeakReference<>(match), name, teams, region, materials, health, health));
            if (materials == null) {
                continue;
            }
            for (Material material : materials) {
                if (material.name().contains("WOOL")) {
                    match.getModule(CraftingModule.class).addRemovedRecipe(Material.SHEARS);
                    break;
                }
            }
        }


        //monument services
        for (Monument monument : this.monuments) {
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
                        playerContext.getPlayer().playSound(monument.getRegion().getCenter(), Sound.ENTITY_IRON_GOLEM_ATTACK, AMBIENT, 1000, 1);
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
        for (Monument monument : this.monuments) {
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
        SimpleScoreboard simpleScoreboard = event.getSimpleScoreboard();
        simpleScoreboard.setTitle(ChatColor.AQUA + "Destroy the Monument");
        int spaceCount = 1;
        int i = 2;
        for (int j = teams.size() - 1; j >= 0; j--) {
            MatchTeam matchTeam = teams.get(j);
            if(matchTeam.isSpectator()) continue;

            for (int k = this.monuments.size() - 1; k >= 0; k--) {
                Monument monument = this.monuments.get(k);
                if (!monument.getOwners().contains(matchTeam)) {
                    if (this.monumentScoreboardLines.containsKey(monument)) {
                        this.monumentScoreboardLines.get(monument).add(i);
                    } else {
                        List<Integer> list = new ArrayList<>();
                        list.add(i);
                        this.monumentScoreboardLines.put(monument, list);
                    }

                    simpleScoreboard.add(getScoreboardString(monument), i++);
                }
            }
            simpleScoreboard.add(getTeamScoreboardString(matchTeam), i);
            this.teamScoreboardLines.put(matchTeam.getId(), i++);

            if (j > 1) {
                simpleScoreboard.add(StringUtils.repeat(" ", spaceCount++), i++);
            }
        }
    }

    @EventHandler
    public void onTeamUpdate(TeamUpdateAliasEvent event) {
        Set<String> teamIds = this.teamScoreboardLines.keySet();
        Set<MatchTeam> matchTeams = teamIds.stream().map(teamManagerModule::getTeamById).collect(Collectors.toSet());

        for (MatchTeam matchTeam : matchTeams) {
            if (event.getMatchTeam() == matchTeam) {
                int i = this.teamScoreboardLines.get(matchTeam.getId());

                for (SimpleScoreboard simpleScoreboard : this.scoreboardManagerModule.getScoreboards().values()) {
                    simpleScoreboard.remove(i);
                    simpleScoreboard.add(getTeamScoreboardString(matchTeam), i);
                    simpleScoreboard.update();
                }
            }
        }
    }

    private void updateOnScoreboard(Monument monument) {
        for (int i : this.monumentScoreboardLines.get(monument)) {
            for (SimpleScoreboard simpleScoreboard : this.scoreboardManagerModule.getScoreboards().values()) {
                simpleScoreboard.remove(i);
                simpleScoreboard.add(getScoreboardString(monument), i);
                simpleScoreboard.update();
            }
        }
    }

    private MatchTeam getHighestHealthTeam() {
        Map<MatchTeam, Integer> teams = new HashMap<>(); // team, health
        for (Monument monument : this.monuments) {
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
            long amount = teams.entrySet().stream().filter(entry -> teams.get(team).equals( entry.getValue())).count();
            if (amount > 1) return null;
            else return team;
        }
        return null;
    }

    private String getTeamScoreboardString(MatchTeam matchTeam) {
        return matchTeam.getColor() + matchTeam.getAlias();
    }

    private String getScoreboardString(Monument monument) {
        int percentage = 100 - monument.getHealthPercentage();
        ChatColor healthColor = ChatColor.YELLOW;
        if (percentage <= 0) {
            healthColor = ChatColor.RED;
        } else if (percentage >= 100) {
            healthColor = ChatColor.GREEN;
        }

        if (monument.getMaxHealth() == 1 || percentage >= 100) {
            String symbol = ChatColor.GREEN + SYMBOL_MONUMENT_COMPLETE;
            if (percentage <= 0) symbol = ChatColor.RED + SYMBOL_MONUMENT_INCOMPLETE;

            return ChatColor.GRAY + "  " + symbol + " " + ChatColor.WHITE + monument.getName();
        }

        return healthColor + "  " + percentage + "% " + ChatColor.WHITE + monument.getName();
    }

    private List<Monument> getAliveMonuments(MatchTeam matchTeam) {
        List<Monument> alive = new ArrayList<>();
        for (Monument monument : this.monuments) {
            if (monument.isAlive() && monument.getOwners().contains(matchTeam)) {
                alive.add(monument);
            }
        }
        return alive;
    }

    @Override
    public void unload() {
        this.monuments.forEach(Monument::unload);

        this.monuments.clear();
    }
}
