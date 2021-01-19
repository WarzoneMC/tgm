package network.warzone.tgm.command;

import com.sk89q.minecraft.util.commands.*;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import network.warzone.tgm.TGM;
import network.warzone.tgm.config.TGMConfigReloadEvent;
import network.warzone.tgm.gametype.GameType;
import network.warzone.tgm.map.MapContainer;
import network.warzone.tgm.map.MapInfo;
import network.warzone.tgm.match.MatchStatus;
import network.warzone.tgm.modules.chat.ChatChannel;
import network.warzone.tgm.modules.chat.ChatConstant;
import network.warzone.tgm.modules.chat.ChatModule;
import network.warzone.tgm.modules.countdown.*;
import network.warzone.tgm.modules.killstreak.KillstreakModule;
import network.warzone.tgm.modules.kit.classes.GameClassModule;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.modules.team.TeamUpdateEvent;
import network.warzone.tgm.modules.time.TimeModule;
import network.warzone.tgm.player.event.PlayerJoinTeamAttemptEvent;
import network.warzone.tgm.user.PlayerContext;
import network.warzone.tgm.util.ColorConverter;
import network.warzone.tgm.util.Strings;
import network.warzone.tgm.util.menu.ClassMenu;
import network.warzone.warzoneapi.models.GetPlayerByNameResponse;
import network.warzone.warzoneapi.models.LeaderboardCriterion;
import network.warzone.warzoneapi.models.UserProfile;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CycleCommands {

    @Command(aliases = {"maps"}, desc = "View the maps that are on server, although not necessarily in the rotation.", usage = "[type]? [page]")
    public static void maps(CommandContext cmd, CommandSender sender) throws CommandException {
        int index = 1;
        String typeString = "";

        try {
            if (cmd.argsLength() == 1) {
                if (cmd.getString(0).matches("[0-9]+")) {
                    index = cmd.getInteger(0);
                } else {
                    typeString = cmd.getString(0);
                }
            } else if (cmd.argsLength() == 2) {
                typeString = cmd.getString(0);
                index = cmd.getInteger(1);
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Number expected.");
            return;
        }

        GameType type = null;
        for (GameType gameType : GameType.values()) {
            if (gameType.name().equalsIgnoreCase(typeString)) {
                type = gameType;
            }
        }

        List<MapContainer> mapLibrary = TGM.get().getMatchManager().getMapLibrary().getMaps();

        if (type != null) {
            final GameType finalType = type;
            mapLibrary = mapLibrary.stream().filter(map -> map.getMapInfo().getGametype().equals(finalType)).collect(Collectors.toList());
        }

        int pageSize = 9;

        int pagesRemainder = mapLibrary.size() % pageSize;
        int pagesDivisible = mapLibrary.size() / pageSize;
        int pages = pagesDivisible;

        if (pagesRemainder >= 1) {
            pages = pagesDivisible + 1;
        }

        if ((index > pages) || (index <= 0)) {
            index = 1;
        }

        sender.sendMessage(ChatColor.YELLOW + "Maps (" + index + "/" + pages + "): ");
        try {
            for (int i = 0; i < pageSize; i++) {
                int position = pageSize * (index - 1) + i;
                MapContainer map = mapLibrary.get(position);
                TextComponent message = mapToTextComponent(position, map.getMapInfo());
                sender.spigot().sendMessage(message);
            }
        } catch (IndexOutOfBoundsException ignored) {
        }
    }

    @Command(aliases = {"findmaps"}, desc = "Find the maps that are on the server, although not necessarily in the rotation.", min = 1, usage = "<map name> [page]")
    public static void findmaps(CommandContext cmd, CommandSender sender) throws CommandException {
        List<MapContainer> mapLibrary = TGM.get().getMatchManager().getMapLibrary().getMaps();
        List<Integer> foundMaps = new ArrayList<>();

        for (int i = 0; i < mapLibrary.size(); i++) {
            if (cmd.getString(0).equalsIgnoreCase(mapLibrary.get(i).getMapInfo().getName())) { // Map with the exact same as input should always be listed first
                foundMaps.add(0, i);
            } else if (mapLibrary.get(i).getMapInfo().getName().toLowerCase().startsWith(cmd.getString(0).toLowerCase())) {
                foundMaps.add(i);
            }
        }

        if (foundMaps.size() == 0) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cNo maps with the name &4" + cmd.getString(0) + "&c were found."));
            return;
        }

        int index = cmd.argsLength() == 1 ? 1 : cmd.getInteger(1);

        int pageSize = 9;

        int pagesRemainder = foundMaps.size() % pageSize;
        int pagesDivisible = foundMaps.size() / pageSize;
        int pages = pagesDivisible;

        if (pagesRemainder >= 1) {
            pages = pagesDivisible + 1;
        }

        if ((index > pages) || (index <= 0)) {
            index = 1;
        }

        sender.sendMessage(ChatColor.YELLOW + "Found Maps (" + index + "/" + pages + "): ");
        try {
            for (int i = 0; i < pageSize; i++) {
                int position = pageSize * (index - 1) + i;
                MapContainer map = mapLibrary.get(foundMaps.get(position));
                TextComponent message = mapToTextComponent(position, map.getMapInfo());
                sender.spigot().sendMessage(message);
            }
        } catch (IndexOutOfBoundsException ignored) {
        }
    }

    @Command(aliases = {"cycle"}, desc = "Cycle to a new map.")
    @CommandPermissions({"tgm.cycle"})
    public static void cycle(CommandContext cmd, CommandSender sender) {
        MatchStatus matchStatus = TGM.get().getMatchManager().getMatch().getMatchStatus();
        if (matchStatus != MatchStatus.MID) {
            int time = CycleCountdown.START_TIME;
            if (cmd.argsLength() > 0) {
                try {
                    time = cmd.getInteger(0);
                } catch (CommandNumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Unknown time \"" + cmd.getString(0) + "\"");
                    return;
                }
            }
            sender.sendMessage(ChatColor.GREEN + "Cycling in " + time + " seconds.");
            TGM.get().getModule(CycleCountdown.class).start(time);
        } else {
            sender.sendMessage(ChatColor.RED + "A match is currently in progress.");
        }
    }

    @Command(aliases = {"start"}, desc = "Start the match.")
    @CommandPermissions({"tgm.start"})
    public static void start(CommandContext cmd, CommandSender sender) {
        MatchStatus matchStatus = TGM.get().getMatchManager().getMatch().getMatchStatus();
        if (matchStatus == MatchStatus.PRE) {
            int time = StartCountdown.START_TIME;
            if (cmd.argsLength() > 0) {
                try {
                    time = cmd.getInteger(0);
                } catch (CommandNumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Unknown time \"" + cmd.getString(0) + "\"");
                    return;
                }
            }
            boolean soloStart = Bukkit.getOnlinePlayers().size() <= 1;
            if (!soloStart)
                sender.sendMessage(ChatColor.GREEN + "Match will start in " + time + " second" + (time == 1 ? "" : "s") + ".");
            TGM.get().getModule(StartCountdown.class).start((soloStart) ? 0 : time);
        } else {
            sender.sendMessage(ChatColor.RED + "The match cannot be started at this time.");
        }
    }

    @Command(aliases = {"end", "finish"}, desc = "End the match.", anyFlags = true, flags = "f")
    @CommandPermissions({"tgm.end"})
    public static void end(CommandContext cmd, CommandSender sender) {
        MatchStatus matchStatus = TGM.get().getMatchManager().getMatch().getMatchStatus();
        if (matchStatus == MatchStatus.MID) {
            if (cmd.argsLength() > 0) {
                MatchTeam matchTeam = TGM.get().getModule(TeamManagerModule.class).getTeamFromInput(cmd.getJoinedStrings(0));
                if (matchTeam == null) {
                    sender.sendMessage(ChatColor.RED + "Unable to find team \"" + cmd.getJoinedStrings(0) + "\"");
                    return;
                }
                sender.sendMessage(ChatColor.GREEN + "Ending match...");
                TGM.get().getMatchManager().endMatch(matchTeam);
            } else {
                sender.sendMessage(ChatColor.GREEN + "Ending match...");
                if (cmd.hasFlag('f')) {
                    TGM.get().getMatchManager().endMatch(null);
                } else {
                    TGM.get().getModule(TimeModule.class).endMatch();
                }
            }
        } else {
            sender.sendMessage(ChatColor.RED + "No match in progress.");
        }
    }

    @Command(aliases = {"cancel"}, desc = "Cancel all countdowns.")
    @CommandPermissions({"tgm.cancel"})
    public static void cancel(CommandContext cmd, CommandSender sender) {
        for (Countdown countdown : TGM.get().getModules(Countdown.class)) {
            countdown.cancel();
        }
        sender.sendMessage(ChatColor.GREEN + "Countdowns cancelled.");
    }

    @Command(aliases = {"setnext", "sn"}, desc = "Set the next map.")
    @CommandPermissions({"tgm.setnext"})
    public static void setNext(CommandContext cmd, CommandSender sender) {
        if (cmd.argsLength() > 0) {
            MapContainer found = null;
            for (MapContainer mapContainer : TGM.get().getMatchManager().getMapLibrary().getMaps()) {
                if (mapContainer.getMapInfo().getName().equalsIgnoreCase(cmd.getJoinedStrings(0))) {
                    found = mapContainer;
                }
            }

            if (found == null) {
                for (MapContainer mapContainer : TGM.get().getMatchManager().getMapLibrary().getMaps()) {
                    if (mapContainer.getMapInfo().getName().toLowerCase().startsWith(cmd.getJoinedStrings(0).toLowerCase())) {
                        found = mapContainer;
                    }
                }
            }

            if (found == null) {
                sender.sendMessage(ChatColor.RED + "Map not found \"" + cmd.getJoinedStrings(0) + "\"");
                return;
            }

            TGM.get().getMatchManager().setForcedNextMap(found);
            sender.sendMessage(ChatColor.GREEN + "Set the next map to " + ChatColor.YELLOW + found.getMapInfo().getName() + ChatColor.GRAY + " (" + found.getMapInfo().getVersion() + ")");
        } else {
            sender.sendMessage(ChatColor.RED + "/sn <map_name>");
        }
    }

    @Command(aliases = {"classes"}, desc = "Class menu.")
    public static void classes(CommandContext cmd, CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatConstant.ERROR_COMMAND_PLAYERS_ONLY.toString());
            return;
        }
        if (TGM.get().getModule(GameClassModule.class) == null) {
            sender.sendMessage(ChatColor.RED + "This map does not use classes.");
            return;
        }

        Player player = (Player) sender;
        ClassMenu.getClassMenu().open(player);
    }

    @SuppressWarnings("unchecked")
    @Command(aliases = {"class"}, desc = "Choose a class.", min = 1, usage = "<kit name>")
    public static void classCommand(CommandContext cmd, CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatConstant.ERROR_COMMAND_PLAYERS_ONLY.toString());
            return;
        }
        if (TGM.get().getModule(GameClassModule.class) == null) {
            sender.sendMessage(ChatColor.RED + "This map does not use classes.");
            return;
        }
        if (TGM.get().getMatchManager().getMatch().getMatchStatus() == MatchStatus.POST) {
            sender.sendMessage(ChatColor.RED + "You cannot change classes at this time!");
            return;
        }

        String chosenClassString = Strings.getTechnicalName(cmd.getString(0));
        GameClassModule gameClassModule = TGM.get().getModule(GameClassModule.class);

        GameClassModule.GameClassStore actualKit = null;
        for (GameClassModule.GameClassStore gameClassStore : GameClassModule.GameClassStore.values()) {
            if (gameClassStore.name().equals(chosenClassString) && gameClassModule.classSetHasInstance(gameClassStore.getHostGameClass())) {
                actualKit = gameClassStore;
                break;
            }
        }
        Player player = (Player) sender;
        if (actualKit == null) {
            player.sendMessage(ChatColor.RED + "Invalid class name! Try /classes!");
            return;
        }

        if (Strings.getTechnicalName(gameClassModule.getCurrentClass(player)).equals(chosenClassString)) {
            player.sendMessage(ChatColor.RED + "You are using this class currently!");
            return;
        }

        if (TGM.get().getMatchManager().getMatch().getMatchStatus() != MatchStatus.MID) {
            gameClassModule.setClassForPlayer(player, chosenClassString);
        } else {
            gameClassModule.addSwitchClassRequest(player, chosenClassString);
        }
    }

    @Command(aliases = {"join", "play"}, desc = "Join a team.")
    public static void join(CommandContext cmd, CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatConstant.ERROR_COMMAND_PLAYERS_ONLY.toString());
            return;
        }
        TeamManagerModule teamManager = TGM.get().getModule(TeamManagerModule.class);
        PlayerContext playerContext = TGM.get().getPlayerManager().getPlayerContext((Player) sender);
        MatchTeam oldTeam = teamManager.getTeam(playerContext.getPlayer());
        MatchTeam team;
        boolean autoJoin = true;
        if (cmd.argsLength() > 0) {
            autoJoin = false;
            team = teamManager.getTeamFromInput(cmd.getRemainingString(0));
            if (team == null) {
                sender.sendMessage(ChatColor.RED + "Unknown team: " + cmd.getRemainingString(0));
                return;
            }
        } else {
            team = teamManager.getSmallestTeam();
        }
        PlayerJoinTeamAttemptEvent event = new PlayerJoinTeamAttemptEvent(playerContext, oldTeam, team, autoJoin, false);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;
        attemptJoinTeam(playerContext.getPlayer(), event.getTeam(), autoJoin);
    }

    @Command(aliases = {"killstreak", "ks"}, max = 1, usage = "[player]", desc = "See your current killstreak")
    public static void killstreak(CommandContext cmd, CommandSender sender) {
        boolean otherPlayer;
        Player player;
        int killstreak;

        if (cmd.argsLength() == 1) {
            otherPlayer = true;
            player = Bukkit.getPlayer(cmd.getString(0));

            if (player == null) {
                sender.sendMessage(ChatColor.RED + "Player not found.");
                return;
            }

            killstreak = TGM.get().getModule(KillstreakModule.class).getKillstreak(player.getUniqueId().toString());

            if (killstreak == 0) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4" + player.getName() + " &cisn't on a kill streak."));
                return;
            }
        } else if (!(sender instanceof Player)) {
            sender.sendMessage(ChatConstant.ERROR_COMMAND_PLAYERS_ONLY.toString());
            return;
        } else {
            otherPlayer = false;
            player = (Player) sender;
            killstreak = TGM.get().getModule(KillstreakModule.class).getKillstreak(player.getUniqueId().toString());
        }

        MatchTeam matchTeam = TGM.get().getModule(TeamManagerModule.class).getTeam(player);

        if (matchTeam != null) {
            if (killstreak < 1 || matchTeam.isSpectator()) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', (otherPlayer ? "&4" + player.getName() + " &cisn't on a kill streak." : "&cYou aren't on a kill streak.")));
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', (otherPlayer ? "&2" + player.getName() + " &ais " : "&aYou're ") + "on a kill streak of &2" + killstreak + "&a kill" + (killstreak == 1 ? "" : "s") + "."));
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Something went wrong. Try again later.");
        }
    }

    @Command(aliases = {"teleport", "tp"}, desc = "Teleport to a player")
    public static void teleport(CommandContext cmd, CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatConstant.ERROR_COMMAND_PLAYERS_ONLY.toString());
            return;
        }
        Player player = (Player) sender;
        MatchTeam matchTeam = TGM.get().getModule(TeamManagerModule.class).getTeam(player);
        MatchStatus matchStatus = TGM.get().getMatchManager().getMatch().getMatchStatus();
        if (matchTeam != null) {
            if (matchTeam.isSpectator() || matchStatus == MatchStatus.POST || player.hasPermission("tgm.teleport")) { // allow staff to tp outside of spectator
                if (cmd.argsLength() == 1) {
                    Player tpTo = Bukkit.getPlayer(cmd.getString(0));
                    if (tpTo == null) {
                        player.sendMessage(ChatColor.RED + "Player not found");
                        return;
                    }
                    player.teleport(tpTo.getLocation());
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                    return;
                }
                player.sendMessage(ChatColor.RED + "Usage: /tp <name>");
            } else {
                player.sendMessage(ChatColor.RED + "You can only execute this command as a spectator!");
            }
        } else {
            player.sendMessage(ChatColor.RED + "Something went wrong. Try again later.");
        }
    }

    @Command(aliases = {"team"}, desc = "Manage teams.")
    @CommandPermissions({"tgm.team"})
    public static void team(CommandContext cmd, CommandSender sender) {
        if (cmd.argsLength() > 0) {
            if (cmd.getString(0).equalsIgnoreCase("alias")) {
                if (cmd.argsLength() == 3) {
                    MatchTeam matchTeam = TGM.get().getModule(TeamManagerModule.class).getTeamFromInput(cmd.getString(1));
                    if (matchTeam == null) {
                        sender.sendMessage(ChatColor.RED + "Unknown team \"" + cmd.getString(1) + "\"");
                        return;
                    }
                    sender.sendMessage(ChatColor.GREEN + "Renamed " + matchTeam.getColor() + matchTeam.getAlias() + ChatColor.GREEN + " to " + matchTeam.getColor() + cmd.getString(2));
                    matchTeam.setAlias(cmd.getString(2));
                    Bukkit.getPluginManager().callEvent(new TeamUpdateEvent(matchTeam));
                } else {
                    sender.sendMessage(ChatColor.RED + "/team alias (team) (name)");
                }
            } else if (cmd.getString(0).equalsIgnoreCase("force")) {
                if (cmd.argsLength() == 3) {
                    MatchTeam matchTeam = TGM.get().getModule(TeamManagerModule.class).getTeamFromInput(cmd.getString(2));
                    if (matchTeam == null) {
                        sender.sendMessage(ChatColor.RED + "Unknown team \"" + cmd.getString(2) + "\"");
                        return;
                    }
                    Player player = Bukkit.getPlayer(cmd.getString(1));
                    if (player == null) {
                        sender.sendMessage(ChatColor.RED + "Unknown player \"" + cmd.getString(1) + "\"");
                        return;
                    }
                    attemptJoinTeam(player, matchTeam, true, true);
                    sender.sendMessage(ChatColor.GREEN + "Forced " + player.getName() + " into " + matchTeam.getColor() + matchTeam.getAlias());
                } else {
                    sender.sendMessage(ChatColor.RED + "/team force (player) (team)");
                }
            } else if (cmd.getString(0).equalsIgnoreCase("size")) {
                if (cmd.argsLength() == 4) {
                    MatchTeam matchTeam = TGM.get().getModule(TeamManagerModule.class).getTeamFromInput(cmd.getString(1));
                    if (matchTeam == null) {
                        sender.sendMessage(ChatColor.RED + "Unknown team \"" + cmd.getString(1) + "\"");
                        return;
                    }
                    int min = 0;
                    int max = 0;
                    try {
                        min = cmd.getInteger(2);
                        max = cmd.getInteger(3);
                    } catch (CommandNumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + "Error: " + e.getMessage());
                        return;
                    }
                    matchTeam.setMin(min);
                    matchTeam.setMax(max);
                    Bukkit.getPluginManager().callEvent(new TeamUpdateEvent(matchTeam));
                    sender.sendMessage(ChatColor.GREEN + "Set " + matchTeam.getColor() + matchTeam.getAlias() + ChatColor.GREEN + " size limits to " + min + "-" + max);
                } else {
                    sender.sendMessage(ChatColor.RED + "/team size (team) (min) (max)");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "/team alias|force|size");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "/team alias|force|size");
        }
    }

    @Command(aliases = {"loadmaps"}, desc = "Load maps.")
    @CommandPermissions({"tgm.loadmaps"})
    public static void loadmaps(CommandContext cmd, CommandSender sender) {
        TGM.get().getMatchManager().getMapLibrary().refreshMaps();
        TGM.get().getMatchManager().getMapRotation().refresh();
        sender.sendMessage(ChatColor.GREEN + "Refreshed map library and rotation.");
    }

    @Command(aliases = {"channel", "chatchannel", "cc"}, desc = "Change or select a chat channel.", usage = "(all|team|staff)", min = 1)
    public static void channel(CommandContext cmd, CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatConstant.ERROR_COMMAND_PLAYERS_ONLY.toString());
            return;
        }
        Player player = (Player) sender;
        if (cmd.argsLength() == 0) {
            player.sendMessage(ColorConverter.filterString("&cUsage: /channel (name)"));
            return;
        }

        String channelName = cmd.getString(0).toUpperCase();
        ChatChannel channel = ChatChannel.byName(channelName);
        if (channel == null) {
            player.sendMessage(ColorConverter.filterString("&cInvalid channel: " + channelName));
            player.sendMessage(ColorConverter.filterString("&cChannels: ( " + StringUtils.join(Arrays.stream(ChatChannel.values()).filter(ch -> ch.hasPermission(player)).collect(Collectors.toList()), " | ")) + " )");
            return;
        }

        if (!channel.hasPermission(player)) {
            player.sendMessage(ColorConverter.filterString("&cError: Insufficient permissions."));
            return;
        }

        ChatModule.getChannels().put(player.getUniqueId().toString(), channel);
        player.sendMessage(ColorConverter.filterString("&7You've been added to the channel &c&l" + channel.name() + "&7."));
    }

    @Command(aliases = {"t"}, desc = "Send a message to your team.", usage = "(message)", min = 1)
    public static void t(CommandContext cmd, CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatConstant.ERROR_COMMAND_PLAYERS_ONLY.toString());
            return;
        }
        if (cmd.argsLength() > 0) {
            PlayerContext playerContext = TGM.get().getPlayerManager().getPlayerContext((Player) sender);
            TGM.get().getModule(ChatModule.class).sendTeamChat(playerContext, cmd.getJoinedStrings(0));
        }
    }

    @Command(aliases = {"next"}, desc = "View the next map in the rotation")
    public static void next(CommandContext cmd, CommandSender sender) {
        MapInfo info = TGM.get().getMatchManager().getNextMap().getMapInfo();
        sender.sendMessage(ChatColor.GRAY + "Next Map: " + ChatColor.YELLOW + info.getName() + ChatColor.GRAY + " by " + ChatColor.YELLOW + String.join(", ", info.getAuthors().stream().map(Strings::getAuthorUsername).collect(Collectors.joining(", "))));
    }

    @Command(aliases = {"map"}, desc = "View the map info for the current map")
    public static void map(CommandContext cmd, CommandSender sender) {
        MapInfo info = TGM.get().getMatchManager().getMatch().getMapContainer().getMapInfo();
        sender.sendMessage(ChatColor.GRAY + "Currently playing " + ChatColor.YELLOW + info.getGametype() + ChatColor.GRAY + " on map " + ChatColor.YELLOW + info.getName() + ChatColor.GRAY + " by " + ChatColor.YELLOW + info.getAuthors().stream().map(Strings::getAuthorUsername).collect(Collectors.joining(", ")));
    }

    @Command(aliases = {"time"}, desc = "Time options")
    public static void time(CommandContext cmd, CommandSender sender) {
        if (cmd.argsLength() <= 0) {
            ChatColor timeColor = ChatColor.GREEN;
            MatchStatus matchStatus = TGM.get().getMatchManager().getMatch().getMatchStatus();
            if (matchStatus == MatchStatus.PRE) {
                timeColor = ChatColor.GOLD;
            } else if (matchStatus == MatchStatus.POST) {
                timeColor = ChatColor.RED;
            }
            sender.sendMessage(ChatColor.AQUA + "Time elapsed: " + timeColor + Strings.formatTime(TGM.get().getModule(TimeModule.class).getTimeElapsed()));
            return;
        }
        if (cmd.getString(0).equalsIgnoreCase("limit")) {
            if (!sender.hasPermission("tgm.time.limit")) {
                sender.sendMessage(ChatColor.RED + "Insufficient permissions.");
                return;
            }
            if (cmd.argsLength() == 1 || cmd.argsLength() > 2) {
                sender.sendMessage(ChatColor.RED + "/" + cmd.getCommand() + " limit <seconds>");
                return;
            }

            TimeModule timeModule = TGM.get().getModule(TimeModule.class);
            if (cmd.getString(1).equalsIgnoreCase("on") || cmd.getString(1).equalsIgnoreCase("true")) {
                timeModule.setTimeLimited(true);
                sender.sendMessage(ChatColor.AQUA + "Time limit: " + ChatColor.GREEN + "true");
                return;
            } else if (cmd.getString(1).equalsIgnoreCase("off") || cmd.getString(1).equalsIgnoreCase("false")) {
                timeModule.setTimeLimited(false);
                sender.sendMessage(ChatColor.AQUA + "Time limit: " + ChatColor.RED + "false");
                return;
            }

            try {
                timeModule.setTimeLimit(cmd.getInteger(1));
                timeModule.setTimeLimited(true);
                sender.sendMessage(ChatColor.AQUA + "Set time limit to: " + ChatColor.GREEN + timeModule.getTimeLimit() + " seconds");
            } catch (CommandNumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "/" + cmd.getCommand() + " limit <seconds>");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "/" + cmd.getCommand() + " limit <seconds>");
        }
    }

    @Command(aliases = {"config"}, desc = "Edit the configuration", usage = "(stats/reload)", min = 1)
    @CommandPermissions({"tgm.config"})
    public static void config(CommandContext cmd, CommandSender sender) {
        if (cmd.getString(0).equalsIgnoreCase("stats")) {
            if (cmd.argsLength() != 2) {
                sender.sendMessage(ChatColor.WHITE + "Stat uploading is set to \"" + TGM.get().getConfig().getBoolean("api.stats.enabled") + "\"");
                return;
            }
            if (cmd.getString(1).equalsIgnoreCase("off")) {
                TGM.get().getConfig().set("api.stats.enabled", false);
                TGM.get().saveConfig();

                sender.sendMessage(ChatColor.GREEN + "Disabled stat uploading.");
            } else if (cmd.getString(1).equalsIgnoreCase("on")) {
                TGM.get().getConfig().set("api.stats.enabled", true);
                TGM.get().saveConfig();

                sender.sendMessage(ChatColor.GREEN + "Enabled stat uploading.");
            } else {
                sender.sendMessage(ChatColor.RED + "Unknown value \"" + cmd.getString(0) + "\". Please specify [on/off]");
            }
        } else if (cmd.getString(0).equalsIgnoreCase("reload")) {
            TGM.get().reloadConfig();
            sender.sendMessage(ChatColor.GREEN + "Reloaded configuration!");
            Bukkit.getPluginManager().callEvent(new TGMConfigReloadEvent());
        }
    }

    @Command(aliases = {"leaderboard", "lb", "lboard"}, usage = "(type)", min = 1, max = 1, desc = "List the top 10 players on the server")
    public static void leaderboard(CommandContext cmd, CommandSender sender) {
        if (!TGM.get().getConfig().getBoolean("api.stats.enabled") || !TGM.get().getConfig().getBoolean("api.enabled")) {
            sender.sendMessage(ChatColor.RED + "Stat tracking is disabled");
        } else {
            LeaderboardCriterion criterion;
            try {
                criterion = LeaderboardCriterion.valueOf(Strings.getTechnicalName(cmd.getString(0)));
            } catch (IllegalArgumentException e) {
                List<String> criteria = Arrays.stream(LeaderboardCriterion.values())
                        .map(c -> c.name().toLowerCase())
                        .collect(Collectors.toList());
                sender.sendMessage(ChatColor.RED + "Usage: /" + cmd.getCommand() + " <" + String.join("|", criteria) + ">");
                return;
            }
            Bukkit.getScheduler().runTaskAsynchronously(TGM.get(), () -> {
                int place = 0;
                sender.sendMessage(ChatColor.DARK_AQUA + "Top 10 players (" + criterion.getDisplay() + ")");
                for (UserProfile player : TGM.get().getTeamClient().getLeaderboard(criterion)) {
                    sender.sendMessage(profileToTextComponent(player, ++place, criterion).getText());
                }
            });
        }
    }

    @Command(aliases = {"stats", "stat"}, desc = "View your stats.")
    public static void stats(final CommandContext cmd, CommandSender sender) {
        if (cmd.argsLength() == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Consoles don't have stats.");
                return;
            }
            viewStats(sender, sender.getName());
        } else {
            viewStats(sender, cmd.getString(0));
        }
    }

    @Command(aliases = {"countdown", "cd"}, desc = "Manage custom countdowns", usage = "<list|start|create|edit|cancel>", min = 1)
    @CommandPermissions({"tgm.countdown"})
    public static void countdown(CommandContext cmd, CommandSender sender) throws CommandNumberFormatException {
        CountdownManagerModule countdownManagerModule = TGM.get().getModule(CountdownManagerModule.class);
        if (cmd.getString(0).equalsIgnoreCase("list")) {
            Map<String, CustomCountdown> countdowns = countdownManagerModule.getCustomCountdowns();
            if (countdowns.size() > 0) {
                sender.sendMessage(ChatColor.GREEN + "Registered countdowns:");
                countdowns.forEach((id, cd) -> sender.sendMessage(" - " + id));
            } else {
                sender.sendMessage(ChatColor.RED + "There are no registered countdowns.");
            }
        }
        else if (cmd.getString(0).equalsIgnoreCase("start")) {
            if (cmd.argsLength() <= 1) {
                sender.sendMessage(ChatColor.RED + "Usage: /" + cmd.getCommand() + " start (id)");
                return;
            }
            CustomCountdown countdown = countdownManagerModule.getCountdown(cmd.getString(1));
            if (countdown == null) {
                sender.sendMessage(ChatColor.RED + "Unknown countdown.");
                return;
            }
            if (cmd.argsLength() <= 2) countdown.start();
            else countdown.start(cmd.getInteger(2));
            if (sender instanceof Player) sender.sendMessage(ChatColor.GREEN + "Countdown started.");
        } else if (cmd.getString(0).equalsIgnoreCase("create")) {
            if (cmd.argsLength() <= 3) {
                sender.sendMessage(ChatColor.RED + "Usage: /" + cmd.getCommand() + " create <id> <time> <title> [color] [style] [visible] [invert] [teams] [onFinish]");
                return;
            }
            String id = cmd.getString(1);
            int time = cmd.getInteger(2);
            String title = cmd.getString(3);
            BarColor color = cmd.argsLength() > 4 ? BarColor.valueOf(Strings.getTechnicalName(cmd.getString(4))) : BarColor.PURPLE;
            BarStyle style = cmd.argsLength() > 5 ? BarStyle.valueOf(Strings.getTechnicalName(cmd.getString(5))) : BarStyle.SOLID;
            boolean visible = cmd.argsLength() <= 6 || Boolean.parseBoolean(cmd.getString(6));
            boolean invert = cmd.argsLength() > 7 && Boolean.parseBoolean(cmd.getString(7));
            List<MatchTeam> teams = cmd.argsLength() > 8 ?
                    Arrays.stream(cmd.getString(8).split(",")).map(t -> TGM.get().getModule(TeamManagerModule.class).getTeamById(t)).collect(Collectors.toList()) :
                    new ArrayList<>();
            List<String> onFinish = cmd.argsLength() > 9 ?
                    Arrays.asList(cmd.getString(9).split(",")) :
                    new ArrayList<>();
            countdownManagerModule.addCountdown(id, new CustomCountdown(time, title, color, style, visible, invert, teams, onFinish));
            sender.sendMessage(ChatColor.GREEN + "Created new countdown.");
        } else if (cmd.getString(0).equalsIgnoreCase("edit")) {
            if (cmd.argsLength() < 3) {
                sender.sendMessage(ChatColor.RED + "Usage: /" + cmd.getCommand() + " edit <id> <time|title|color|style|visible|invert|teams|onFinish> <value>");
                return;
            }
            String id = cmd.getString(1);
            CustomCountdown countdown = countdownManagerModule.getCountdown(id);
            switch (cmd.getString(2)) {
                case "time":
                    countdown.setTime(cmd.getInteger(3));
                    break;
                case "title":
                    countdown.setTitle(cmd.getRemainingString(3));
                    break;
                case "color":
                    countdown.setColor(BarColor.valueOf(Strings.getTechnicalName(cmd.getRemainingString(3))));
                    break;
                case "style":
                    countdown.setStyle(BarStyle.valueOf(Strings.getTechnicalName(cmd.getRemainingString(3))));
                    break;
                case "visible":
                    countdown.setVisible(Boolean.parseBoolean(cmd.getString(3)));
                    break;
                case "invert":
                    countdown.setInvert(Boolean.parseBoolean(cmd.getString(3)));
                    break;
                case "teams":
                    if (cmd.getRemainingString(3).equalsIgnoreCase("-")) {
                        countdown.setTeams(new ArrayList<>());
                        break;
                    }
                    TeamManagerModule teamManagerModule = TGM.get().getModule(TeamManagerModule.class);
                    countdown.setTeams(Arrays.stream(cmd.getRemainingString(3).split(";")).map(teamManagerModule::getTeamFromInput).collect(Collectors.toList()));
                    break;
                case "onFinish":
                    if (cmd.getRemainingString(3).equalsIgnoreCase("-")) {
                        countdown.setOnFinish(new ArrayList<>());
                        break;
                    }
                    countdown.setOnFinish(Arrays.asList(cmd.getRemainingString(3).split(";")));
                    break;
                default:
                    sender.sendMessage(ChatColor.RED + "Usage: /" + cmd.getCommand() + " edit <id> <time|title|color|style|visible|invert|teams|onFinish> [value]");
                    return;
            }
            sender.sendMessage(ChatColor.GREEN + "Updated countdown.");
        } else if (cmd.getString(0).equalsIgnoreCase("cancel")) {
            if (cmd.argsLength() < 2) {
                sender.sendMessage(ChatColor.RED + "Usage: /" + cmd.getCommand() + " cancel <id>");
                return;
            }
            String id = cmd.getString(1);
            CustomCountdown countdown = countdownManagerModule.getCountdown(id);
            if (countdown == null) {
                sender.sendMessage(ChatColor.RED + "Countdown not found!");
                return;
            }
            countdown.cancel();
            sender.sendMessage(ChatColor.YELLOW + "Countdown cancelled.");
        } else {
            sender.sendMessage(ChatColor.RED + "Usage: /" + cmd.getCommand() + " <list|start|create|edit|cancel>");
        }
    }

    private static void viewStats(CommandSender sender, String target) {
        Player targetPlayer = Bukkit.getServer().getPlayer(target);
        if (targetPlayer == null) {
            Bukkit.getScheduler().runTaskAsynchronously(TGM.get(), () -> {
                GetPlayerByNameResponse response = TGM.get().getTeamClient().player(target);
                if (response == null || response.getUser() == null) {
                    sender.sendMessage(ChatColor.RED + "Player not found!");
                    return;
                }
                UserProfile up = response.getUser();
                sender.sendMessage(ChatColor.BLUE + ChatColor.STRIKETHROUGH.toString() + "-------------------------------");
                sender.sendMessage(ChatColor.DARK_AQUA + "   Viewing stats for " + ChatColor.AQUA + up.getName());
                sender.sendMessage("");
                sender.sendMessage(ChatColor.DARK_AQUA + "   Level: " + up.getLevel());
                sender.sendMessage(ChatColor.DARK_AQUA + "   XP: " + ChatColor.AQUA + up.getXP() + "/" + ChatColor.DARK_AQUA + UserProfile.getRequiredXP(up.getLevel() + 1) + " (approx.)");
                sender.sendMessage("");
                sender.sendMessage(ChatColor.DARK_AQUA + "   Kills: " + ChatColor.GREEN + up.getKills());
                sender.sendMessage(ChatColor.DARK_AQUA + "   Deaths: " + ChatColor.RED + up.getDeaths());
                sender.sendMessage(ChatColor.DARK_AQUA + "   K/D: " + ChatColor.AQUA + up.getKDR());
                sender.sendMessage("");
                sender.sendMessage(ChatColor.DARK_AQUA + "   Wins: " + ChatColor.GREEN + up.getWins());
                sender.sendMessage(ChatColor.DARK_AQUA + "   Losses: " + ChatColor.RED + up.getLosses());
                sender.sendMessage(ChatColor.DARK_AQUA + "   W/L: " + ChatColor.AQUA + up.getWLR());
                sender.sendMessage(ChatColor.BLUE + ChatColor.STRIKETHROUGH.toString() + "-------------------------------");
            });
            return;
        }
        PlayerContext targetUser = TGM.get().getPlayerManager().getPlayerContext(targetPlayer);
        boolean getNickedStats = !target.equalsIgnoreCase(targetUser.getOriginalName()) && targetUser.isNicked();
        UserProfile profile = getNickedStats ? targetUser.getUserProfile() : targetUser.getUserProfile(true);
        String levelString = getNickedStats ? targetUser.getLevelString() : targetUser.getLevelString(true);
        sender.sendMessage(ChatColor.BLUE + ChatColor.STRIKETHROUGH.toString() + "-------------------------------");
        sender.sendMessage(ChatColor.DARK_AQUA + "   Viewing stats for " + ChatColor.AQUA + (target.equalsIgnoreCase(targetUser.getOriginalName()) ? targetUser.getOriginalName() : targetUser.getDisplayName()));
        sender.sendMessage("");
        sender.sendMessage(ChatColor.DARK_AQUA + "   Level: " + levelString.replace("[", "").replace("]", ""));
        sender.sendMessage(ChatColor.DARK_AQUA + "   XP: " + ChatColor.AQUA + profile.getXP() + "/" + ChatColor.DARK_AQUA + UserProfile.getRequiredXP(profile.getLevel() + 1) + " (approx.)");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.DARK_AQUA + "   Kills: " + ChatColor.GREEN + profile.getKills());
        sender.sendMessage(ChatColor.DARK_AQUA + "   Deaths: " + ChatColor.RED + profile.getDeaths());
        sender.sendMessage(ChatColor.DARK_AQUA + "   K/D: " + ChatColor.AQUA + profile.getKDR());
        sender.sendMessage("");
        sender.sendMessage(ChatColor.DARK_AQUA + "   Wins: " + ChatColor.GREEN + profile.getWins());
        sender.sendMessage(ChatColor.DARK_AQUA + "   Losses: " + ChatColor.RED + profile.getLosses());
        sender.sendMessage(ChatColor.DARK_AQUA + "   W/L: " + ChatColor.AQUA + profile.getWLR());
        sender.sendMessage(ChatColor.BLUE + ChatColor.STRIKETHROUGH.toString() + "-------------------------------");
    }


    public static void attemptJoinTeam(Player player, MatchTeam matchTeam, boolean autoJoin) {
        attemptJoinTeam(player, matchTeam, autoJoin, false);
    }

    public static void attemptJoinTeam(Player player, MatchTeam matchTeam, boolean autoJoin, boolean ignoreFull) {
        TeamManagerModule teamManager = TGM.get().getModule(TeamManagerModule.class);
        MatchTeam currentTeam = teamManager.getTeam(player);
        if (!ignoreFull && !currentTeam.isSpectator() && !matchTeam.isSpectator()) {
            player.sendMessage(ChatColor.RED + "You are already in a team.");
            return;
        }
        if (matchTeam.getMembers().size() >= matchTeam.getMax() && !ignoreFull) {
            player.sendMessage(ChatColor.RED + "Team is full! Wait for a spot to open up.");
            return;
        }

        if (!autoJoin) {
            if (!player.hasPermission("tgm.pickteam")) {
                player.sendMessage(ChatColor.LIGHT_PURPLE + "Only premium users can pick their team!\nPurchase a rank at " + TGM.get().getConfig().getString("server.store"));
                return;
            } else if (!player.hasPermission("tgm.pickteam.bypass") &&
                    (!TGM.get().getApiManager().isStatsDisabled() || !TGM.get().getConfig().getBoolean("map.team-picking-conditions.ignore-untracked"))) {
                if (getPlayerCount() < TGM.get().getConfig().getInt("map.team-picking-conditions.min-players")) {
                    player.sendMessage(ChatColor.RED + "There are not enough players in the server for you to be able to pick your team.");
                    return;
                } else if (!enoughPlayers(TGM.get().getConfig().getInt("map.team-picking-conditions.min-playing"))) {
                    player.sendMessage(ChatColor.RED + "There are not enough players already playing for you to be able to pick your team.");
                    return;
                }
            }
        }

        PlayerContext playerContext = TGM.get().getPlayerManager().getPlayerContext(player);
        teamManager.joinTeam(playerContext, matchTeam, ignoreFull, false);
    }

    public static TextComponent profileToTextComponent(UserProfile profile, int place, LeaderboardCriterion criterion) {
        TextComponent main = new TextComponent(
                ChatColor.translateAlternateColorCodes('&', "&7" + place + "." + " &b" +
                        profile.getName() + " &7(&9" + criterion.extract(profile) + " " +
                        criterion.getDisplay() + "&7)"
                )
        );
        main.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{
                new TextComponent(ChatColor.AQUA + "Level: " + ChatColor.RESET + profile.getLevel()),
                new TextComponent("\n"),
                new TextComponent("\n" + ChatColor.AQUA + "XP: " + ChatColor.RESET + profile.getXP()),
                new TextComponent("\n" + ChatColor.AQUA + "Kills: " + ChatColor.RESET + profile.getKills()),
                new TextComponent("\n" + ChatColor.AQUA + "Deaths: " + ChatColor.RESET + profile.getDeaths()),
                new TextComponent("\n" + ChatColor.AQUA + "K/D: " + ChatColor.RESET + profile.getKDR()),
                new TextComponent("\n"),
                new TextComponent("\n" + ChatColor.AQUA + "Wins: " + ChatColor.RESET + profile.getWins()),
                new TextComponent("\n" + ChatColor.AQUA + "Losses: " + ChatColor.RESET + profile.getLosses()),
                new TextComponent("\n" + ChatColor.AQUA + "W/L: " + ChatColor.RESET + profile.getWLR())
        }));
        main.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/stats " + profile.getName()));
        return main;
    }

    public static TextComponent mapToTextComponent(int position, MapInfo mapInfo) {
        String mapName = ChatColor.GOLD + mapInfo.getName();

        if (mapInfo.equals(TGM.get().getMatchManager().getMatch().getMapContainer().getMapInfo())) {
            mapName = ChatColor.GREEN + "" + (position + 1) + ". " + mapName;
        } else {
            mapName = ChatColor.WHITE + "" + (position + 1) + ". " + mapName;
        }
        TextComponent message = new TextComponent(mapName);
        message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/setnext " + mapInfo.getName()));
        message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GOLD + mapInfo.getName()).append("\n\n")
                .append(ChatColor.GRAY + "Authors: ").append(ChatColor.YELLOW + mapInfo.getAuthors().stream().map(Strings::getAuthorUsername).collect(Collectors.joining(", "))).append("\n")
                .append(ChatColor.GRAY + "Game Type: ").append(ChatColor.YELLOW + mapInfo.getGametype().toString()).append("\n")
                .append(ChatColor.GRAY + "Version: ").append(ChatColor.YELLOW + mapInfo.getVersion()).create()));
        return message;
    }

    private static int getPlayerCount() {
        return Bukkit.getOnlinePlayers().size();
    }

    private static boolean enoughPlayers(int min) {
        for (MatchTeam team : TGM.get().getModule(TeamManagerModule.class).getTeams()) {
            if (team.isSpectator()) continue;
            if (team.getMembers().size() < min) return false;
        }
        return true;
    }
}
