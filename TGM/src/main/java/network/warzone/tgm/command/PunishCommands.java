package network.warzone.tgm.command;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import network.warzone.tgm.TGM;
import network.warzone.tgm.modules.reports.Report;
import network.warzone.tgm.modules.reports.ReportsModule;
import network.warzone.tgm.user.PlayerContext;
import network.warzone.warzoneapi.models.*;
import org.bson.types.ObjectId;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PunishCommands {

    private static final Pattern IP_PATTERN = Pattern.compile(
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    public static boolean isIP(final String ip) {
        return IP_PATTERN.matcher(ip).matches();
    }

    @Command(aliases = {"ban-ip", "banip"}, desc = "IP Ban a rulebreaker", min = 2, usage = "(name|ip) (length) (reason...)", anyFlags = true, flags = "s")
    @CommandPermissions({"tgm.punish.ban-ip"})
    public static void banIP(CommandContext cmd, CommandSender sender) {
        String name = cmd.getString(0);

        TimeUnitPair timeUnitPair = parseLength(cmd.getString(1));
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

        TimeUnitPair timeUnitPair = parseLength(cmd.getString(1));
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

        String reason = cmd.argsLength() > 1 ? cmd.getRemainingString(1) : "Inappropriate Behavior";

        issuePunishment("kick", name, sender, "kicked", new TimeUnitPair(1, ChronoUnit.MILLIS), reason, false, !cmd.hasFlag('s'));
    }

    @Command(aliases = "mute", desc = "Mute a rulebreaker", min = 2, usage = "(name) (length) (reason...)", anyFlags = true, flags = "s")
    @CommandPermissions({"tgm.punish.mute"})
    public static void mute(CommandContext cmd, CommandSender sender) {
        String name = cmd.getString(0);

        TimeUnitPair timeUnitPair = parseLength(cmd.getString(1));
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
                java.util.Map<ObjectId, String> map = new HashMap<>();
                for (PunishmentsListResponse.LoadedUser loadedUser : punishmentsListResponse.getLoadedUsers()) {
                    map.put(loadedUser.getId(), loadedUser.getName());
                }
                for (Punishment punishment : punishmentsListResponse.getPunishments()) {
                    sender.spigot().sendMessage(punishmentToTextComponent(punishment, map.get(punishment.getPunished()), map.getOrDefault(punishment.getPunisher(), "Console"), true));
                }
            }
        });
    }

    @Command(aliases = {"lookup", "lu"}, desc = "Get player info", min = 1, max = 1, usage = "(name|ip)")
    @CommandPermissions({"tgm.lookup"})
    public static void lookup(CommandContext cmd, CommandSender sender) {
        String filter = cmd.getString(0);
        Bukkit.getScheduler().runTaskAsynchronously(TGM.get(), () -> {
            PlayerInfoResponse playerInfoResponse = TGM.get().getTeamClient().getPlayerInfo(new PlayerInfoRequest(isIP(filter) ? null : filter, isIP(filter) ? filter : null));
            if (playerInfoResponse.isError()) {
                sender.sendMessage(ChatColor.RED + playerInfoResponse.getMessage());
            } else {
                sender.sendMessage(ChatColor.YELLOW + "Info for " + playerInfoResponse.getQueryFilter() + ":");
                if (isIP(playerInfoResponse.getQueryFilter())) {
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
                } else {
                    if (playerInfoResponse.getUsers().isEmpty()) return;
                    UserProfile profile = playerInfoResponse.getUsers().get(0);
                    sender.sendMessage(ChatColor.GRAY + "ID: " + ChatColor.RESET + profile.getId() +
                            "\n" + ChatColor.GRAY + "UUID: " + ChatColor.RESET + profile.getUuid() +
                            "\n" + ChatColor.GRAY + "Name: " + ChatColor.RESET + profile.getName() +
                            "\n" + ChatColor.GRAY + "First join: " + ChatColor.RESET + new Date(profile.getInitialJoinDate()).toString() +
                            "\n" + ChatColor.GRAY + "Last online: " + ChatColor.RESET + new Date(profile.getLastOnlineDate()).toString()
                    );
                }
            }
        });
    }

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
                alts.addAll(response.getUsers().stream().map(user -> ChatColor.GRAY + " - " + ChatColor.WHITE + user.getName()).collect(Collectors.toList()));
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
                java.util.Map<ObjectId, String> userMappings = new HashMap<>();
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

    @Command(aliases = {"sc", "staffchat", "staffc"}, desc = "Staff chat", min = 1, usage = "(message)")
    @CommandPermissions({"tgm.staffchat"})
    public static void staffchat(CommandContext cmd, CommandSender sender) {
        String prefix;
        if (sender instanceof Player) {
            PlayerContext playerContext = TGM.get().getPlayerManager().getPlayerContext((Player) sender);
            prefix = playerContext.getUserProfile().getPrefix() != null ? ChatColor.translateAlternateColorCodes('&', playerContext.getUserProfile().getPrefix().trim()) + " " : "";
        } else {
            prefix = "";
        }
        String result = ChatColor.DARK_RED + "[STAFF] " + prefix + ChatColor.GRAY + sender.getName() + ": " + ChatColor.RESET + cmd.getRemainingString(0);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("tgm.staffchat")) player.sendMessage(result);
        }
        Bukkit.getConsoleSender().sendMessage(result);
    }

    @Command(aliases = {"chat"}, desc = "Control chat settings", min = 1, usage = "(mute|clear)")
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
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
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

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("tgm.reports")) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4[REPORT]&8 [" + amount + "] &5" + reporter.getName() + "&7 reported &d" + reported.getName() + "&7 for &r" + cmd.getJoinedStrings(1)));
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
            sender.sendMessage(org.bukkit.ChatColor.RED + "Number expected.");
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
                        .append(org.bukkit.ChatColor.GRAY + "Date/Time: ").append(dateFormat.format(date)).append("\n")
                        .append(org.bukkit.ChatColor.GRAY + "Reporter: ").append(report.getReporter()).create()));

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
                    broadcastPunishment(response.getName(), response.getIp(), punisher.getName().replace("CONSOLE", "Console"), verb, timeUnitPair, reason, time, broadcast);
                    Player target;
                    if (response.getName() != null && (target = Bukkit.getPlayer(response.getName())) != null) {
                        TGM.get().getPlayerManager().getPlayerContext(target).getUserProfile().addPunishment(response.getPunishment());
                    }
                });
            }
        });
    }

    private static void broadcastPunishment(String name, String ip, String punisher, String verb, TimeUnitPair timeUnitPair, String reason, boolean time, boolean everyone) {
        ChatColor punisherColor = ChatColor.DARK_PURPLE;
        ChatColor punishedColor = ChatColor.LIGHT_PURPLE;
        ChatColor durationColor = ChatColor.LIGHT_PURPLE;
        if (name != null) {
            if (time) {
                if (everyone) Bukkit.broadcastMessage(punisherColor + punisher + ChatColor.GRAY + " " + verb + " " + punishedColor + name + ChatColor.GRAY +
                        " for " + durationColor + timeUnitPair.getTimeWord() + ChatColor.GRAY  +
                        " for " + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', reason));
                else {
                    Bukkit.getOnlinePlayers().stream().filter(player -> player.hasPermission("tgm.punish.list") || player.getName().equalsIgnoreCase(name)).forEach(player -> player.sendMessage(ChatColor.GRAY + "[SILENT] " + punisherColor + punisher + ChatColor.GRAY + " " + verb + " " + punishedColor + name + ChatColor.GRAY +
                            " for " + durationColor + timeUnitPair.getTimeWord() + ChatColor.GRAY  +
                            " for " + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', reason)));
                }
            } else {
                if (everyone) Bukkit.broadcastMessage(punisherColor + punisher + ChatColor.GRAY + " " + verb + " " + punishedColor + name + ChatColor.GRAY +
                        " for " + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', reason));
                else {
                    Bukkit.getOnlinePlayers().stream().filter(player -> player.hasPermission("tgm.punish.list") || player.getName().equalsIgnoreCase(name)).forEach(player -> player.sendMessage(ChatColor.GRAY + "[SILENT] " + punisherColor + punisher + ChatColor.GRAY + " " + verb + " " + punishedColor + name + ChatColor.GRAY +
                            " for " + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', reason)));
                }
            }
        } else {
            if (time) {
                String result = ChatColor.GRAY + "[SILENT] " + punisherColor + punisher + ChatColor.GRAY + " " + verb + " " + punishedColor + ip + ChatColor.GRAY +
                        " for " + durationColor + timeUnitPair.getTimeWord() + ChatColor.GRAY +
                        " for " + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', reason);
                Bukkit.getOnlinePlayers().stream().filter(player -> player.hasPermission("tgm.punish.list")).forEach(player -> player.sendMessage(result));
                Bukkit.getConsoleSender().sendMessage(result);
            } else {
                String result = ChatColor.GRAY + "[SILENT] " + punisherColor + punisher + ChatColor.GRAY + " " + verb + " " + punishedColor + ip + ChatColor.GRAY +
                        " for " + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', reason);
                Bukkit.getOnlinePlayers().stream().filter(player -> player.hasPermission("tgm.punish.list")).forEach(player -> player.sendMessage(result));
                Bukkit.getConsoleSender().sendMessage(result);
            }
        }
    }

    private static void kickPlayer(Punishment punishment, String name) {
            if (punishment.getType().toLowerCase().equals("ban")) {
                if (punishment.isIp_ban()) {
                    boolean found = false;
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player.getAddress().getHostString().equals(punishment.getIp())) {
                            player.kickPlayer(ChatColor.RED + "You have been banned from the server. Reason:\n"
                                            + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', punishment.getReason()) + "\n\n"
                                            + ChatColor.RED + "Ban expires: " + ChatColor.RESET +
                                            (punishment.getExpires() != -1 ? new Date(punishment.getExpires()).toString() : "Never") + "\n"
                                            + ChatColor.AQUA + "Appeal at https://discord.io/WarzoneMC\n"
                                            + ChatColor.GRAY + "ID: " + punishment.getId().toString());
                            found = true;
                        }
                    }
                    if (found) return;
                }
                Player player;
                if ((player = Bukkit.getPlayer(name)) != null)
                    player.kickPlayer(ChatColor.RED + "You have been banned from the server. Reason:\n"
                                + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', punishment.getReason()) + "\n\n"
                                + ChatColor.RED + "Ban expires: " + ChatColor.RESET +
                                (punishment.getExpires() != -1 ? new Date(punishment.getExpires()).toString() : "Never") + "\n"
                                + ChatColor.AQUA + "Appeal at https://discord.io/WarzoneMC\n"
                                + ChatColor.GRAY + "ID: " + punishment.getId().toString());
            } else {
                Bukkit.getPlayer(name).kickPlayer(ChatColor.RED + "You have been kicked from the server. Reason:\n" + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', punishment.getReason()));
            }

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

    private static TimeUnitPair parseLength(String s) {
        if (s.equalsIgnoreCase("permanent") ||
                s.equalsIgnoreCase("perm") ||
                s.equalsIgnoreCase("p") ||
                s.equalsIgnoreCase("forever") ||
                s.equalsIgnoreCase("f") ||
                s.equalsIgnoreCase("-1")) return new TimeUnitPair(1, ChronoUnit.FOREVER);
        ChronoUnit timeUnit;

        String time = "";
        String unit = "";
        boolean digitsDone = false;
        for (int i = 0; i < s.length(); i++) {
            if (!digitsDone && Character.isDigit(s.charAt(i))) {
                time += s.charAt(i);
            } else if (!Character.isDigit(s.charAt(i))) {
                digitsDone = true;
                unit += s.charAt(i);
            } else {
                break;
            }
        }
        timeUnit = getTimeUnit(unit);
        if (timeUnit == null) return null;
        return new TimeUnitPair(Integer.valueOf(time), timeUnit);
    }

    private static ChronoUnit getTimeUnit(String s) {
        for (ChronoUnit timeUnit : ChronoUnit.values()) {
            if (timeUnit == ChronoUnit.NANOS || timeUnit == ChronoUnit.MICROS || timeUnit == ChronoUnit.MILLIS) continue;
            if (timeUnit.name().toLowerCase().startsWith(s.toLowerCase())) {
                return timeUnit;
            }
        }
         return null;
    }

    @AllArgsConstructor @Getter
    public static class TimeUnitPair {

        private int value;
        private ChronoUnit timeUnit;

        public String getTimeWord() {
            if (timeUnit == ChronoUnit.FOREVER || toMilliseconds() == -1) return "permanent";
            if (value == 1) {
                if (timeUnit == ChronoUnit.MILLENNIA) return value + " millennium";
                if (timeUnit == ChronoUnit.CENTURIES) return value + " century";
                if (timeUnit.name().toLowerCase().endsWith("s")) {
                    return value + " " + timeUnit.name().substring(0, timeUnit.name().length() - 1).toLowerCase().replace("_", " ");
                }
            }
            return value + " " + timeUnit.name().toLowerCase().replace("_", " ");
        }

        public long toMilliseconds() {
            if (timeUnit == ChronoUnit.FOREVER) {
                return -1;
            }
            if (value <= 0) return -1;
            return timeUnit.getDuration().getSeconds() * value * 1000;
        }

    }

}
