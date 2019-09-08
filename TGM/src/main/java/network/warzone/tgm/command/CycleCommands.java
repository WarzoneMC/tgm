package network.warzone.tgm.command;

import com.sk89q.minecraft.util.commands.*;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import network.warzone.tgm.TGM;
import network.warzone.tgm.gametype.GameType;
import network.warzone.tgm.map.MapContainer;
import network.warzone.tgm.map.MapInfo;
import network.warzone.tgm.match.MatchManager;
import network.warzone.tgm.match.MatchStatus;
import network.warzone.tgm.modules.ChatModule;
import network.warzone.tgm.modules.countdown.Countdown;
import network.warzone.tgm.modules.countdown.CycleCountdown;
import network.warzone.tgm.modules.countdown.StartCountdown;
import network.warzone.tgm.modules.ffa.FFAModule;
import network.warzone.tgm.modules.killstreak.KillstreakModule;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.modules.team.TeamUpdateEvent;
import network.warzone.tgm.modules.time.TimeModule;
import network.warzone.tgm.user.PlayerContext;
import network.warzone.tgm.util.ColorConverter;
import network.warzone.tgm.util.Strings;
import network.warzone.warzoneapi.models.GetPlayerByNameResponse;
import network.warzone.warzoneapi.models.UserProfile;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CycleCommands {

    @Command(aliases = {"maps"}, desc = "View the maps that are on Warzone, although not necessarily in the rotation.", usage = "[type]? [page]")
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
             }
             else if (cmd.argsLength() == 2) {
                 typeString = cmd.getString(0);
                 index = cmd.getInteger(1);
             }
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Number expected.");
            return;
        }

        GameType type = null;
        for (GameType gameType : GameType.values()) {
            if (gameType.name().toLowerCase().equals(typeString.toLowerCase())) {
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
                int position = 9 * (index - 1) + i;
                MapContainer map = mapLibrary.get(position);
                TextComponent message = mapToTextComponent(position, map.getMapInfo());
                sender.spigot().sendMessage(message);
            }
        } catch (IndexOutOfBoundsException ignored) {}
    }

    @Command(aliases = {"findmaps"}, desc = "Find the maps that are on Warzone, although not necessarily in the rotation.", min = 1, usage = "<map name> [page]")
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
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&cNo maps with the name &4" + cmd.getString(0) + "&c were found."));
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
        } catch (IndexOutOfBoundsException ignored) {}
    }

    @Command(aliases = {"rot", "rotation", "rotations"}, desc = "View the maps that are in the rotation.", usage = "[page]")
    public static void rotation(final CommandContext cmd, CommandSender sender) throws CommandException {
        int index = cmd.argsLength() == 0 ? 1 : cmd.getInteger(0);
        List<MapContainer> rotation = TGM.get().getMatchManager().getMapRotation().getMaps();

        int pageSize = 9;

        int pagesRemainder = rotation.size() % pageSize;
        int pagesDivisible = rotation.size() / pageSize;
        int pages = pagesDivisible;

        if (pagesRemainder >= 1) {
            pages = pagesDivisible + 1;
        }

        if ((index > pages) || (index <= 0)) {
            index = 1;
        }

        sender.sendMessage(ChatColor.YELLOW + "Active Rotation (" + index + "/" + pages + "): ");
        try {
            for (int i = 0; i < pageSize; i++) {
                int position = 9 * (index - 1) + i;
                MapContainer map = rotation.get(position);
                TextComponent message = mapToTextComponent(position, map.getMapInfo());
                sender.spigot().sendMessage(message);
            }
        } catch (IndexOutOfBoundsException ignored) { }
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
                }
            }
            sender.sendMessage(ChatColor.GREEN + "Match will start in " + time + " second" + (time == 1 ? "" : "s") + ".");
            TGM.get().getModule(StartCountdown.class).start(time);
        } else {
            sender.sendMessage(ChatColor.RED + "The match cannot be started at this time.");
        }
    }

    @Command(aliases = {"end"}, desc = "End the match.", anyFlags = true, flags = "f")
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
    
    @Command(aliases = {"join", "play"}, desc = "Join a team.")
    public static void join(CommandContext cmd, CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You must be a player to do that.");
            return;
        }
        TeamManagerModule teamManager = TGM.get().getModule(TeamManagerModule.class);
        MatchManager matchManager = TGM.get().getMatchManager();
        GameType gameType = matchManager.getMatch().getMapContainer().getMapInfo().getGametype();
        MatchStatus matchStatus = matchManager.getMatch().getMatchStatus();
        if (cmd.argsLength() == 0) {
            if (gameType.equals(GameType.Blitz) || gameType.equals(GameType.FFA) && TGM.get().getModule(FFAModule.class).isBlitzMode()) {
                if (!matchStatus.equals(MatchStatus.PRE)) {
                    sender.sendMessage(ChatColor.RED + "You can't pick a team after the match starts in this gamemode.");
                    return;
                }
            }
            if (teamManager.getTeam((Player) sender).isSpectator() || matchStatus.equals(MatchStatus.PRE)) {
                if (gameType.equals(GameType.Infected)) {
                    if (matchStatus.equals(MatchStatus.MID) || matchStatus.equals(MatchStatus.POST)) {
                        MatchTeam team = teamManager.getTeamById("infected");
                        attemptJoinTeam((Player) sender, team, true);
                        return;
                    }

                    MatchTeam team = teamManager.getTeamById("humans");
                    attemptJoinTeam((Player) sender, team, true);
                    return;
                }
                MatchTeam matchTeam = teamManager.getSmallestTeam();
                attemptJoinTeam((Player) sender, matchTeam, true);
            } else {
                sender.sendMessage(ChatColor.RED + "You have already chosen a team.");
            }
        } else {
            MatchTeam matchTeam = teamManager.getTeamFromInput(cmd.getJoinedStrings(0));

            if (matchTeam == null) {
                sender.sendMessage(ChatColor.RED + "Unable to find team \"" + cmd.getJoinedStrings(0) + "\"");
                return;
            }

            if (gameType.equals(GameType.Infected)) {
                if (matchStatus.equals(MatchStatus.POST)) {
                    if (!matchTeam.isSpectator()) {
                        sender.sendMessage(ChatColor.RED + "The game has already ended.");
                        return;
                    } else {
                        attemptJoinTeam((Player) sender, matchTeam, false);
                        return;
                    }
                } else if (matchStatus.equals(MatchStatus.MID)) {
                    if (!matchTeam.isSpectator()) {
                        sender.sendMessage(ChatColor.RED + "You can't pick a team after the match starts in this gamemode.");
                        return;
                    } else {
                        attemptJoinTeam((Player) sender, matchTeam, false);
                        return;
                    }
                }
            } else if (gameType.equals(GameType.Blitz) || gameType.equals(GameType.FFA) && TGM.get().getModule(FFAModule.class).isBlitzMode()) {
                if (!matchStatus.equals(MatchStatus.PRE)) {
                    sender.sendMessage(ChatColor.RED + "You can't pick a team after the match starts in this gamemode.");
                    return;
                }
            }

            attemptJoinTeam((Player) sender, matchTeam, false);
        }
    }

    @Command(aliases = {"killstreak", "ks"}, desc = "See your current killstreak")
    public static void killstreak(CommandContext cmd, CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command");
            return;
        }
        Player player = (Player) sender;
        String playerUUID = player.getUniqueId().toString();

        MatchTeam matchTeam = TGM.get().getModule(TeamManagerModule.class).getTeam(player);
        int killstreak = TGM.get().getModule(KillstreakModule.class).getKillstreak(playerUUID);

        if (matchTeam != null) {
            if (killstreak == 0 || matchTeam.isSpectator()) {
                sender.sendMessage(ChatColor.RED + "You aren't on a killstreak.");
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aYou're on a kill streak of &2" + killstreak + "&a kill" + (killstreak == 1 ? "" : "s") + "."));
            }
        } else {
            player.sendMessage(ChatColor.RED + "Something went wrong. Try again later.");
        }
    }

    @Command(aliases = {"teleport", "tp"}, desc = "Teleport to a player")
    public static void teleport(CommandContext cmd, CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
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
        if(!(sender instanceof Player)) {
            sender.sendMessage("Error: Only players can use this command.");
            return;
        }
        Player player = (Player) sender;
        if (cmd.argsLength() == 0) {
          player.sendMessage(ColorConverter.filterString("&cUsage: /channel (name)"));
          return;
        }

        String channelName = cmd.getString(0).toUpperCase();
        ChatModule.Channel channel = ChatModule.Channel.byName(channelName);
        if (channel == null) {
          player.sendMessage(ColorConverter.filterString("&cInvalid channel: " + channelName));
          player.sendMessage(ColorConverter.filterString("&cChannels: ( " + StringUtils.join(Arrays.stream(ChatModule.Channel.values()).filter(ch -> ch.hasPermission(player)).collect(Collectors.toList()), " | ")) + " )");
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
            sender.sendMessage(ChatColor.RED + "You must be a player to do that.");
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

    @Command(aliases = {"config"}, desc = "Edit the configuration", usage = "(stats)", min = 1)
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
        }
    }

    @Command(aliases = {"leaderboard", "lb", "lboard"}, usage="(kills)", min = 1, max = 1, desc = "List the top 10 players on the server")
    public static void leaderboard(CommandContext cmd, CommandSender sender) {
        if (!TGM.get().getConfig().getBoolean("api.stats.enabled") || !TGM.get().getConfig().getBoolean("api.enabled")) {
            sender.sendMessage(ChatColor.RED + "Stat tracking is disabled");
        } else {
            if (!cmd.getString(0).equalsIgnoreCase("kills"))  {
                sender.sendMessage(ChatColor.RED + "Invalid stat: " + cmd.getString(0));
            } else if (cmd.getString(0).equalsIgnoreCase("kills")) {
                Bukkit.getScheduler().runTaskAsynchronously(TGM.get(), () -> {
                    int place = 0;
                    sender.sendMessage(ChatColor.DARK_AQUA + "Top 10 players (kills)");
                    for (UserProfile player : TGM.get().getTeamClient().getKillsLeaderboard()) {
                        sender.sendMessage(profileToTextComponent(player, ++place).getText());
                    }
                });
            }
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

    public static void viewStats(CommandSender sender, String target) {
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
                sender.sendMessage(ChatColor.DARK_AQUA + "   Viewing stats for " +  ChatColor.AQUA + up.getName());
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
        sender.sendMessage(ChatColor.BLUE + ChatColor.STRIKETHROUGH.toString() + "-------------------------------");
        sender.sendMessage(ChatColor.DARK_AQUA + "   Viewing stats for " +  ChatColor.AQUA + targetPlayer.getName());
        sender.sendMessage("");
        sender.sendMessage(ChatColor.DARK_AQUA + "   Level: " + targetUser.getLevelString().replace("[", "").replace("]", ""));
        sender.sendMessage(ChatColor.DARK_AQUA + "   XP: " + ChatColor.AQUA + targetUser.getUserProfile().getXP() + "/" + ChatColor.DARK_AQUA + UserProfile.getRequiredXP(targetUser.getUserProfile().getLevel() + 1) + " (approx.)");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.DARK_AQUA + "   Kills: " + ChatColor.GREEN + targetUser.getUserProfile().getKills());
        sender.sendMessage(ChatColor.DARK_AQUA + "   Deaths: " + ChatColor.RED + targetUser.getUserProfile().getDeaths());
        sender.sendMessage(ChatColor.DARK_AQUA + "   K/D: " + ChatColor.AQUA + targetUser.getUserProfile().getKDR());
        sender.sendMessage("");
        sender.sendMessage(ChatColor.DARK_AQUA + "   Wins: " + ChatColor.GREEN + targetUser.getUserProfile().getWins());
        sender.sendMessage(ChatColor.DARK_AQUA + "   Losses: " + ChatColor.RED + targetUser.getUserProfile().getLosses());
        sender.sendMessage(ChatColor.DARK_AQUA + "   W/L: " + ChatColor.AQUA + targetUser.getUserProfile().getWLR());
        sender.sendMessage(ChatColor.BLUE + ChatColor.STRIKETHROUGH.toString() + "-------------------------------");
    }


    public static void attemptJoinTeam(Player player, MatchTeam matchTeam, boolean autoJoin) {
        attemptJoinTeam(player, matchTeam, autoJoin, false);
    }

    public static void attemptJoinTeam(Player player, MatchTeam matchTeam, boolean autoJoin, boolean ignoreFull) {
        if (!ignoreFull && autoJoin && !player.hasPermission("tgm.pickteam") && !TGM.get().getModule(TeamManagerModule.class).getTeam(player).isSpectator()) {
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
            }
        }

        PlayerContext playerContext = TGM.get().getPlayerManager().getPlayerContext(player);
        TGM.get().getModule(TeamManagerModule.class).joinTeam(playerContext, matchTeam);
    }

    private static TextComponent profileToTextComponent(UserProfile profile, int place) {
        TextComponent main = new TextComponent(ChatColor.translateAlternateColorCodes('&', "&7" + place + "." + " &b" + profile.getName() + " &7(&9" + profile.getKills() + " kills&7)"));
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

    private static TextComponent mapToTextComponent(int position, MapInfo mapInfo) {
        String mapName = ChatColor.GOLD + mapInfo.getName();

        if (mapInfo.equals(TGM.get().getMatchManager().getMatch().getMapContainer().getMapInfo())) {
            mapName = ChatColor.GREEN + "" + (position + 1) + ". " + mapName;
        } else {
            mapName = ChatColor.WHITE + "" + (position + 1) + ". " + mapName;
        }
        TextComponent message = new TextComponent(mapName);
        message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/sn " + mapInfo.getName()));
        message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GOLD + mapInfo.getName()).append("\n\n")
                .append(ChatColor.GRAY + "Authors: ").append(ChatColor.YELLOW + mapInfo.getAuthors().stream().map(Strings::getAuthorUsername).collect(Collectors.joining(", "))).append("\n")
                .append(ChatColor.GRAY + "Game Type: ").append(ChatColor.YELLOW + mapInfo.getGametype().toString()).append("\n")
                .append(ChatColor.GRAY + "Version: ").append(ChatColor.YELLOW + mapInfo.getVersion()).create()));
        return message;
    }
}
