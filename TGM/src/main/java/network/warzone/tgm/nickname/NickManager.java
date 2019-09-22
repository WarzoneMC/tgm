package network.warzone.tgm.nickname;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.util.UUIDTypeAdapter;
import lombok.Getter;
import net.minecraft.server.v1_14_R1.*;
import network.warzone.tgm.TGM;
import network.warzone.tgm.modules.SpectatorModule;
import network.warzone.tgm.modules.scoreboard.ScoreboardManagerModule;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.modules.visibility.VisibilityController;
import network.warzone.tgm.modules.visibility.VisibilityControllerImpl;
import network.warzone.tgm.user.PlayerContext;
import network.warzone.warzoneapi.models.Rank;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.json.JSONObject;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.*;

public class NickManager {

    public static String RATELIMITED_MESSAGE = ChatColor.GOLD.toString() + ChatColor.BOLD + "Slow Down! " + ChatColor.RESET.toString() + ChatColor.RED + "You're being ratelimited.";

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

    private HashMap<String, UUID> uuidCache = new HashMap<>();
    private HashMap<String, Skin> skinCache = new HashMap<>();

    public NickManager() {
        visiblityController = new VisibilityControllerImpl(TGM.get().getModule(SpectatorModule.class));
    }

    public void addQueuedNick(Player player, String newName) {
        Bukkit.getScheduler().runTaskAsynchronously(TGM.get(), () -> {
            Skin skin;
            try {
                UUID nickedUUID = getUUID(newName);
                skin = getSkin(nickedUUID);
            } catch (UnirestException exception) {
                player.sendMessage(RATELIMITED_MESSAGE);
                return;
            }

            queuedNicks.add(new QueuedNick(newName, skin, player));
        });
    }

    public Optional<QueuedNick> getQueuedNick(Player player) {
        return queuedNicks.stream().filter(queuedNick -> queuedNick.getPlayer().getUniqueId().equals(player.getUniqueId())).findFirst();
    }

    public void setNick(Player player, String newName) throws NoSuchFieldException, IllegalAccessException {
        setName(player, newName);
        setSkin(player, newName, null);
    }

    public void reset(Player player, boolean kick) throws NoSuchFieldException, IllegalAccessException, UnirestException {
        if (kick) {
            originalNames.remove(player.getUniqueId());
            nickNames.remove(player.getUniqueId());
            skins.remove(player.getUniqueId());
            player.kickPlayer(ChatColor.RED + "Resetting nickname");
        } else {
            String originalName = originalNames.get(player.getUniqueId());
            setName(player, originalName);
            setSkin(player, originalName, player.getUniqueId());
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
        updatePlayerList(player);
    }

    public void setStats(Player player, Integer kills, Integer deaths, Integer wins, Integer losses, Integer woolDestroys) {
        NickedUserProfile nickedStats = getUserProfile(player);
        if (kills != null) {
            nickedStats.setKills(kills);
        }
        if (deaths != null){
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
        nickedStats.setRanksLoaded(Collections.emptyList());
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
        scoreboardManagerModule.updatePlayerListName(context);
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

    public void setSkin(Player player, String nameOfPlayer, @Nullable UUID uuid) {
        Bukkit.getScheduler().runTaskAsynchronously(TGM.get(), () -> {
            Skin skin;
            try {
                UUID theUUID = uuid;
                if (theUUID == null) {
                    theUUID = getUUID(nameOfPlayer);
                }
                skin = getSkin(theUUID);
            } catch (UnirestException exception) {
                player.sendMessage(RATELIMITED_MESSAGE);
                return;
            }

            setSkin(player, skin);
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
        PacketPlayOutRespawn respawn = new PacketPlayOutRespawn(DimensionManager.OVERWORLD, WorldType.getType(Objects.requireNonNull(toExclude.getWorld().getWorldType()).getName()), EnumGamemode.getById(toExclude.getGameMode().getValue()));
        entityPlayer.playerConnection.sendPacket(respawn);
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

    private UUID getUUID(String name) throws UnirestException {
        if (uuidCache.containsKey(name)) {
            return uuidCache.get(name);
        } else {
            UUID uuid = fetchUUID(name);
            uuidCache.put(name, uuid);
            return uuid;
        }
    }

    private Skin getSkin(UUID uuid) throws UnirestException {
        if (skinCache.containsKey(uuid.toString())) {
            return skinCache.get(uuid.toString());
        } else {
            Skin skin = fetchSkin(uuid);
            skinCache.put(uuid.toString(), skin);
            return skin;
        }
    }

    private  UUID fetchUUID(String name) throws UnirestException {
        HttpResponse<String> response = Unirest.get("https://api.mojang.com/users/profiles/minecraft/" + name).asString();
        if (response.getStatus() == 200) {
            return UUID.fromString(insertDashUUID(new JSONObject(response.getBody()).getString("id")));
        }
        return null;
    }

    private static String insertDashUUID(String uuid) {
        StringBuilder sb = new StringBuilder(uuid);
        sb.insert(8, "-");
        sb = new StringBuilder(sb.toString());
        sb.insert(13, "-");
        sb = new StringBuilder(sb.toString());
        sb.insert(18, "-");
        sb = new StringBuilder(sb.toString());
        sb.insert(23, "-");

        return sb.toString();
    }

    private Skin fetchSkin(UUID uuid) throws UnirestException {
        HttpResponse<String> response = Unirest.get(String.format("https://sessionserver.mojang.com/session/minecraft/profile/%s?unsigned=false", UUIDTypeAdapter.fromUUID(uuid))).asString();
        if (response.getStatus() == 200) {
            JSONObject object = new JSONObject(response.getBody());
            JSONObject properties = object.getJSONArray("properties").getJSONObject(0);
            return new Skin(properties.getString("value"), properties.getString("signature"));
        } else {
            System.out.println("Connection couldn't be established code=" + response.getStatus());
            return null;
        }
    }

    private EntityPlayer getEntityPlayer(Player player) {
        return ((CraftPlayer) player).getHandle();
    }

}
