package network.warzone.tgm.command;

import com.sk89q.minecraft.util.commands.*;
import net.md_5.bungee.api.chat.*;
import network.warzone.tgm.TGM;
import network.warzone.tgm.modules.chat.ChatConstant;
import network.warzone.tgm.modules.reports.Report;
import network.warzone.tgm.modules.reports.ReportsModule;
import network.warzone.tgm.user.PlayerContext;
import network.warzone.tgm.util.Players;
import network.warzone.tgm.util.TimeUnitPair;
import network.warzone.tgm.util.itemstack.ItemFactory;
import network.warzone.tgm.util.menu.ConfirmMenu;
import network.warzone.tgm.util.menu.PunishMenu;
import network.warzone.warzoneapi.models.*;
import org.bson.types.ObjectId;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Pattern;

public class PunishCommands {

    private static final Pattern IP_PATTERN = Pattern.compile(
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
    private static final ChatColor punisherColor = ChatColor.DARK_PURPLE;
    private static final ChatColor punishedColor = ChatColor.LIGHT_PURPLE;
    private static final ChatColor durationColor = ChatColor.LIGHT_PURPLE;

    public static boolean isIP(final String ip) {
        return IP_PATTERN.matcher(ip).matches();
    }

    @Command(aliases = {"punish", "pun", "pg", "pgui"}, desc = "Punishment interface", usage = "(reload | <players>)", flags = "s")
    public static void punish(CommandContext cmd, CommandSender sender) throws CommandPermissionsException {
        if (cmd.argsLength() == 1 && "reload".equalsIgnoreCase(cmd.getString(0))) {
            if (!sender.hasPermission("tgm.punish.preset.reload")) {
                throw new CommandPermissionsException();
            }
            PunishMenu.getPresetsMenu().load();
            sender.sendMessage(ChatColor.YELLOW + "Reloaded punishment presets.");
            return;
        }
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.hasPermission("tgm.punish.ban-ip") &&
                    !player.hasPermission("tgm.punish.ban") &&
                    !player.hasPermission("tgm.punish.kick") &&
                    !player.hasPermission("tgm.punish.mute") &&
                    !player.hasPermission("tgm.punish.warn")) {
                throw new CommandPermissionsException();
            }
            if (cmd.argsLength() >= 1) {
                String[] players = cmd.getRemainingString(0).split(" ");
                PunishMenu.PunishConfig config = PunishMenu.getConfig(player.getUniqueId());
                if (!cmd.hasFlag('s') || !player.hasPermission("tgm.punish.confirm.skip")) {
                    ItemStack configItem = config.toItem();
                    String[] lore = new String[players.length + 2];
                    lore[0] = "";
                    lore[1] = ChatColor.GRAY + "Players:";
                    for (int i = 0; i < players.length; i++) lore[i + 2] = ChatColor.GRAY + "- " + ChatColor.WHITE + players[i];
                    ItemFactory.appendLore(configItem, lore);
                    new ConfirmMenu(player, ChatColor.UNDERLINE + "Confirm bulk punish", configItem,
                            (p, e) -> {
                                Arrays.stream(players).forEach(username -> PunishMenu.issuePunishment(username, player, config));
                                p.closeInventory();
                                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
                            }, (p, e) -> {
                                p.closeInventory();
                                p.sendMessage(ChatColor.RED + "Bulk punish cancelled.");
                    }).open(player);
                    return;
                }
                Arrays.stream(players).forEach(username -> PunishMenu.issuePunishment(username, player, config));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
                return;
            }
            PunishMenu.openNew(player);
        } else {
            throw new CommandPermissionsException();
        }
    }

    @Command(aliases = {"ban-ip", "banip"}, desc = "IP Ban a rulebreaker", min = 2, usage = "(name|ip) (length) (reason...)", anyFlags = true, flags = "s")
    @CommandPermissions({"tgm.punish.ban-ip"})
    public static void banIP(CommandContext cmd, CommandSender sender) {
        String name = cmd.getString(0);

        TimeUnitPair timeUnitPair = TimeUnitPair.parse(cmd.getString(1));
        if (timeUnitPair == null) {
            sender.sendMessage(ChatColor.RED + "Invalid duration. Should be: 1m, 1h, 1d, etc.");
            return;
        }

        String reason = cmd.argsLength() > 2 ? cmd.getRemainingString(2) : "Inappropriate Behavior";
        if (isIP(name)) {
            issuePunishment("ban", null, name, true, sender, "IP banned", timeUnitPair, reason, true, !cmd.hasFlag('s'));
        } else {
            Player target;
            if ((target = Bukkit.getPlayer(name)) != null) {
                issuePunishment("ban", name, target.getAddress().getHostString(), true, sender, "IP banned", timeUnitPair, reason, true, !cmd.hasFlag('s'));
            } else {
                issuePunishment("ban", name, null, true, sender, "IP banned", timeUnitPair, reason, true, !cmd.hasFlag('s'));
            }
        }
    }

    @Command(aliases = "ban", desc = "Ban a rulebreaker", min = 2, usage = "(name) (length) (reason...)", anyFlags = true, flags = "s")
    @CommandPermissions({"tgm.punish.ban"})
    public static void ban(CommandContext cmd, CommandSender sender) {
        String name = cmd.getString(0);

        TimeUnitPair timeUnitPair = TimeUnitPair.parse(cmd.getString(1));
        if (timeUnitPair == null) {
            sender.sendMessage(ChatColor.RED + "Invalid duration. Should be: 1m, 1h, 1d, etc.");
            return;
        }

        String reason = cmd.argsLength() > 2 ? cmd.getRemainingString(2) : "Inappropriate Behavior";

        issuePunishment("ban", name, sender, "banned", timeUnitPair, reason, true, !cmd.hasFlag('s'));
    }

    @Command(aliases = "kick", desc = "Kick a rulebreaker", min = 1, usage = "(name) (reason...)", anyFlags = true, flags = "s")
    @CommandPermissions({"tgm.punish.kick"})
    public static void kick(CommandContext cmd, CommandSender sender) {
        String name = cmd.getString(0);
        Player player = Bukkit.getPlayer(name);
        if (player == null) {
            sender.sendMessage(ChatColor.RED + "Player not found: " + name);
            return;
        }
        String reason = cmd.argsLength() > 1 ? cmd.getRemainingString(1) : "Inappropriate Behavior";

        issuePunishment("kick", name, sender, "kicked", new TimeUnitPair(1, ChronoUnit.MILLIS), reason, false, !cmd.hasFlag('s'));
    }

    @Command(aliases = "mute", desc = "Mute a rulebreaker", min = 2, usage = "(name) (length) (reason...)", anyFlags = true, flags = "s")
    @CommandPermissions({"tgm.punish.mute"})
    public static void mute(CommandContext cmd, CommandSender sender) {
        String name = cmd.getString(0);

        TimeUnitPair timeUnitPair = TimeUnitPair.parse(cmd.getString(1));
        if (timeUnitPair == null) {
            sender.sendMessage(ChatColor.RED + "Invalid duration. Should be: 1m, 1h, 1d, etc.");
            return;
        }

        String reason = cmd.argsLength() > 2 ? cmd.getRemainingString(2) : "Inappropriate Behavior";

        issuePunishment("mute", name, sender, "muted", timeUnitPair, reason, true, !cmd.hasFlag('s'));

    }

    @Command(aliases = "warn", desc = "Warn a rulebreaker", min = 1, usage = "(name) (reason...)", anyFlags = true, flags = "s")
    @CommandPermissions({"tgm.punish.warn"})
    public static void warn(CommandContext cmd, CommandSender sender) {
        String name = cmd.getString(0);

        String reason = cmd.argsLength() > 1 ? cmd.getRemainingString(1) : "Inappropriate Behavior";

        issuePunishment("warn", name, sender, "warned", new TimeUnitPair(1, ChronoUnit.MILLIS), reason, false, !cmd.hasFlag('s'));
    }

    @Command(aliases = {"punishments", "p"}, desc = "Get player punishments", min = 1, max = 1, usage = "(name|ip)")
    @CommandPermissions({"tgm.punish.list"})
    public static void punishments(CommandContext cmd, CommandSender sender) {
        String name = cmd.getString(0);
        Bukkit.getScheduler().runTaskAsynchronously(TGM.get(), () -> {
            PunishmentsListResponse punishmentsListResponse = TGM.get().getTeamClient().getPunishments(new PunishmentsListRequest(!isIP(name) ? name : null, isIP(name) ? name : null));
            if (punishmentsListResponse.isNotFound()) {
                sender.sendMessage(ChatColor.RED + "Player not found!");
            } else {
                //if (punishmentsListResponse.getLoadedUsers() != null) sender.sendMessage(ChatColor.YELLOW + "Punishments for " + punishmentsListResponse.getUserProfile().getName());
                String displayName = name;
                for (PunishmentsListResponse.LoadedUser user : punishmentsListResponse.getLoadedUsers()) {
                    if (name.equalsIgnoreCase(user.getName())) {
                        displayName = user.getName();
                    }
                }
                sender.sendMessage(ChatColor.YELLOW + "Punishments for " + displayName);
                HashMap<ObjectId, String> map = new HashMap<>();
                for (PunishmentsListResponse.LoadedUser loadedUser : punishmentsListResponse.getLoadedUsers()) {
                    map.put(loadedUser.getId(), loadedUser.getName());
                }
                for (Punishment punishment : punishmentsListResponse.getPunishments()) {
                    sender.spigot().sendMessage(punishmentToTextComponent(punishment, map.get(punishment.getPunished()), map.getOrDefault(punishment.getPunisher(), "Console"), true));
                }
            }
        });
    }

    @Command(aliases = {"lookup", "lu"}, desc = "Get player info", min = 1, max = 1, usage = "(name)")
    @CommandPermissions({"tgm.lookup"})
    public static void lookup(CommandContext cmd, CommandSender sender) {
        String filter = cmd.getString(0);
        Bukkit.getScheduler().runTaskAsynchronously(TGM.get(), () -> {
            PlayerInfoResponse playerInfoResponse = TGM.get().getTeamClient().getPlayerInfo(new PlayerInfoRequest(filter, null));
            if (playerInfoResponse.isError()) {
                sender.sendMessage(ChatColor.RED + playerInfoResponse.getMessage());
            } else {
                sender.sendMessage(ChatColor.YELLOW + "Info for " + playerInfoResponse.getQueryFilter() + ":");
                if (playerInfoResponse.getUsers().isEmpty()) return;
                UserProfile profile = playerInfoResponse.getUsers().get(0);
                sender.sendMessage(ChatColor.GRAY + "ID: " + ChatColor.RESET + profile.getId() +
                        "\n" + ChatColor.GRAY + "UUID: " + ChatColor.RESET + profile.getUuid() +
                        "\n" + ChatColor.GRAY + "Name: " + ChatColor.RESET + profile.getName() +
                        "\n" + ChatColor.GRAY + "First join: " + ChatColor.RESET + new Date(profile.getInitialJoinDate()).toString() +
                        "\n" + ChatColor.GRAY + "Last online: " + ChatColor.RESET + new Date(profile.getLastOnlineDate()).toString()
                );
            }
        });
    }

    @Command(aliases = {"lookupip", "luip"}, desc = "Get IP info", min = 1, max = 1, usage = "(ip)")
    @CommandPermissions({"tgm.lookup"})
    public static void lookupip(CommandContext cmd, CommandSender sender) {
        String filter = cmd.getString(0);
        Bukkit.getScheduler().runTaskAsynchronously(TGM.get(), () -> {
            PlayerInfoResponse playerInfoResponse = TGM.get().getTeamClient().getPlayerInfo(new PlayerInfoRequest(null, filter));
            if (playerInfoResponse.isError()) {
                sender.sendMessage(ChatColor.RED + playerInfoResponse.getMessage());
            } else {
                sender.sendMessage(ChatColor.YELLOW + "Info for IP " + playerInfoResponse.getQueryFilter() + ":");
                sender.sendMessage(ChatColor.GRAY + "Users " + ChatColor.RESET + "(" + playerInfoResponse.getUsers().size() + ")" + ChatColor.GRAY + ":");
                for (UserProfile profile : playerInfoResponse.getUsers()) {
                    sender.spigot().sendMessage(new ComponentBuilder(ChatColor.GRAY + " - " + ChatColor.RESET + profile.getName()).event(
                            new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(
                                    ChatColor.GRAY + "ID: " + ChatColor.RESET + profile.getId() +
                                            "\n" + ChatColor.GRAY + "UUID: " + ChatColor.RESET + profile.getUuid() +
                                            "\n" + ChatColor.GRAY + "Name: " + ChatColor.RESET + profile.getName() +
                                            "\n" + ChatColor.GRAY + "First join: " + ChatColor.RESET + new Date(profile.getInitialJoinDate()).toString() +
                                            "\n" + ChatColor.GRAY + "Last online: " + ChatColor.RESET + new Date(profile.getLastOnlineDate()).toString()
                            ).create())).event(
                            new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/lookup " + profile.getName())
                    ).create());
                }
            }
        });
    }

    @SuppressWarnings("DuplicatedCode")
    @Command(aliases = "alts", desc = "Lookup alts of a user", min = 1, max = 1, usage = "(name)")
    @CommandPermissions("tgm.lookup")
    public static void alts(CommandContext cmd, CommandSender sender) {
        Bukkit.getScheduler().runTaskAsynchronously(TGM.get(), () -> {
            PlayerAltsResponse response = TGM.get().getTeamClient().getAlts(cmd.getString(0));
            if (response == null) {
                sender.sendMessage(ChatColor.RED + "Something went wrong");
            } else if (response.isError()) {
                sender.sendMessage(ChatColor.RED + response.getMessage());
            } else {
                if (response.getUsers().isEmpty()) {
                    sender.sendMessage(ChatColor.GREEN + response.getLookupUser().getName() + " has no known alts.");
                    return;
                }

                String name = response.getLookupUser().getName();
                List<String> alts = new ArrayList<>(Arrays.asList(
                        "",
                        ChatColor.AQUA + name + (name.endsWith("s") ? "'" : "'s") + " known alts:"
                ));

                for (UserProfile user : response.getUsers()) {
                    boolean isBanned = false;
                    boolean isMuted = false;

                    PunishmentsListResponse punishmentsListResponse = TGM.get().getTeamClient().getPunishments(new PunishmentsListRequest(user.getName(), null));
                    if (!punishmentsListResponse.isNotFound()) {
                        for (Punishment punishment : punishmentsListResponse.getPunishments()) {
                            if ("BAN".equals(punishment.getType().toUpperCase()) && punishment.isActive()) {
                                isBanned = true;
                                break;
                            }

                            if ("MUTE".equals(punishment.getType().toUpperCase()) && punishment.isActive()) {
                                isMuted = true;
                            }
                        }
                    }

                    ChatColor chatColor = ChatColor.WHITE;

                    if (isMuted) {
                        chatColor = ChatColor.YELLOW;
                    }

                    if (isBanned) {
                        chatColor = ChatColor.RED;
                    }

                    alts.add(ChatColor.GRAY + " - " + chatColor + user.getName());
                }

                alts.add(" ");
                sender.sendMessage(alts.toArray(new String[0]));
            }
        });
    }

    @Command(aliases = "revert", desc = "Revert a punishment", min = 1, max = 1, usage = "(id)")
    @CommandPermissions({"tgm.punish.revert"})
    public static void revert(CommandContext cmd, CommandSender sender) {
        String id = cmd.getString(0);
        Bukkit.getScheduler().runTaskAsynchronously(TGM.get(), () -> {
            RevertPunishmentResponse revertPunishmentResponse = TGM.get().getTeamClient().revertPunishment(id);
            if (revertPunishmentResponse == null || revertPunishmentResponse.isNotFound()) {
                sender.sendMessage(ChatColor.RED + "Punishment not found.");
            } else {
                HashMap<ObjectId, String> userMappings = new HashMap<>();
                for (RevertPunishmentResponse.LoadedUser loadedUser : revertPunishmentResponse.getLoadedUsers()) {
                    userMappings.put(loadedUser.getId(), loadedUser.getName());
                }
                if (revertPunishmentResponse.isSuccess()) {
                    Bukkit.getOnlinePlayers().stream().filter(player -> player.hasPermission("tgm.punish.revert")).forEach(player ->
                            player.spigot().sendMessage(new TextComponent(ChatColor.YELLOW + sender.getName() + " reverted "),
                                    punishmentToTextComponent(
                                            revertPunishmentResponse.getPunishment(),
                                            userMappings.get(revertPunishmentResponse.getPunishment().getPunished()),
                                            userMappings.get(revertPunishmentResponse.getPunishment().getPunisher()),
                                            false)
                            )
                    );
                    TGM.get().getPlayerManager().getPlayers().forEach(playerContext -> {
                        Punishment punishment;
                        if ((punishment = playerContext.getUserProfile().getPunishment(revertPunishmentResponse.getPunishment().getId())) != null) {
                            punishment.setReverted(true);
                        }
                    });
                    sender.sendMessage(ChatColor.GREEN + "Punishment reverted.");
                } else {
                    sender.sendMessage(ChatColor.RED + "Punishment was already reverted.");
                }
            }
        });
    }

    @Command(aliases = {"chat", "c"}, desc = "Control chat settings", min = 1, usage = "(mute|clear)")
    @CommandPermissions({"tgm.chat.control"})
    public static void chat(CommandContext cmd, CommandSender sender) {
        String action = cmd.getString(0);
        if (action.equalsIgnoreCase("mute")) {
            if (TGM.get().getConfig().getBoolean("chat.enabled")) {
                TGM.get().getConfig().set("chat.enabled", false);
                TGM.get().saveConfig();
                Bukkit.broadcastMessage(ChatColor.DARK_AQUA + sender.getName() + " muted the chat.");
            } else {
                TGM.get().getConfig().set("chat.enabled", true);
                TGM.get().saveConfig();
                Bukkit.broadcastMessage(ChatColor.DARK_AQUA + sender.getName() + " unmuted the chat.");
            }
        } else if (action.equalsIgnoreCase("clear")) {
            for (int i = 0; i < 100; i++) {
                Bukkit.broadcastMessage("\n");
            }
            Bukkit.broadcastMessage(ChatColor.DARK_AQUA + sender.getName() + " cleared the chat.");
        }
    }

    @Command(aliases = {"report"}, desc = "Report a player", min = 2, usage = "(name) (reason...)")
    public static void report(CommandContext cmd, CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatConstant.ERROR_COMMAND_PLAYERS_ONLY.toString());
            return;
        }

        Player reporter = (Player) sender;
        Player reported = Bukkit.getPlayer(cmd.getString(0));

        if (reported == null) {
            reporter.sendMessage(ChatColor.RED + "Player not found!");
            return;
        }

        if (reported.getName().equals(reporter.getName())) {
            reporter.sendMessage(ChatColor.RED + "You can't report yourself!");
            return;
        }

        if (ReportsModule.cooldown(reporter.getUniqueId().toString())) {
            reporter.sendMessage(ChatColor.RED + "Please wait until reporting again!");
            return;
        }

        int amount = ReportsModule.getAmount(reported.getUniqueId().toString()) + 1;

        Report report = new Report()
                .setReporter(reporter.getName())
                .setReported(reported.getName())
                .setReason(cmd.getJoinedStrings(1))
                .setTimestamp(System.currentTimeMillis())
                .setAmount(amount);

        ReportsModule.addReport(report);
        ReportsModule.setAmount(reported.getUniqueId().toString(), amount);
        ReportsModule.setCooldown(reporter.getUniqueId().toString(), report.getTimestamp());

        PlayerContext reportedContext = TGM.get().getPlayerManager().getPlayerContext(reported.getUniqueId());
        StringBuilder reportedNameBuilder = new StringBuilder(reported.getName());

        if (reportedContext.isNicked()) {
            reportedNameBuilder.append(" ").append("&8(&a").append(reportedContext.getOriginalName()).append("&8)");
        }

        String reportedName = reportedNameBuilder.toString();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("tgm.reports")) {
                TextComponent message = new TextComponent(ChatColor.translateAlternateColorCodes('&',
                        "&4[REPORT]&8 [" + amount + "] &5" + reporter.getName() + " &7reported &d" +
                                reportedName + " &7for &r" + cmd.getJoinedStrings(1)));
                message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp " + report.getReported()));
                player.spigot().sendMessage(message);
            }
        }

        reporter.sendMessage(ChatColor.GREEN + "Your report has been sent to online staff.");
    }

    @Command(aliases = {"reports"}, desc = "View reports", max = 1, usage = "[page]")
    @CommandPermissions({"tgm.reports"})
    public static void reports(CommandContext cmd, CommandSender sender) throws CommandException {
        if (cmd.argsLength() == 1 && cmd.getString(0).equalsIgnoreCase("clear")) {
            ReportsModule.clear();
            sender.sendMessage(ChatColor.GREEN + "Cleared all reports.");
            return;
        }

        int index;

        try {
            index = cmd.argsLength() == 0 ? 1 : cmd.getInteger(0);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Number expected.");
            return;
        }

        List<Report> reports = ReportsModule.getReports();

        if (reports.size() == 0) {
            sender.sendMessage(ChatColor.RED + "No reports found.");
            return;
        }

        int pageSize = 9;

        int pagesRemainder = reports.size() % pageSize;
        int pagesDivisible = reports.size() / pageSize;
        int pages = pagesDivisible;

        if (pagesRemainder >= 1) {
            pages = pagesDivisible + 1;
        }

        if ((index > pages) || (index <= 0)) {
            index = 1;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");

        sender.sendMessage(ChatColor.YELLOW + "Reports (" + index + "/" + pages + "): ");
        try {
            for (int i = 0; i < pageSize; i++) {
                int position = pageSize * (index - 1) + i;
                Report report = reports.get(reports.size() - position - 1); // List new reports first

                String reported = ChatColor.translateAlternateColorCodes('&', "&8[" + report.getAmount() + "] &5" + report.getReported() + "&7 - &f" + report.getReason() + "&7 (" + report.getAgo() + " ago)");

                Date date = new Date();
                date.setTime(report.getTimestamp());

                TextComponent message = new TextComponent(reported);
                message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp " + report.getReported()));
                message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GOLD + reported).append("\n\n")
                        .append(ChatColor.GRAY + "Date/Time: ").append(dateFormat.format(date)).append("\n")
                        .append(ChatColor.GRAY + "Reporter: ").append(report.getReporter()).create()));

                sender.spigot().sendMessage(message);
            }
        } catch (IndexOutOfBoundsException ignored) {}
    }

    private static void issuePunishment(String type, String name, CommandSender punisher, String verb, TimeUnitPair timeUnitPair, String reason, boolean time, boolean broadcast) {
        issuePunishment(type, name, null, false, punisher, verb, timeUnitPair, reason, time, broadcast);
    }

    private static void issuePunishment(String type, String name, String ip, boolean ip_ban, CommandSender punisher, String verb, TimeUnitPair timeUnitPair, String reason, boolean time, boolean broadcast) {
        Bukkit.getScheduler().runTaskAsynchronously(TGM.get(), () -> {
            IssuePunishmentResponse response = TGM.get().getTeamClient().issuePunishment(
                    new IssuePunishmentRequest(
                            name,
                            ip,
                            ip_ban,
                            punisher instanceof Player ? ((Player) punisher).getUniqueId() : null,
                            type.toUpperCase(),
                            timeUnitPair.toMilliseconds(),
                            reason)
            );
            if (response.isNotFound()) {
                punisher.sendMessage(ChatColor.RED + "Player not found!");
            } else {
                Bukkit.getScheduler().runTask(TGM.get(), () -> {
                    if (response.isKickable()) {
                        kickPlayer(response.getPunishment(), response.getName());
                    }

                    broadcastPunishment(response.getName(), response.getIp(), TGM.get().getNickManager().getOriginalName(punisher.getName()).replace("CONSOLE", "Console"), verb, timeUnitPair, reason, time, broadcast);
                    Player target;
                    if (response.getName() != null && (target = Bukkit.getPlayer(response.getName())) != null) {
                        TGM.get().getPlayerManager().getPlayerContext(target).getUserProfile().addPunishment(response.getPunishment());
                    }
                });
            }
        });
    }

    private static boolean kickPlayer(Punishment punishment, String name) {
        if (punishment.getType().equalsIgnoreCase("ban")) {
            String reason = ChatColor.RED + "You have been banned from the server. Reason:\n"
                    + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', punishment.getReason()) + "\n\n"
                    + ChatColor.RED + "Ban expires: " + ChatColor.RESET +
                    (punishment.getExpires() != -1 ? new Date(punishment.getExpires()).toString() : "Never") + "\n"
                    + ChatColor.AQUA + "Appeal at " + TGM.get().getConfig().getString("server.appeal") + "\n"
                    + ChatColor.GRAY + "ID: " + punishment.getId().toString();
            if (punishment.isIp_ban()) {
                boolean found = false;
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (Objects.requireNonNull(player.getAddress()).getHostString().equals(punishment.getIp())) {
                        player.kickPlayer(reason);
                        found = true;
                    }
                }
                if (found) return true;
            }
            Player player;
            if ((player = Bukkit.getPlayer(name)) != null) {
                player.kickPlayer(reason);
                return true;
            }
        } else {
            Player player = Bukkit.getPlayer(name);
            if (player != null) {
                player.kickPlayer(ChatColor.RED + "You have been kicked from the server. Reason:\n" + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', punishment.getReason()));
                return true;
            }
        }
        return false;
    }

    private static TextComponent punishmentToTextComponent(Punishment punishment, String punished, String punisher, boolean revertOption) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        TextComponent textComponent = new TextComponent(ChatColor.GRAY + "[" + dateFormat.format(new Date(punishment.getIssued())) + "] "
                + (punishment.isReverted() ? ChatColor.STRIKETHROUGH + "" : (punishment.isActive() ? ChatColor.RED : ChatColor.YELLOW))
                + punishment.getType() + ChatColor.RESET + " " + (punished == null ? punishment.getIp() : punished));

        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{
                new TextComponent(ChatColor.GRAY + "ID: "               + ChatColor.RESET + punishment.getId().toString()),
                new TextComponent(ChatColor.GRAY + "\nType: "           + ChatColor.RESET + punishment.getType().toUpperCase()),
                new TextComponent(ChatColor.GRAY + "\nPunished IP: "    + ChatColor.RESET + punishment.getIp()),
                new TextComponent(ChatColor.GRAY + "\nIP Punishment: "  + ChatColor.RESET + punishment.isIp_ban()),
                new TextComponent(ChatColor.GRAY + "\nIssued by: "      + ChatColor.RESET + punisher),
                new TextComponent(ChatColor.GRAY + "\nReverted: "       + ChatColor.RESET + punishment.isReverted()),
                new TextComponent(ChatColor.GRAY + "\nIssued: "         + ChatColor.RESET + new Date(punishment.getIssued()).toString()),
                new TextComponent(ChatColor.GRAY + "\nExpires: "        + ChatColor.RESET + (punishment.getExpires() != -1 ? new Date(punishment.getExpires()).toString() : "Never")),
                new TextComponent(ChatColor.GRAY + "\n\nReason: "       + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', punishment.getReason()) +
                (revertOption && !punishment.isReverted() ? "\n\n" + ChatColor.YELLOW + "Click to revert" : ""))
        }));

        if (revertOption && !punishment.isReverted()) textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/revert " + punishment.getId().toString()));
        return textComponent;
    }

    private static void broadcastPunishment(String name, String ip, String punisher, String action, TimeUnitPair timeUnitPair, String reason, boolean timed, boolean everyone) {
        TGM.get().getLogger().info(String.format("new-punishment {name=%s, ip=%s, punisher=%s, action=%s, timeUnitPair=%s, reason=%s, timed=%b, public=%s}",
                name, ip, punisher, action, timeUnitPair.toString(), reason, timed, everyone));
        if (name != null) {
            if (timed) {
                if (everyone) { // Timed & Public
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (p.hasPermission("tgm.punish.list")) {
                            Players.sendMessage(p, "%s%s&7 has been &c%s&7 by %s%s&7 for %s%s&7 for &r%s",
                                    punishedColor, name,
                                    action,
                                    punisherColor, punisher,
                                    durationColor, timeUnitPair,
                                    ChatColor.translateAlternateColorCodes('&', reason)
                            );
                        } else {
                            Players.sendMessage(p, "%s%s&7 has been &c%s&7 for %s%s&7 for &r%s",
                                    punishedColor, name,
                                    action,
                                    durationColor, timeUnitPair,
                                    ChatColor.translateAlternateColorCodes('&', reason)
                            );
                        }
                    }
                } else { // Timed & Silent
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (p.hasPermission("tgm.punish.list")) {
                            Players.sendMessage(p, "&7[SILENT] %s%s&7 has been &c%s&7 by %s%s&7 for %s%s&7 for &r%s",
                                    punishedColor, name,
                                    action,
                                    punisherColor, punisher,
                                    durationColor, timeUnitPair,
                                    ChatColor.translateAlternateColorCodes('&', reason)
                            );
                        } else if (p.getName().equalsIgnoreCase(name)) {
                            Players.sendMessage(p, "%s%s&7 has been &c%s&7 for %s%s&7 for &r%s",
                                    punishedColor, name,
                                    action,
                                    durationColor, timeUnitPair,
                                    ChatColor.translateAlternateColorCodes('&', reason)
                            );
                        }
                    }
                }
            } else {
                if (everyone) { // Non-timed & Public
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (p.hasPermission("tgm.punish.list")) {
                            Players.sendMessage(p, "%s%s&7 has been &c%s&7 by %s%s&7 for &r%s",
                                    punishedColor, name,
                                    action,
                                    punisherColor, punisher,
                                    ChatColor.translateAlternateColorCodes('&', reason)
                            );
                        } else {
                            Players.sendMessage(p, "%s%s&7 has been &c%s&7 for &r%s",
                                    punishedColor, name,
                                    action,
                                    ChatColor.translateAlternateColorCodes('&', reason)
                            );
                        }
                    }
                } else { // Non-timed & Silent
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (p.hasPermission("tgm.punish.list")) {
                            Players.sendMessage(p, "&7[SILENT] %s%s&7 has been &c%s&7 by %s%s&7 for &r%s",
                                    punishedColor, name,
                                    action,
                                    punisherColor, punisher,
                                    ChatColor.translateAlternateColorCodes('&', reason)
                            );
                        } else if (p.getName().equalsIgnoreCase(name)) {
                            Players.sendMessage(p, "%s%s&7 has been &c%s&7 for &r%s",
                                    punishedColor, name,
                                    action,
                                    ChatColor.translateAlternateColorCodes('&', reason)
                            );
                        }
                    }
                }
            }
        } else { // IP Ban, no need to broadcast publicly.
            if (timed) { // Timed & Silent
                String result = String.format("&7[SILENT] %s%s&7 has been &c%s&7 by %s%s&7 for %s%s&7 for &r%s",
                        punishedColor, ip,
                        action,
                        punisherColor, punisher,
                        durationColor, timeUnitPair,
                        ChatColor.translateAlternateColorCodes('&', reason)
                );
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.hasPermission("tgm.punish.list")) {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', result));
                    }
                }
            } else { // Non-timed & Silent
                String result = String.format("&7[SILENT] %s%s&7 has &c%s&7 %s%s&7 for &r%s",
                        punisherColor, punisher,
                        action,
                        punishedColor, ip,
                        ChatColor.translateAlternateColorCodes('&', reason)
                );
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.hasPermission("tgm.punish.list")) {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', result));
                    }
                }
            }
        }
    }

}
