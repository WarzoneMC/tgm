package network.warzone.tgm.util.menu;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import network.warzone.tgm.TGM;
import network.warzone.tgm.util.Strings;
import network.warzone.tgm.util.TimeUnitPair;
import network.warzone.tgm.util.itemstack.ItemFactory;
import network.warzone.warzoneapi.models.PunishmentType;
import network.warzone.warzoneapi.models.UserProfile;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

/**
 * Created by Jorge on 08/24/2019
 */
public class PunishMenu extends Menu {

    private static Map<UUID, PunishConfig> configs = new HashMap<>();
    @Getter private static PresetsMenu presetsMenu = new PresetsMenu();

    @Getter private Player player;
    @Getter private UUID playerUuid;

    private List<String> players = new ArrayList<>();
    private int page = 0;
    private Mode mode = Mode.NONE;
    private boolean liveUpdate = true;

    static {
        TGM.registerEvents(new Listener() {
            @EventHandler
            public void onQuit(PlayerQuitEvent event) {
                configs.remove(event.getPlayer().getUniqueId());
            }
        });
    }

    private PunishMenu(Player player) {
        this(player, 0);
    }

    private PunishMenu(Player player, int page) {
        super(ChatColor.UNDERLINE + "Punish Menu", 9*6);
        if (player == null) {
            this.disable();
            return;
        }
        this.player = player;
        this.playerUuid = player.getUniqueId();
        this.page = page;
        Bukkit.getOnlinePlayers().stream().filter(p -> p != player).forEach(p -> players.add(p.getName()));
        Collections.sort(players);
        updateMenu();
    }

    private int getPageCount() {
        return (int) Math.ceil(players.size() / 36.0);
    }

    private void updateMenu() {
        clear();
        PunishConfig config = getConfig();
        if (players.isEmpty()) setItem(22, ItemFactory.createItem(Material.BARRIER, ChatColor.RED + "No players online"));
        int slot = 0;
        for (int i = 0; i < 36; i++) {
            int offset = i + (page * 36);
            if (players.size() <= offset) break;
            String playerName = players.get(offset);
            setItem(slot, getDisplayItem(playerName, config, true), (player, clickEvent) -> {
                if (clickEvent.isShiftClick() && !liveUpdate) {
                    issuePunishment(playerName, player, config);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
                    return;
                }
                new ConfirmMenu(player, ChatColor.UNDERLINE + "Confirm", getDisplayItem(playerName, config, false),
                        (p, e) -> {
                            issuePunishment(playerName, player, config);
                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
                            new PunishMenu(player).open(player);
                        },
                        (p, e) -> new PunishMenu(player).open(player)
                ).open(player);
            });
            slot++;
        }
        for (int i = 36; i <= 44; i++) setItem(i, ItemFactory.createItem(Material.GRAY_STAINED_GLASS_PANE, ChatColor.GRAY + "Page " + ChatColor.WHITE + (page + 1)));

        if (page != 0) setItem(36, ItemFactory.createItem(Material.ARROW, ChatColor.GREEN + "Page " + page), (p, event) -> {
            if (page <= 0) page = 0;
            else page--;
            clickSound();
            updateMenu();
        });
        if (page < getPageCount() - 1) setItem(44, ItemFactory.createItem(Material.ARROW, ChatColor.GREEN + "Page " + (page + 2)), (p, event) -> {
            if (page >= getPageCount()) page = getPageCount() - 1;
            else page++;
            clickSound();
            updateMenu();
        });

        setItem(46, getTypeScroller(config), (p, event) -> {
            PunishmentType[] punishmentTypes = PunishmentType.values();
            config.setType(punishmentTypes[(config.getType().ordinal() + 1) % punishmentTypes.length]);
            clickSound();
            updateMenu();
        });
        if (config.getType().isTimed())
            setItem(47, ItemFactory.createItem(Material.CLOCK, ChatColor.DARK_AQUA + "Length", Arrays.asList(
                    "",
                    ChatColor.GRAY + config.getTime().toString(),
                    "",
                    ChatColor.YELLOW + "Click to edit")
            ), (p, event) -> {
                this.mode = Mode.SETTING_LENGTH;
                clickSound();
                p.closeInventory();
                p.sendMessage(ChatColor.AQUA + "Enter the punishment length (1h, 2d, 3w, 4mo):");
            });
        else
            setItem(47, ItemFactory.createItem(Material.BLACK_STAINED_GLASS_PANE, ChatColor.DARK_GRAY + "Length"));
        setItem(48, ItemFactory.createItem(Material.NOTE_BLOCK, ChatColor.DARK_AQUA + "Silent", Arrays.asList(
                "",
                (config.isSilent() ? ChatColor.GREEN + "True" : ChatColor.RED + "False"),
                "",
                ChatColor.YELLOW + "Click to toggle"
        )), (p, event) -> {
            config.setSilent(!config.isSilent());
            clickSound();
            updateMenu();
        });
        setItem(49, ItemFactory.createItem(Material.PAPER, ChatColor.DARK_AQUA + "Reason", Arrays.asList(
                "",
                ChatColor.GRAY + config.getReason(),
                "",
                ChatColor.YELLOW + "Click to edit")
        ), (p, event) -> {
            this.mode = Mode.SETTING_REASON;
            clickSound();
            p.closeInventory();
            TextComponent textComponent = new TextComponent(ChatColor.AQUA + "Enter the punishment reason: (Click to paste in chat)");
            textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, config.getReason()));
            p.spigot().sendMessage(textComponent);
        });
        setItem(51, ItemFactory.createItem(Material.CHEST, ChatColor.DARK_AQUA + "Presets"), (p, event) -> {
            clickSound();
            presetsMenu.open(p);
        });
        setItem(52, ItemFactory.createItem(Material.ENDER_PEARL, ChatColor.DARK_AQUA + "Live update: " + (liveUpdate ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF"), ChatColor.GRAY,
                "",
                "Update list on player",
                "join and leave.",
                "",
                "If live update is off,",
                "shift-click to skip",
                "confirmation.",
                "",
                ChatColor.YELLOW + "Click to toggle"
        ), (p, event) -> {
            clickSound();
            liveUpdate = !liveUpdate;
            updateMenu();
        });
    }
    
    private void clickSound() {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.0f, 2.0f);
    }

    private ItemStack getDisplayItem(String playerName, PunishConfig config, boolean clickMsg) {
        ItemStack itemStack = ItemFactory.getPlayerSkull(playerName, ChatColor.AQUA + playerName);
        Player player = Bukkit.getPlayer(playerName);
        List<String> lore = new ArrayList<>();
        lore.add("");
        if (player != null) {
            UserProfile userProfile = TGM.get().getPlayerManager().getPlayerContext(player).getUserProfile();
            lore.add(ChatColor.GRAY + "Level: " + ChatColor.RESET + userProfile.getLevel());
            lore.add(ChatColor.GRAY + "First join: " + ChatColor.RESET + Strings.getAgo(userProfile.getInitialJoinDate()) + " ago");
            lore.add(ChatColor.GRAY + "Last join: " + ChatColor.RESET + Strings.getAgo(userProfile.getLastOnlineDate()) + " ago");
        } else
            lore.add(ChatColor.RED + "Player is offline.");
        lore.add("");
        lore.add(ChatColor.GRAY + "Type: " + ChatColor.RESET + config.getType().name() +
              (config.getType().isTimed() ? " " + ChatColor.GRAY + "Time: " + ChatColor.RESET + config.getTime().toString() : ""));
        lore.add(ChatColor.GRAY + "Silent: " + ChatColor.RESET + config.isSilent());
        lore.add(ChatColor.GRAY + "Reason: " + ChatColor.RESET + config.getReason());
        if (clickMsg) {
            lore.add("");
            lore.add(ChatColor.YELLOW + "Click to issue punishment");
        }
        ItemFactory.setLore(itemStack, lore);
        return itemStack;
    }

    private ItemStack getTypeScroller(PunishConfig config) {
        ItemStack itemStack = ItemFactory.createItem(Material.YELLOW_TERRACOTTA, ChatColor.DARK_AQUA + "Type");
        List<String> lore = new ArrayList<>();
        lore.add("");
        for (PunishmentType type : PunishmentType.values()) {
            String typeString = "";
            if (type == config.getType()) typeString += ChatColor.GREEN;
            else typeString += ChatColor.GRAY;
            if (!getPlayer().hasPermission(type.getPermission())) typeString += ChatColor.STRIKETHROUGH;
            lore.add(typeString + type.name());
        }
        lore.add("");
        lore.add(ChatColor.YELLOW + "Click to cycle");
        ItemFactory.setLore(itemStack, lore);
        return itemStack;
    }

    public static void issuePunishment(String playerName, Player sender, PunishConfig config) {
        StringBuilder command = new StringBuilder(config.getType().name().toLowerCase() + " " + playerName + " ");
        if (config.getType().isTimed()) {
            command.append(config.getTime().getValue());
            command.append(config.getTime().getUnit().name());
            command.append(" ");
        }
        command.append(config.getReason());
        if (config.isSilent()) command.append(" -s");
        TGM.get().getLogger().info("Punishment issued from menu for " + playerName + " by " + sender.getName() + ": /" + command.toString());
        sender.performCommand(command.toString());
    }

    private PunishConfig getConfig() {
        return getConfig(getPlayerUuid());
    }

    public static PunishConfig getConfig(UUID uuid) {
        if (!configs.containsKey(uuid)) {
            configs.put(uuid, PunishConfig.INAPPROPRIATE_BEHAVIOR.clone());
        }
        return configs.get(uuid);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (!event.getPlayer().getUniqueId().equals(getPlayerUuid())) return;
        if (this.mode != Mode.NONE) {
            switch (this.mode) {
                case SETTING_REASON:
                    this.mode = Mode.NONE;
                    Bukkit.getScheduler().runTask(TGM.get(), () -> {
                        getConfig().setReason(event.getMessage());
                        getPlayer().sendMessage(ChatColor.GRAY + "Set reason to: " + ChatColor.WHITE + event.getMessage());
                        updateMenu();
                        this.open(getPlayer());
                    });
                    event.setCancelled(true);
                    break;
                case SETTING_LENGTH:
                    this.mode = Mode.NONE;
                    Bukkit.getScheduler().runTask(TGM.get(), () -> {
                        TimeUnitPair timeUnitPair = TimeUnitPair.parse(event.getMessage());
                        if (timeUnitPair == null) {
                            getPlayer().sendMessage(ChatColor.RED + "Invalid duration. Should be: 1m, 1h, 1d, etc.");
                            this.open(getPlayer());
                            return;
                        }
                        getConfig().setTime(timeUnitPair);
                        getPlayer().sendMessage(ChatColor.GRAY + "Set length to: " + ChatColor.WHITE + getConfig().getTime().toString());
                        updateMenu();
                        this.open(getPlayer());
                    });
                    event.setCancelled(true);
                    break;
                default:
                    break;
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        players.add(event.getPlayer().getName());
        Collections.sort(players);
        if (this.liveUpdate) updateMenu();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (event.getPlayer().getUniqueId().equals(playerUuid)) {
            this.disable();
            return;
        }
        players.remove(event.getPlayer().getName());
        if (this.liveUpdate) updateMenu();
    }

    @Override
    public void disable() {
        this.players.clear();
        super.disable();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (this.mode != Mode.NONE) return;
        if (event.getInventory().equals(this.getInventory()) && event.getPlayer().getUniqueId().equals(playerUuid)) {
            super.disable();
        }
    }

    public static void openNew(Player player) {
        new PunishMenu(player).open(player);
    }

    @AllArgsConstructor @Data
    public static class PunishConfig {

        public static final PunishConfig INAPPROPRIATE_BEHAVIOR = new PunishConfig(PunishmentType.WARN, TimeUnitPair.permanent(), "Inappropriate Behavior", false);

        public static final PunishConfig SPAM_BOT = new PunishConfig(PunishmentType.BAN, TimeUnitPair.permanent(), "Spam Bot", true);
        public static final PunishConfig COMBAT_MOD = new PunishConfig(PunishmentType.BAN, TimeUnitPair.permanent(), "Client Modification (Combat)", false);
        public static final PunishConfig MOVEMENT_MOD = new PunishConfig(PunishmentType.BAN, TimeUnitPair.permanent(), "Client Modification (Movement)", false);
        public static final PunishConfig AUTO_CLICK = new PunishConfig(PunishmentType.BAN, TimeUnitPair.permanent(), "Gameplay Modification (Auto-clicker)", false);
        public static final PunishConfig BUG_EXPLOIT = new PunishConfig(PunishmentType.KICK, TimeUnitPair.permanent(), "Bug Exploiting", true);
        public static final PunishConfig DISCRIMINATION = new PunishConfig(PunishmentType.MUTE, TimeUnitPair.permanent(), "Discrimination", false);
        public static final PunishConfig MUTE_EVASION = new PunishConfig(PunishmentType.MUTE, TimeUnitPair.permanent(), "Mute Evasion", false);
        public static final PunishConfig SUICIDE_ENCOURAGEMENT = new PunishConfig(PunishmentType.MUTE, TimeUnitPair.permanent(), "Suicide Encouragement", false);
        public static final PunishConfig EXTREME_HARASSMENT = new PunishConfig(PunishmentType.MUTE, TimeUnitPair.permanent(), "Extreme Harassment", false);

        private PunishmentType type;
        private TimeUnitPair time;
        private String reason;
        private boolean silent;

        public PunishConfig clone() {
            return new PunishConfig(type, time, reason, silent);
        }

        public ItemStack toItem() {
            return ItemFactory.createItem(Material.ENDER_CHEST, ChatColor.YELLOW + getReason(), ChatColor.GRAY,
                    "",
                    "Type: " + ChatColor.WHITE + getType().name() + (getType().isTimed() ? " " + ChatColor.GRAY + "Time: " + ChatColor.RESET + getTime().toString() : ""),
                    "Silent: " + ChatColor.WHITE + isSilent()
            );
        }

    }

    private enum Mode {
        NONE,
        SETTING_LENGTH,
        SETTING_REASON
    }

    public static class PresetsMenu extends Menu {

        private File presets = new File(TGM.get().getDataFolder().getAbsolutePath() + "/punishmentPresets.json");

        private PresetsMenu() {
            super(ChatColor.UNDERLINE + "Presets", 9*3);
        }

        private void registerPresets(PunishConfig... configs) {
            int i = 0;
            for (PunishConfig config : configs) {
                registerPreset(i++, config);
            }
        }

        public void load() {
            clear();
            if (!this.presets.exists()) TGM.get().saveResource("punishmentPresets.json", true);
            try {
                PunishConfig[] configs = TGM.get().getGson().fromJson(new FileReader(presets), PunishConfig[].class);
                registerPresets(configs);
            } catch (FileNotFoundException e) {
                TGM.get().getLogger().warning("Punishment presets file not found. Using fallback presets.");
                registerPresets(
                        PunishConfig.SPAM_BOT,
                        PunishConfig.COMBAT_MOD,
                        PunishConfig.MOVEMENT_MOD,
                        PunishConfig.AUTO_CLICK,
                        PunishConfig.BUG_EXPLOIT,
                        PunishConfig.DISCRIMINATION,
                        PunishConfig.MUTE_EVASION,
                        PunishConfig.SUICIDE_ENCOURAGEMENT,
                        PunishConfig.EXTREME_HARASSMENT
                );
            }
            setItem(22, ItemFactory.createItem(Material.ARROW, ChatColor.RED + "Close"), (p, event) -> PunishMenu.openNew(p));
        }

        private void registerPreset(int i, PunishConfig config) {
            ItemStack item = config.toItem();
            ItemFactory.appendLore(item, "", ChatColor.YELLOW + "Click to select");
            setItem(i, item, (p, event) -> {
                configs.put(p.getUniqueId(), config.clone());
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
                PunishMenu.openNew(p);
            });
        }

    }
}
