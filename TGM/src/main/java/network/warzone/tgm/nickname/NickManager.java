package network.warzone.tgm.nickname;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.Getter;
import network.warzone.tgm.TGM;
import network.warzone.tgm.modules.SpectatorModule;
import network.warzone.tgm.modules.scoreboard.ScoreboardManagerModule;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.modules.visibility.VisibilityController;
import network.warzone.tgm.modules.visibility.VisibilityControllerImpl;
import network.warzone.tgm.user.PlayerContext;
import network.warzone.tgm.util.HashMaps;
import network.warzone.warzoneapi.models.MojangProfile;
import network.warzone.warzoneapi.models.Rank;
import network.warzone.warzoneapi.models.Skin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.*;

public class NickManager {

    private VisibilityController visiblityController;

    @Getter
    private HashMap<UUID, String> originalNames = new HashMap<>();

    @Getter
    private HashMap<UUID, String> nickNames = new HashMap<>();

    @Getter
    private HashMap<UUID, Skin> skins = new HashMap<>();

    @Getter
    private HashMap<UUID, NickedUserProfile> stats = new HashMap<>();

    @Getter
    private List<QueuedNick> queuedNicks = new ArrayList<>();

    private ProfileCache profileCache = ProfileCache.getInstance();

    public NickManager() {
        visiblityController = new VisibilityControllerImpl(TGM.get().getModule(SpectatorModule.class));
    }

    public void addQueuedNick(Player player, String newName) {
        Bukkit.getScheduler().runTaskAsynchronously(TGM.get(), () -> {
            UUID nickedUUID = getUUID(newName);
            Skin skin;
            if (nickedUUID != null) {
                skin = getSkin(nickedUUID);
            } else {
                skin = null; // TODO: Default to a random player skin
            }
            queuedNicks.add(new QueuedNick(newName, skin, player));
        });
    }

    public String getOriginalName(String username) {
        if (nickNames.containsValue(username)) {
            UUID uuid = HashMaps.reverseGetFirst(username, nickNames);
            return originalNames.get(uuid);
        } else {
            return username;
        }
    }

    public Optional<QueuedNick> getQueuedNick(Player player) {
        return queuedNicks.stream().filter(queuedNick -> queuedNick.getPlayer().getUniqueId().equals(player.getUniqueId())).findFirst();
    }

    public void setNick(Player player, String newName) throws NoSuchFieldException, IllegalAccessException {
        setName(player, newName);
        setSkin(player, newName);
    }

    public void reset(Player player, boolean kick) throws NoSuchFieldException, IllegalAccessException {
        if (kick) {
            originalNames.remove(player.getUniqueId());
            nickNames.remove(player.getUniqueId());
            skins.remove(player.getUniqueId());
            stats.remove(player.getUniqueId());
            player.kickPlayer(ChatColor.RED + "Resetting nickname");
        } else {
            String originalName = originalNames.get(player.getUniqueId());
            setName(player, originalName);
            setSkin(player, player.getUniqueId());
        }
    }

    public void setName(Player player, String newName) throws NoSuchFieldException, IllegalAccessException {
        EntityPlayer entityPlayer = getEntityPlayer(player);
        updateOriginalName(player, newName);

        TeamManagerModule teamManagerModule = TGM.get().getModule(TeamManagerModule.class);
        MatchTeam matchTeam = teamManagerModule.getTeam(player);

        // Modify the player's game profile.
        GameProfile profile = entityPlayer.getProfile();
        setGameProfileField(profile, "name", newName);

        updatePlayers(player);
        updatePlayerTeam(player, matchTeam);
        updatePlayerList(player);
    }

    private void updateOriginalName(Player player, String newName) {
        nickNames.put(player.getUniqueId(), newName);

        if (!originalNames.containsKey(player.getUniqueId())) {
            originalNames.put(player.getUniqueId(), player.getName());
        } else if (newName.equals(originalNames.get(player.getUniqueId()))) {
            originalNames.remove(player.getUniqueId());
            nickNames.remove(player.getUniqueId());
        }
    }

    private void setGameProfileField(Object obj, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = GameProfile.class.getDeclaredField(fieldName);
        field.setAccessible(true);

        field.set(obj, value);
    }

    public void setStats(Player player, String statName, int value) throws NoSuchFieldException {
        NickedUserProfile nickedStats = getUserProfile(player);

        switch(statName.toLowerCase()) {
            case "kills":
                setStats(player, value, null, null, null, null);
                break;
            case "deaths":
                setStats(player, null, value, null, null, null);
                break;
            case "wins":
                setStats(player, null, null, value, null, null);
                break;
            case "losses":
                setStats(player, null, null, null, value, null);
                break;
            case "objectives":
                setStats(player, null, null, null, null, value);
                break;
            default:
                throw new NoSuchFieldException();
        }

        stats.put(player.getUniqueId(), nickedStats);
        setNew(player, false);
        updatePlayerList(player);
    }

    public void setStats(Player player, Integer kills, Integer deaths, Integer wins, Integer losses, Integer woolDestroys) {
        NickedUserProfile nickedStats = getUserProfile(player);
        if (kills != null) {
            nickedStats.setKills(kills);
        }
        if (deaths != null) {
            nickedStats.setDeaths(deaths);
        }
        if (wins != null) {
            nickedStats.setWins(wins);
        }
        if (losses != null) {
            nickedStats.setLosses(losses);
        }
        if (woolDestroys != null) {
            nickedStats.setWool_destroys(woolDestroys);
        }
        stats.put(player.getUniqueId(), nickedStats);

        updatePlayerList(player);
    }

    public void setRank(Player player, Rank rank) {
        NickedUserProfile nickedStats = getUserProfile(player);
        nickedStats.setRanksLoaded(new ArrayList<>());
        nickedStats.addRank(rank);
        stats.put(player.getUniqueId(), nickedStats);
    }

    public void setNew(Player player, boolean isNew) {
        NickedUserProfile nickedStats = getUserProfile(player);
        nickedStats.setNew(isNew);
        stats.put(player.getUniqueId(), nickedStats);
    }

    private void updatePlayerList(Player player) {
        PlayerContext context = TGM.get().getPlayerManager().getPlayerContext(player);
        ScoreboardManagerModule scoreboardManagerModule = TGM.get().getModule(ScoreboardManagerModule.class);
        TeamManagerModule teamManagerModule = TGM.get().getModule(TeamManagerModule.class);
        scoreboardManagerModule.updatePlayerListName(context, teamManagerModule.getTeam(player));
    }

    private void updatePlayerTeam(Player player, MatchTeam team) {
        PlayerContext context = TGM.get().getPlayerManager().getPlayerContext(player);
        ScoreboardManagerModule scoreboardManagerModule = TGM.get().getModule(ScoreboardManagerModule.class);
        scoreboardManagerModule.updatePlayerTeam(context, team, team);
    }

    public NickedUserProfile getUserProfile(Player player) {
        PlayerContext context = TGM.get().getPlayerManager().getPlayerContext(player);
        return stats.getOrDefault(player.getUniqueId(), NickedUserProfile.createFromUserProfile(context.getUserProfile()));
    }

    public void setSkin(Player player, Skin skin) {
        EntityPlayer entityPlayer = getEntityPlayer(player);

        entityPlayer.getProfile().getProperties().put("textures", new Property("textures", skin.value, skin.signature));

        updatePlayers(player);

        skins.put(player.getUniqueId(), skin);
    }

    public void setSkin(Player player, UUID uuid) {
        Bukkit.getScheduler().runTaskAsynchronously(TGM.get(), () -> {
            MojangProfile profile = retrieveProfile(uuid);
            if (profile != null && profile.getTextures().getSkin() != null) {
                setSkin(player, profile.getTextures().getSkin());
            }
        });
    }

    public void setSkin(Player player, String nameOfPlayer) {
        Bukkit.getScheduler().runTaskAsynchronously(TGM.get(), () -> {
            MojangProfile profile = retrieveProfile(nameOfPlayer);
            if (profile != null && profile.getTextures().getSkin() != null) {
                setSkin(player, profile.getTextures().getSkin());
            }
        });
    }

    private void updatePlayers(Player toExclude) {
        EntityPlayer entityPlayer = getEntityPlayer(toExclude);

        PacketPlayOutPlayerInfo removeSelfPacket = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, entityPlayer);
        entityPlayer.playerConnection.sendPacket(removeSelfPacket);

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.equals(toExclude) && visiblityController.canSee(p, toExclude)) {
                EntityPlayer entityOther = getEntityPlayer(p);

                // Remove the old player.
                PacketPlayOutPlayerInfo removePlayerPacket = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, entityPlayer);
                entityOther.playerConnection.sendPacket(removePlayerPacket);

                // Add the player back.
                PacketPlayOutPlayerInfo addPlayerPacket = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entityPlayer);
                PacketPlayOutEntityDestroy destroyEntityPacket = new PacketPlayOutEntityDestroy(toExclude.getEntityId());
                PacketPlayOutNamedEntitySpawn namedEntitySpawnPacket = new PacketPlayOutNamedEntitySpawn(entityPlayer);
                entityOther.playerConnection.sendPacket(addPlayerPacket);
                entityOther.playerConnection.sendPacket(destroyEntityPacket);
                entityOther.playerConnection.sendPacket(namedEntitySpawnPacket);
            }
        }

        PacketPlayOutPlayerInfo addSelfPacket = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entityPlayer);
        entityPlayer.playerConnection.sendPacket(addSelfPacket);
//        PacketPlayOutRespawn respawn = new PacketPlayOutRespawn(DimensionManager.OVERWORLD, WorldType.getType(Objects.requireNonNull(toExclude.getWorld().getWorldType()).getName()), EnumGamemode.getById(toExclude.getGameMode().getValue()));
        toExclude.spigot().respawn();
        PacketPlayOutEntityTeleport playerTP = new PacketPlayOutEntityTeleport(entityPlayer);
        try {
            Field field = PacketPlayOutEntityTeleport.class.getDeclaredField("a");
            field.setAccessible(true);
            field.set(playerTP, -1);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        entityPlayer.playerConnection.sendPacket(playerTP);
        toExclude.updateInventory();
        entityPlayer.updateAbilities();
    }

    private UUID getUUID(String name) {
        MojangProfile profile = retrieveProfile(name);
        if (profile == null) {
            return null;
        }
        return profile.getUuid();
    }

    private Skin getSkin(UUID uuid) {
        MojangProfile profile = retrieveProfile(uuid);
        if (profile == null) {
            return null;
        }
        return profile.getTextures().getSkin();
    }

    private MojangProfile retrieveProfile(String name) {
        if (profileCache.contains(name)) {
            return profileCache.get(name);
        } else {
            MojangProfile profile = TGM.get().getTeamClient().getMojangProfile(name);
            if (profile == null) return null;
            profileCache.add(profile);
            return profile;
        }
    }

    private MojangProfile retrieveProfile(UUID uuid) {
        if (profileCache.contains(uuid)) {
            return profileCache.get(uuid);
        } else {
            MojangProfile profile = TGM.get().getTeamClient().getMojangProfile(uuid);
            if (profile == null) return null;
            profileCache.add(profile);
            return profile;
        }
    }

    private EntityPlayer getEntityPlayer(Player player) {
        return ((CraftPlayer) player).getHandle();
    }

}
