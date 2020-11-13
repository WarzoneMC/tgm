package network.warzone.tgm.nickname;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.util.Pair;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_16_R3.*;
import network.warzone.tgm.TGM;
import network.warzone.tgm.modules.SpectatorModule;
import network.warzone.tgm.modules.scoreboard.ScoreboardManagerModule;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.modules.visibility.VisibilityController;
import network.warzone.tgm.modules.visibility.VisibilityControllerImpl;
import network.warzone.tgm.user.PlayerContext;
import network.warzone.tgm.util.GameProfileUtil;
import network.warzone.warzoneapi.models.MojangProfile;
import network.warzone.warzoneapi.models.Rank;
import network.warzone.warzoneapi.models.Skin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class NickManager {

    private final VisibilityController visibilityController;

    @Getter
    private final List<Nick> nicks = new ArrayList<>();

    private final ProfileCache profileCache = ProfileCache.getInstance();

    public NickManager() {
        visibilityController = new VisibilityControllerImpl(TGM.get().getModule(SpectatorModule.class));
    }

    public void create(PlayerContext context, NickDetails details) {
        Bukkit.getScheduler().runTaskAsynchronously(TGM.get(), () -> {
            nicks.removeIf(nick -> nick.getUuid().equals(context.getPlayer().getUniqueId()) && !nick.isActive());

            UUID originalUUID = context.getPlayer().getUniqueId();
            String name = details.getName();
            UUID nickedUUID = getUUID(details.getSkin());
            Skin skin = nickedUUID != null ? getSkin(nickedUUID) : null;
            NickedUserProfile profile = new NickedUserProfile(context.getUserProfile());

            applyDetailToProfile(details, profile);

            nicks.add(new Nick(
                    originalUUID,
                    getOriginalName(context.getPlayer().getName()),
                    name,
                    skin,
                    details,
                    profile,
                    false,
                    false
            ));
        });
    }

    public void update(PlayerContext context, Consumer<Nick> action) {
        getNicks(context).forEach(action);
    }

    public void apply(PlayerContext context, boolean force) {
        nicks.removeIf(nick -> nick.getUuid().equals(context.getPlayer().getUniqueId()) && nick.isApplied());
        if (!force) {
            update(context, nick -> {
                nick.setApplied(true);
            });
        } else {
            update(context, nick -> {
                nick.setApplied(true);
                nick.setActive(true);
                try {
                    if (nick.getName() != null) {
                        setName(context, nick.getName());
                    }
                    setSkin(context, nick.getSkin());
                } catch (NoSuchFieldException | IllegalAccessException ignored) {}
            });
        }
    }

    public void reset(PlayerContext context, boolean force) {
        if (!force) {
            nicks.removeIf(uuidMatch(context));
            context.getPlayer().kickPlayer(ChatColor.RED + "Resetting nick.");
        } else {
            String originalName = getNick(context).map(Nick::getOriginalName).orElse(context.getPlayer().getName());
            Skin skin = getSkin(context.getPlayer().getUniqueId());
            nicks.removeIf(uuidMatch(context));
            try {
                setName(context, originalName);
                setSkin(context, skin);
            } catch (NoSuchFieldException | IllegalAccessException ignored) {}
        }
    }

    public List<Nick> getNicks(PlayerContext context) {
        return nicks.stream().filter(uuidMatch(context)).collect(Collectors.toList());
    }

    public Optional<Nick> getNick(PlayerContext context) {
        return nicks.stream().filter(uuidMatch(context)).findFirst();
    }

    public boolean hasNick(PlayerContext context) {
        return nicks.stream().anyMatch(uuidMatch(context));
    }

    public boolean isNicked(PlayerContext context) {
        return nicks.stream().anyMatch(nick -> nick.getUuid().equals(context.getPlayer().getUniqueId()) && nick.isActive());
    }

    public boolean isNickName(String name) {
        return nicks.stream().anyMatch(nick -> nick.getName().equals(name) && nick.isActive());
    }

    public String getOriginalName(String username) {
        Optional<Nick> playerNick = nicks.stream().filter(nick -> nick.getName().equals(username)).findFirst();

        return playerNick.map(Nick::getOriginalName).orElse(username);
    }

    public void setName(PlayerContext context, String newName) throws NoSuchFieldException, IllegalAccessException {
        TeamManagerModule teamManagerModule = TGM.get().getModule(TeamManagerModule.class);
        MatchTeam matchTeam = teamManagerModule.getTeam(context.getPlayer());

        // Modify the player's game profile.
        GameProfile profile = GameProfileUtil.getGameProfile(context.getPlayer());
        GameProfileUtil.setGameProfileField(profile, "name", newName);

        updatePlayers(context.getPlayer());
        updatePlayerTeam(context, matchTeam);
        updatePlayerList(context);
    }

    public void setSkin(PlayerContext context, Skin skin) {
        EntityPlayer entityPlayer = getEntityPlayer(context.getPlayer());

        entityPlayer.getProfile().getProperties().put("textures", new Property("textures", skin.value, skin.signature));

        updatePlayers(context.getPlayer());
    }

    private void updatePlayerList(PlayerContext context) {
        ScoreboardManagerModule scoreboardManagerModule = TGM.get().getModule(ScoreboardManagerModule.class);
        TeamManagerModule teamManagerModule = TGM.get().getModule(TeamManagerModule.class);
        scoreboardManagerModule.updatePlayerListName(context, teamManagerModule.getTeam(context.getPlayer()));
    }

    private void updatePlayerTeam(PlayerContext context, MatchTeam team) {
        ScoreboardManagerModule scoreboardManagerModule = TGM.get().getModule(ScoreboardManagerModule.class);
        scoreboardManagerModule.updatePlayerTeam(context, team, team);
    }

    private void updatePlayers(Player toExclude) {
        EntityPlayer entityPlayer = getEntityPlayer(toExclude);

        List<Pair<EnumItemSlot, ItemStack>> inventory = new ArrayList<>();
        if (toExclude.getEquipment() != null) {
            inventory.add(new Pair<>(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(toExclude.getEquipment().getHelmet())));
            inventory.add(new Pair<>(EnumItemSlot.CHEST, CraftItemStack.asNMSCopy(toExclude.getEquipment().getChestplate())));
            inventory.add(new Pair<>(EnumItemSlot.LEGS, CraftItemStack.asNMSCopy(toExclude.getEquipment().getLeggings())));
            inventory.add(new Pair<>(EnumItemSlot.FEET, CraftItemStack.asNMSCopy(toExclude.getEquipment().getBoots())));
            inventory.add(new Pair<>(EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(toExclude.getEquipment().getItemInMainHand())));
            inventory.add(new Pair<>(EnumItemSlot.OFFHAND, CraftItemStack.asNMSCopy(toExclude.getEquipment().getItemInOffHand())));
        }

        PacketPlayOutPlayerInfo playerInfoRemovePacket = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, entityPlayer);
        PacketPlayOutPlayerInfo playerInfoAddPacket = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entityPlayer);
        PacketPlayOutEntityDestroy entityDestroyPacket = new PacketPlayOutEntityDestroy(toExclude.getEntityId());
        PacketPlayOutNamedEntitySpawn namedEntitySpawnPacket = new PacketPlayOutNamedEntitySpawn(entityPlayer);
        PacketPlayOutEntityEquipment entityEquipmentPacket = new PacketPlayOutEntityEquipment(toExclude.getEntityId(), inventory);

        Location l = toExclude.getLocation();
        PacketPlayOutPosition positionPacket = new PacketPlayOutPosition(l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch(), new HashSet<>(), -1);
        PacketPlayOutEntityHeadRotation entityHeadRotationPacket = new PacketPlayOutEntityHeadRotation(entityPlayer, (byte) ((l.getYaw() * 256.0F) / 360.0F));

        DataWatcherObject<Byte> dataWatcherObject;
        try {
            Field field = EntityHuman.class.getDeclaredField("bp");
            field.setAccessible(true);
            dataWatcherObject = (DataWatcherObject<Byte>) field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return;
        }

        entityPlayer.getDataWatcher().set(dataWatcherObject, (byte) (0x40 | 0x20 | 0x10 | 0x08 | 0x04 | 0x02 | 0x01));
        PacketPlayOutEntityMetadata entityMetadataPacket = new PacketPlayOutEntityMetadata(toExclude.getEntityId(), entityPlayer.getDataWatcher(), true);

        for (Player p : Bukkit.getOnlinePlayers()) {
            EntityPlayer entityOther = getEntityPlayer(p);
            CraftPlayer craftHandle = (CraftPlayer) p;
            if (p.equals(toExclude)) {
                entityOther.playerConnection.sendPacket(playerInfoRemovePacket);
                entityOther.playerConnection.sendPacket(playerInfoAddPacket);

                toExclude.spigot().respawn();

                entityOther.playerConnection.sendPacket(positionPacket);

                craftHandle.updateScaledHealth();
                craftHandle.updateCommands();
                p.updateInventory();
                entityOther.updateAbilities();
            } else if (visibilityController.canSee(p, toExclude)) {
                // Remove the old player.
                entityOther.playerConnection.sendPacket(playerInfoRemovePacket);
                entityOther.playerConnection.sendPacket(entityDestroyPacket);

                // Add the player back.
                entityOther.playerConnection.sendPacket(playerInfoAddPacket);
                entityOther.playerConnection.sendPacket(namedEntitySpawnPacket);

                // Send the player's inventory.
                entityOther.playerConnection.sendPacket(entityEquipmentPacket);
                // Send the data metadata, this displays the second layer of the skin.
                entityOther.playerConnection.sendPacket(entityMetadataPacket);
                entityOther.playerConnection.sendPacket(entityHeadRotationPacket);
            } else {
                entityPlayer.playerConnection.sendPacket(playerInfoRemovePacket);
                entityPlayer.playerConnection.sendPacket(playerInfoAddPacket);
            }
        }
    }

    public NickedUserProfile getUserProfile(PlayerContext context) {
        return nicks.stream().filter(uuidMatch(context)).findFirst().map(Nick::getProfile).orElse(new NickedUserProfile(context.getUserProfile()));
    }

    public UUID getUUID(String name) {
        MojangProfile profile = retrieveProfile(name);
        if (profile == null) {
            return null;
        }
        return profile.getUuid();
    }

    public Skin getSkin(UUID uuid) {
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

    private Predicate<Nick> uuidMatch(PlayerContext context) {
        return nick -> nick.getUuid().equals(context.getPlayer().getUniqueId());
    }

    private void applyDetailToProfile(NickDetails details, NickedUserProfile profile) {
        if (details.getRank() != null) {
            profile.setRanksLoaded(new ArrayList<>());
            profile.addRank(details.getRank());
        }

        if (details.getKills() != null) profile.setKills(details.getKills());
        if (details.getDeaths() != null) profile.setDeaths(details.getDeaths());
        if (details.getWins() != null) profile.setWins(details.getWins());
        if (details.getLosses() != null) profile.setLosses(details.getLosses());
        if (details.getObjectives() != null) profile.setWool_destroys(details.getObjectives());
        if (details.getFrozen() != null) profile.setFrozen(details.getFrozen());
    }

    @Getter @Setter @AllArgsConstructor
    public static class NickDetails {
        private String name;
        private String skin;
        private Rank rank;
        private Integer kills;
        private Integer deaths;
        private Integer wins;
        private Integer losses;
        private Integer objectives;
        private Boolean frozen;
    }

}
