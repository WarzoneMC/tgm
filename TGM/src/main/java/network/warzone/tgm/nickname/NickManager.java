package network.warzone.tgm.nickname;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.util.UUIDTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_14_R1.*;
import network.warzone.tgm.TGM;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.user.PlayerContext;
import network.warzone.warzoneapi.models.Rank;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nullable;
import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

public class NickManager {

    @Getter @Setter @AllArgsConstructor
    private class Skin {
        public String value;
        public String signature;
    }

    public HashMap<UUID, String> originalNames = new HashMap<>();
    public HashMap<UUID, String> nickNames = new HashMap<>();
    public HashMap<UUID, Skin> skins = new HashMap<>();
    public HashMap<UUID, Rank> ranks = new HashMap<>();
    public HashMap<UUID, Integer> levels = new HashMap<>();

    private HashMap<String, UUID> uuidCache = new HashMap<>();
    private HashMap<String, Skin> skinCache = new HashMap<>();

    public void setNick(Player player, String newName, boolean uuidSpoof) {
        UUID uuid = getUUID(newName);
        if (uuid == null ){
            setName(player, newName, false, null);
            setSkin(player, newName, null);
        } else {
            setName(player, newName, uuidSpoof, uuid);
            setSkin(player, newName, uuid);
        }
    }

    public void setName(Player player, String newName, boolean uuidSpoof, UUID uuid) {
        EntityPlayer entityPlayer = getEntityPlayer(player);
        if (!originalNames.containsKey(player.getUniqueId())) {
            originalNames.put(player.getUniqueId(), player.getName());
        } else if (newName.equals(originalNames.get(player.getUniqueId()))) {
            originalNames.remove(player.getUniqueId());
            nickNames.remove(player.getUniqueId());
        }
        PacketPlayOutPlayerInfo playerInfo1 = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, entityPlayer);
        entityPlayer.playerConnection.sendPacket(playerInfo1);

        // Get player's old team.
        TeamManagerModule teamManager = TGM.get().getModule(TeamManagerModule.class);
        MatchTeam team = teamManager.getTeam(player);
        PlayerContext context = TGM.get().getPlayerManager().getPlayerContext(player);
        team.removePlayer(context);

        // Modify the player's game profile.
        GameProfile profile = entityPlayer.getProfile();
        try {
            Field field = GameProfile.class.getDeclaredField("name");
            field.setAccessible(true);

            field.set(profile, newName);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        if (uuidSpoof) {
            try {
                Field field = GameProfile.class.getDeclaredField("id");
                field.setAccessible(true);

                field.set(profile, uuid);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.equals(player) && p.canSee(player)) {
                EntityPlayer entityOther = getEntityPlayer(p);

                // Remove the old player.
                PacketPlayOutPlayerInfo playerInfo = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, entityPlayer);
                entityOther.playerConnection.sendPacket(playerInfo);

                // Add the player back.
                PacketPlayOutPlayerInfo playerAddBack = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entityPlayer);
                PacketPlayOutEntityDestroy entityDestroy = new PacketPlayOutEntityDestroy(player.getEntityId());
                PacketPlayOutNamedEntitySpawn namedEntitySpawn = new PacketPlayOutNamedEntitySpawn(entityPlayer);
                entityOther.playerConnection.sendPacket(playerAddBack);
                entityOther.playerConnection.sendPacket(entityDestroy);
                entityOther.playerConnection.sendPacket(namedEntitySpawn);
            }
        }

        PacketPlayOutPlayerInfo playerAddBack = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entityPlayer);
        entityPlayer.playerConnection.sendPacket(playerAddBack);
        nickNames.put(player.getUniqueId(), newName);

        TGM.get().getModule(TeamManagerModule.class).joinTeam(context, team);
    }

    public void setLevel(Player player, Integer level) {
        levels.put(player.getUniqueId(), level);
    }

    public void setRank(Player player, Rank rank) {
        ranks.put(player.getUniqueId(), rank);
    }

    public void setSkin(Player player, String nameOfPlayer, @Nullable UUID uuid) {
        EntityPlayer entityPlayer = getEntityPlayer(player);

        UUID theUUID = uuid;
        if (theUUID == null) {
            theUUID = getUUID(nameOfPlayer);
        }
        if (theUUID == null) return;
        Skin skin = getSkin(theUUID);

        PacketPlayOutPlayerInfo playerInfo1 = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, entityPlayer);
        entityPlayer.playerConnection.sendPacket(playerInfo1);

        Collection<Property> properties = entityPlayer.getProfile().getProperties().get("textures");
        Property property = (Property) properties.toArray()[0];
        skinCache.put(player.getUniqueId().toString(), new Skin(property.getValue(), property.getSignature()));
        uuidCache.put(originalNames.get(player.getUniqueId()), player.getUniqueId());

        entityPlayer.getProfile().getProperties().put("textures", new Property("textures", skin.value, skin.signature));

        if (skin != null) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.equals(player) && p.canSee(player)) {
                    EntityPlayer entityOther = getEntityPlayer(p);

                    // Remove the old player.
                    PacketPlayOutPlayerInfo playerInfo = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, entityPlayer);
                    entityOther.playerConnection.sendPacket(playerInfo);

                    // Add the player back.
                    PacketPlayOutPlayerInfo playerAddBack = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entityPlayer);
                    PacketPlayOutEntityDestroy entityDestroy = new PacketPlayOutEntityDestroy(player.getEntityId());
                    PacketPlayOutNamedEntitySpawn namedEntitySpawn = new PacketPlayOutNamedEntitySpawn(entityPlayer);
                    entityOther.playerConnection.sendPacket(playerAddBack);
                    entityOther.playerConnection.sendPacket(entityDestroy);
                    entityOther.playerConnection.sendPacket(namedEntitySpawn);
                }
            }

            PacketPlayOutPlayerInfo playerAddBack = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entityPlayer);
            entityPlayer.playerConnection.sendPacket(playerAddBack);
            player.spigot().respawn();
            skins.put(player.getUniqueId(), skin);
        }
    }

    private UUID getUUID(String name) {
        if (uuidCache.containsKey(name)) {
            return uuidCache.get(name);
        } else {
            UUID uuid = fetchUUID(name);
            uuidCache.put(name, uuid);
            return uuid;
        }
    }

    private Skin getSkin(UUID uuid) {
        if (skinCache.containsKey(uuid.toString())) {
            return skinCache.get(uuid.toString());
        } else {
            Skin skin = fetchSkin(uuid);
            skinCache.put(uuid.toString(), skin);
            return skin;
        }
    }

    private UUID fetchUUID(String name) {
        try {
            HttpResponse<String> response = Unirest.get("https://api.mojang.com/users/profiles/minecraft/" + name).asString();
            if (response.getStatus() == 200) {
                return UUID.fromString(insertDashUUID(new JSONObject(response.getBody()).getString("id")));
            }
        } catch (UnirestException e) {
            e.printStackTrace();
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

    private Skin fetchSkin(UUID uuid) {
        try {
            HttpResponse<String> response = Unirest.get(String.format("https://sessionserver.mojang.com/session/minecraft/profile/%s?unsigned=false", UUIDTypeAdapter.fromUUID(uuid))).asString();
            if (response.getStatus() == 200) {
                JSONObject object = new JSONObject(response.getBody());
                JSONObject properties = object.getJSONArray("properties").getJSONObject(0);
                return new Skin(properties.getString("value"), properties.getString("signature"));
            } else {
                System.out.println("Connection couldn't be established code=" + response.getStatus());
                return null;
            }
        } catch (UnirestException e) {
            e.printStackTrace();
            return null;
        }
    }

    private EntityPlayer getEntityPlayer(Player player) {
        return ((CraftPlayer) player).getHandle();
    }

}
