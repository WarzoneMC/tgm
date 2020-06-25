package network.warzone.tgm.modules.flag;

import com.google.gson.JsonObject;
import lombok.Getter;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchStatus;
import network.warzone.tgm.modules.base.PlayerRedeemable;
import network.warzone.tgm.modules.region.CuboidRegion;
import network.warzone.tgm.modules.region.Region;
import network.warzone.tgm.modules.respawn.RespawnModule;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamChangeEvent;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.parser.banner.BannerPatternsDeserializer;
import network.warzone.tgm.player.event.TGMPlayerDeathEvent;
import network.warzone.tgm.util.Parser;
import network.warzone.tgm.util.Strings;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Rotatable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.ref.WeakReference;
import java.util.List;

@Getter
public class MatchFlag extends PlayerRedeemable implements Listener {
    // banner details
    private List<Pattern> bannerPatterns;
    private String bannerType;
    private String rotation;
    private Location location;

    private FlagSubscriber flagSubscriber;
    private MatchTeam team;
    private Player flagHolder;

    private Region protectiveRegion;

    private WeakReference<Match> match;
    private TeamManagerModule teamManagerModule;
    private RespawnModule respawnModule;

    public MatchFlag(List<Pattern> bannerPatterns, String bannerType, String rotation, Location location, FlagSubscriber flagSubscriber, MatchTeam team) {

        this.bannerPatterns = bannerPatterns;
        this.bannerType = bannerType;
        this.rotation = rotation;
        this.location = location;
        this.flagSubscriber = flagSubscriber;
        this.team = team;

        this.match = new WeakReference<Match>(TGM.get().getMatchManager().getMatch());
        this.teamManagerModule = TGM.get().getModule(TeamManagerModule.class);
        this.respawnModule = TGM.get().getModule(RespawnModule.class);

        TGM.registerEvents(this);

        if (bannerType.contains("WALL")) {
            this.protectiveRegion = new CuboidRegion(
                    location.clone().subtract(1, 2, 1),
                    location.clone().add(1, 1, 1)
            );
        } else {
            this.protectiveRegion = new CuboidRegion(
                    location.clone().subtract(1, 1, 1),
                    location.clone().add(1, 2, 1)
            );
        }

        placeFlag();
    }

    // Filters are wack. Do not allow build or break in the base region
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!protectiveRegion.contains(event.getBlock().getLocation())) return;
        event.getPlayer().sendMessage(ChatColor.RED + "You cannot break near a flag area!");
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!protectiveRegion.contains(event.getBlock().getLocation())) return;
        event.getPlayer().sendMessage(ChatColor.RED + "You cannot build near a flag area!");
        event.setCancelled(true);
    }

    public Material generateMaterial() {
        return Material.valueOf(Strings.getTechnicalName(bannerType) + "_BANNER");
    }

    public ItemStack generateBannerItem() {
        ItemStack bannerItem = new ItemStack(generateMaterial());
        ItemMeta itemMeta = bannerItem.getItemMeta();
        if (itemMeta instanceof BannerMeta) {
            BannerMeta bannerMeta = (BannerMeta) itemMeta;
            bannerMeta.setPatterns(bannerPatterns);
            bannerItem.setItemMeta(bannerMeta);
        }
        bannerItem.addUnsafeEnchantment(Enchantment.BINDING_CURSE, 1);
        return bannerItem;
    }

    private void refreshFlag() {
        this.flagHolder = null;
        placeFlag();
    }

    private void placeFlag() {
        Block block = location.getBlock();
        block.setType(generateMaterial(), false);

        final BlockState state = block.getState();
        if(state instanceof Banner) {
            Banner banner = (Banner) block.getState();
            banner.setPatterns(bannerPatterns);

            BlockData bannerData = banner.getBlockData();
            if (bannerData instanceof Rotatable) {
                Rotatable bannerRotatable = (Rotatable) bannerData;
                bannerRotatable.setRotation(BlockFace.valueOf(Strings.getTechnicalName(rotation)));
                banner.setBlockData(bannerRotatable);
            }

            banner.update(true);
        }
    }

    private boolean passesGeneralConditions(MatchTeam team) {
        return match.get().getMatchStatus() == MatchStatus.MID && !team.isSpectator();
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (flagHolder != null) return;
        MatchTeam playerTeam = teamManagerModule.getTeam(event.getPlayer());
        if (respawnModule.isDead(event.getPlayer()) ||
            !passesGeneralConditions(playerTeam) ||
            playerTeam.equals(team)) return;
        else if (event.getFrom().distanceSquared(location) > 1) return;

        this.flagHolder = event.getPlayer();
        location.getBlock().setType(Material.AIR);

        flagSubscriber.pickup(this, event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (match.get().getMatchStatus() != MatchStatus.MID || event.getPlayer() != flagHolder) return;
        this.refreshFlag();
        flagSubscriber.drop(this, event.getPlayer(), null);
    }

    @EventHandler
    public void onTeamChange(TeamChangeEvent event) {
        if (match.get().getMatchStatus() != MatchStatus.MID || event.getPlayerContext().getPlayer() != flagHolder || event.getTeam().equals(event.getOldTeam())) return;
        this.refreshFlag();
        flagSubscriber.drop(this, event.getPlayerContext().getPlayer(), null);
    }

    @EventHandler
    public void onTGMDeath(TGMPlayerDeathEvent event) {
        if (match.get().getMatchStatus() != MatchStatus.MID || event.getVictim() != flagHolder) return;
        this.refreshFlag();
        flagSubscriber.drop(this, event.getVictim(), event.getKiller());
    }

    public void unload() {
        flagHolder = null;
        TGM.unregisterEvents(this);
    }

    @Override
    public void redeem(Player player) {
        this.refreshFlag();
        flagSubscriber.capture(this, player);
    }

    @Override
    public boolean hasRedeemable(Player player) {
        return flagHolder.equals(player);
    }

    /**
     * Returns a MatchFlag instance from JSON
     * @param flagJson Flag JSON
     * @param flagSubscriber Subscriber for event handling
     * @param world World for location parsing
     * @return Deserialized MatchFlag instance
     */
    public static MatchFlag deserialize(JsonObject flagJson, FlagSubscriber flagSubscriber, World world) {
        List<Pattern> bannerPatterns = BannerPatternsDeserializer.parse(flagJson.get("patterns"));
        String bannerType = flagJson.get("type").getAsString();
        String bannerRotation = flagJson.has("rotation") ? flagJson.get("rotation").getAsString() : "EAST";
        Location location = Parser.convertLocation(world, flagJson.get("location"));

        TeamManagerModule teamManagerModule = TGM.get().getModule(TeamManagerModule.class);
        MatchTeam team = teamManagerModule.getTeamById(flagJson.get("team").getAsString());

        return new MatchFlag(bannerPatterns, bannerType, bannerRotation, location, flagSubscriber, team);
    }

}
