package network.warzone.tgm.command;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandNumberFormatException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import network.warzone.tgm.TGM;
import network.warzone.tgm.modules.chat.ChatConstant;
import network.warzone.tgm.nickname.Nick;
import network.warzone.tgm.nickname.NickManager;
import network.warzone.tgm.nickname.NickedUserProfile;
import network.warzone.tgm.user.PlayerContext;
import network.warzone.warzoneapi.models.Rank;
import network.warzone.warzoneapi.models.RankList;
import network.warzone.warzoneapi.models.Skin;
import network.warzone.warzoneapi.models.UserProfile;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class NickCommands {

    private static final String NAME_REGEX = "^[a-z_A-Z0-9]{1,16}$";

    @Command(aliases = {"nicks"}, desc = "View all nicked players", max = 1)
    @CommandPermissions({"tgm.command.whois"})
    public static void nicks(CommandContext cmd, CommandSender sender) {
        List<Nick> nicks = TGM.get().getNickManager().getNicks();
        sender.sendMessage(ChatColor.YELLOW + "Nicked Players:");
        for (Nick nick : nicks) {
            sender.sendMessage(
                    ChatColor.DARK_PURPLE + nick.getOriginalName()
                            + ChatColor.GRAY + " is nicked as "
                            + ChatColor.LIGHT_PURPLE + nick.getName()
                            + (nick.isActive() ? ChatColor.GREEN + " [ACTIVE]" : "")
            );
        }
    }

    @Command(aliases = {"nick"}, flags= "f", desc = "Change nickname", usage = "(name)")
    @CommandPermissions({"tgm.command.nick"})
    public static void nick(CommandContext cmd, CommandSender sender) throws CommandNumberFormatException, NoSuchFieldException, IllegalAccessException {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatConstant.ERROR_COMMAND_PLAYERS_ONLY.toString());
            return;
        }

        if (cmd.argsLength() == 0) {
            sender.sendMessage(ChatColor.RED + "Invalid usage. /nick [player] <create/set/preview/apply/reset/status> ...");
            return;
        }

        String subcommand = cmd.getString(0);
        List<String> subcommands = Arrays.asList("create", "set", "preview", "apply", "reset", "status");

        Player p = ((Player) sender);
        Player target = p;
        boolean targetIsSelf = true;
        int index = 0;
        if (!subcommands.contains(subcommand)) {
            // Try and find player by name.
            target = Bukkit.getPlayer(subcommand);

            // If the player is online, switch the subcommand to the next argument.
            if (target != null) {
                if (cmd.argsLength() > 1) {
                    subcommand = cmd.getString(1);
                    index = 1;
                    targetIsSelf = false;
                } else {
                    sender.sendMessage(ChatColor.RED + "Invalid usage. /nick [player] <create/set/preview/apply/reset> ...");
                    return;
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Invalid player!");
                return;
            }
        }

        PlayerContext context = TGM.get().getPlayerManager().getPlayerContext(target);
        if ("create".equals(subcommand)) {
            create(cmd, p, index, context);
        } else if ("set".equals(subcommand)) {
            set(cmd, p, index, context, targetIsSelf);
        } else if ("preview".equals(subcommand)) {
            preview(p, context, targetIsSelf);
        } else if ("apply".equals(subcommand)) {
            apply(cmd, p, context, targetIsSelf);
        } else if ("reset".equals(subcommand)) {
            reset(cmd, p, context, targetIsSelf);
        } else if ("status".equals(subcommand)) {
            status(p, context, targetIsSelf);
        }
    }

    @Command(aliases = {"whois"}, desc = "Reveals a nicked player")
    @CommandPermissions({"tgm.command.whois"})
    public static void whois(CommandContext cmd, CommandSender sender) {
        if (cmd.argsLength() > 0) {
            String username = cmd.getString(0);
            Optional<Nick> optionalNick = TGM.get().getNickManager().getNicks().stream()
                    .filter(nick -> nick.getName().equals(username))
                    .findFirst();

            if (optionalNick.isPresent()) {
                sender.sendMessage(ChatColor.YELLOW + username + ChatColor.GREEN + " is " + ChatColor.YELLOW + optionalNick.get().getOriginalName());
            } else {
                sender.sendMessage(ChatColor.RED + "That user isn't nicked!");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Invalid usage. /whois <player>");
        }
    }

    private static void create(CommandContext cmd, Player sender, int index, PlayerContext context) throws CommandNumberFormatException {
        NickManager nickManager = TGM.get().getNickManager();
        String name = null;
        if (cmd.argsLength() > index + 1) {
            name = cmd.getString(index + 1);
            if (!name.matches(NAME_REGEX)) {
                sender.sendMessage(ChatColor.RED + "Invalid name.");
                name = null;
            } else if (Bukkit.getPlayer(name) != null) {
                sender.sendMessage(ChatColor.RED + "That player is online.");
                name = null;
            }
        }
        String skin = null;
        if (cmd.argsLength() > index + 2) {
            skin = cmd.getString(index + 2);
            if (nickManager.getUUID(skin) == null) {
                sender.sendMessage(ChatColor.RED + "Invalid skin.");
                skin = null;
            }
        }
        NickManager.NickDetails details = new NickManager.NickDetails(
                name,
                skin,
                getRank(cmd.getString(index + 3, "")),
                cmd.getInteger(index + 4, context.getUserProfile(true).getKills()),
                cmd.getInteger(index + 5, context.getUserProfile(true).getDeaths()),
                cmd.getInteger(index + 6, context.getUserProfile(true).getWins()),
                cmd.getInteger(index + 7, context.getUserProfile(true).getLosses()),
                cmd.getInteger(index + 8, context.getUserProfile(true).getWool_destroys()),
                "true".equals(cmd.getString(index + 9, null))
        );
        nickManager.create(context, details);
        send("Created new nick. Use [/nick preview] to view and [/nick apply] to apply it.", ChatColor.GREEN, sender);
    }

    private static void set(CommandContext cmd, Player sender, int index, PlayerContext context, boolean target) throws CommandNumberFormatException, NoSuchFieldException, IllegalAccessException {
        NickManager nickManager = TGM.get().getNickManager();
        Optional<Nick> optionalNick = nickManager.getNick(context);
        if (!optionalNick.isPresent()) {
            send((target ? "You do" : (ChatColor.YELLOW + context.getPlayer().getName() + ChatColor.RED + " does")) + " not have a nick. Use [/nick create] to create one.", ChatColor.RED, sender);
            return;
        }

        if (cmd.argsLength() > index + 1) {
            String setSubcommand = cmd.getString(index + 1);
            if ("name".equals(setSubcommand) && cmd.argsLength() > index + 2) {
                setName(cmd, index, sender, context);
            } else if ("skin".equals(setSubcommand) && cmd.argsLength() > index + 2) {
                setSkin(cmd, index, sender, context);
            } else if ("rank".equals(setSubcommand) && cmd.argsLength() > index + 2) {
                setRank(cmd, index, sender, context);
            } else if ("frozen".equals(setSubcommand)) {
                setFrozen(sender, context, nickManager);
            } else if ("stats".equals(setSubcommand) && cmd.argsLength() > index + 2) {
                setStats(cmd, sender, index, context, nickManager);
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Invalid usage. /nick (name) set <name/skin/rank/stats/frozen/Username> ...");
        }
    }

    private static void setStats(CommandContext cmd, Player sender, int index, PlayerContext context, NickManager nickManager) throws CommandNumberFormatException {
        String stat = cmd.getString(index + 2);
        List<String> stats = Arrays.asList("kills", "deaths", "wins", "losses", "objectives");
        if (stats.contains(stat)) {
            setPreset(cmd, sender, index, context, nickManager, stat);
        } else {
            setIndividualStat(sender, context, nickManager, stat);
        }
    }

    private static void setIndividualStat(Player sender, PlayerContext context, NickManager nickManager, String stat) {
        double wlr;
        double kdr;
        int objectives;
        if ("good".equals(stat)) {
            kdr = generateDouble(2, 3);
            wlr = generateDouble(2, 3);
            objectives = generateInt(200, 300);
        } else if ("average".equals(stat)) {
            kdr = generateDouble(0.75, 1.5);
            wlr = generateDouble(0.75, 1.5);
            objectives = generateInt(100, 200);
        } else if ("bad".equals(stat)) {
            kdr = generateDouble(0.1, 0.5);
            wlr = generateDouble(0.1, 0.5);
            objectives = generateInt(0, 100);
        } else if ("new".equals(stat)) {
            nickManager.update(context, nick -> {
                nick.getProfile().setKills(0);
                nick.getProfile().setDeaths(0);
                nick.getProfile().setWins(0);
                nick.getProfile().setDeaths(0);
                nick.getProfile().setWool_destroys(0);
                nick.getProfile().setNew(true);
            });
            sender.sendMessage(ChatColor.GREEN + "Updated stats to the " + ChatColor.YELLOW + stat + ChatColor.GREEN + " preset.");
            return;
        } else {
            sender.sendMessage(ChatColor.RED + "Invalid stat preset. Try good, average, bad or new");
            return;
        }

        nickManager.update(context, nick -> {
            int deaths = generateInt(50, 200);
            int losses = generateInt(10, 50);
            nick.getProfile().setKills((int) (deaths * kdr));
            nick.getProfile().setDeaths(deaths);
            nick.getProfile().setWins((int) (losses * wlr));
            nick.getProfile().setLosses(losses);
            nick.getProfile().setWool_destroys(objectives);
        });
        sender.sendMessage(ChatColor.GREEN + "Updated stats to the " + ChatColor.YELLOW + stat + ChatColor.GREEN + " preset.");
    }

    private static void setPreset(CommandContext cmd, Player sender, int index, PlayerContext context, NickManager nickManager, String stat) throws CommandNumberFormatException {
        if (cmd.argsLength() > index + 3) {
            int newValue = cmd.getInteger(index + 3);
            switch (stat) {
                case "kills":
                    nickManager.update(context, nick -> nick.getProfile().setKills(newValue));
                    break;
                case "deaths":
                    nickManager.update(context, nick -> nick.getProfile().setDeaths(newValue));
                    break;
                case "wins":
                    nickManager.update(context, nick -> nick.getProfile().setWins(newValue));
                    break;
                case "losses":
                    nickManager.update(context, nick -> nick.getProfile().setLosses(newValue));
                    break;
                case "objectives":
                    nickManager.update(context, nick -> nick.getProfile().setWool_destroys(newValue));
                    break;
                default:
                    sender.sendMessage(ChatColor.RED + "Invalid stat name. Try kills, deaths, wins, losses or objectives.");
                    return;
            }
            sender.sendMessage(ChatColor.GREEN + "Updated " + ChatColor.YELLOW + stat + ChatColor.GREEN + " to " + ChatColor.YELLOW + newValue);
        }
    }

    private static void setFrozen(Player sender, PlayerContext context, NickManager nickManager) {
        nickManager.update(context, nick -> {
            nick.getProfile().setFrozen(!nick.getProfile().isFrozen());
            sender.sendMessage(ChatColor.GREEN + "Toggled stat freeze " + ChatColor.YELLOW + (nick.getProfile().isFrozen() ? "on" : "off"));
        });
    }

    private static void setName(CommandContext cmd, int index, Player sender, PlayerContext context) throws NoSuchFieldException, IllegalAccessException {
        NickManager nickManager = TGM.get().getNickManager();
        String newName = cmd.getString(index + 2);
        if (!newName.matches(NAME_REGEX)) {
            sender.sendMessage(ChatColor.RED + "Invalid name.");
            return;
        }
        if (Bukkit.getPlayer(newName) != null) {
            sender.sendMessage(ChatColor.RED + "That player is online.");
            return;
        }
        nickManager.update(context, nick -> nick.setName(newName));

        if (cmd.hasFlag('f')) {
            nickManager.setName(context, newName);
            sender.sendMessage(ChatColor.GREEN + "Force updated nickname to " + ChatColor.YELLOW + newName);
        } else {
            sender.sendMessage(ChatColor.GREEN + "Updated nickname to " + ChatColor.YELLOW + newName);
        }
    }

    private static void setSkin(CommandContext cmd, int index, Player sender, PlayerContext context) {
        NickManager nickManager = TGM.get().getNickManager();
        String newSkin = cmd.getString(index + 2);
        UUID uuid = nickManager.getUUID(newSkin);
        if (uuid == null) {
            sender.sendMessage(ChatColor.RED + "Invalid player.");
            return;
        }
        Skin skin = nickManager.getSkin(uuid);
        nickManager.update(context, nick -> {
            nick.getDetails().setSkin(newSkin);
            nick.setSkin(skin);
        });

        if (cmd.hasFlag('f')) {
            nickManager.setSkin(context, skin);
            sender.sendMessage(ChatColor.GREEN + "Force updated skin to " + ChatColor.YELLOW + newSkin);
        } else {
            sender.sendMessage(ChatColor.GREEN + "Updated skin to " + ChatColor.YELLOW + newSkin);
        }
    }

    private static void setRank(CommandContext cmd, int index, Player sender, PlayerContext context) {
        NickManager nickManager = TGM.get().getNickManager();
        String rankName = cmd.getString(index + 2);
        Rank rank = null;
        RankList ranks = TGM.get().getTeamClient().retrieveRanks();
        for (Rank r : ranks) {
            if (r.getName().equalsIgnoreCase(rankName)) rank = r;
        }
        if (rank == null) {
            sender.sendMessage(ChatColor.RED + "Invalid rank. Valid ranks are "
                    + ranks.stream().map(Rank::getName).collect(Collectors.joining(", ")));
            return;
        }
        Rank finalRank = rank;
        nickManager.update(context, nick -> {
            nick.getProfile().setRanksLoaded(new ArrayList<>());
            nick.getProfile().addRank(finalRank);
        });
        sender.sendMessage(ChatColor.GREEN + "Set nicked rank to " + ChatColor.YELLOW + finalRank.getName());
    }

    private static void preview(Player sender, PlayerContext context, boolean target) {
        NickManager nickManager = TGM.get().getNickManager();
        Optional<Nick> optionalNick = nickManager.getNick(context);
        if (!optionalNick.isPresent()) {
            send((target ? "You do" : (ChatColor.YELLOW + context.getPlayer().getName() + ChatColor.RED + " does")) + " not have a nick. Use [/nick create] to create one.", ChatColor.RED, sender);
            return;
        }
        Nick nick = optionalNick.get();
        NickedUserProfile profile = nick.getProfile();

        sender.sendMessage(ChatColor.BLUE + ChatColor.STRIKETHROUGH.toString() + "-------------------------------");
        sender.sendMessage(ChatColor.DARK_AQUA + "   Previewing " + ChatColor.AQUA + context.getOriginalName() + ChatColor.DARK_AQUA + (context.getPlayer().getName().endsWith("s") ? "' nick" : "'s nick"));
        sender.sendMessage("");
        sender.sendMessage(ChatColor.DARK_AQUA + "   Name: " + ChatColor.AQUA + nick.getName());
        sender.sendMessage(ChatColor.DARK_AQUA + "   Skin: " + ChatColor.AQUA + nick.getDetails().getSkin());
        if (nick.getProfile().getHighestRank() != null) sender.sendMessage(ChatColor.DARK_AQUA + "   Rank: " + ChatColor.AQUA + ChatColor.translateAlternateColorCodes('&', nick.getProfile().getHighestRank().getPrefix()));
        sender.sendMessage("");
        sender.sendMessage(ChatColor.DARK_AQUA + "   Level: " + ChatColor.AQUA + profile.getLevel());
        sender.sendMessage(ChatColor.DARK_AQUA + "   XP: " + ChatColor.AQUA + profile.getXP() + "/" + ChatColor.DARK_AQUA + UserProfile.getRequiredXP(profile.getLevel() + 1) + " (approx.)");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.DARK_AQUA + "   Kills: " + ChatColor.GREEN + profile.getKills());
        sender.sendMessage(ChatColor.DARK_AQUA + "   Deaths: " + ChatColor.RED + profile.getDeaths());
        sender.sendMessage(ChatColor.DARK_AQUA + "   K/D: " + ChatColor.AQUA + profile.getKDR());
        sender.sendMessage("");
        sender.sendMessage(ChatColor.DARK_AQUA + "   Wins: " + ChatColor.GREEN + profile.getWins());
        sender.sendMessage(ChatColor.DARK_AQUA + "   Losses: " + ChatColor.RED + profile.getLosses());
        sender.sendMessage(ChatColor.DARK_AQUA + "   W/L: " + ChatColor.AQUA + profile.getWLR());
        sender.sendMessage("");
        sender.sendMessage(ChatColor.DARK_AQUA + "   Objectives: " + ChatColor.AQUA + profile.getWool_destroys());
        sender.sendMessage(ChatColor.BLUE + ChatColor.STRIKETHROUGH.toString() + "-------------------------------");
    }

    private static void apply(CommandContext cmd, Player sender, PlayerContext context, boolean target) {
        NickManager nickManager = TGM.get().getNickManager();
        if (!nickManager.hasNick(context)) {
            send((target ? "You do" : (ChatColor.YELLOW + context.getPlayer().getName() + ChatColor.RED + " does")) + " not have a nick. Use [/nick create] to create one.", ChatColor.RED, sender);
            return;
        }

        if (cmd.hasFlag('f')) {
            nickManager.apply(context, true);
            sender.sendMessage(ChatColor.GREEN + "Nick applied.");
        } else {
            nickManager.apply(context, false);
            sender.sendMessage(ChatColor.GREEN + "Nick applied. Reconnect for it to take affect.");
        }
    }

    private static void reset(CommandContext cmd, Player sender, PlayerContext context, boolean target) {
        NickManager nickManager = TGM.get().getNickManager();
        if (!nickManager.isNicked(context)) {
            send((target ? "You do" : (ChatColor.YELLOW + context.getPlayer().getName() + ChatColor.RED + " does")) + " not have an active nick. Use [/nick create] to create one.", ChatColor.RED, sender);
            return;
        }

        if (cmd.hasFlag('f')) {
            nickManager.reset(context, true);
            sender.sendMessage(ChatColor.GREEN + "Successfully reset your nickname.");
        } else {
            nickManager.reset(context, false);
        }
    }

    private static void status(Player sender, PlayerContext context, boolean target) {
        NickManager nickManager = TGM.get().getNickManager();
        Optional<Nick> optionalNick = nickManager.getNick(context);
        if (!optionalNick.isPresent()) {
            send((target ? "You do" : (ChatColor.YELLOW + context.getPlayer().getName() + ChatColor.RED + " does")) + " not have a nick. Use [/nick create] to create one.", ChatColor.RED, sender);
            return;
        }
        Nick nick = optionalNick.get();

        sender.sendMessage(ChatColor.YELLOW + "Nick Status:");
        sender.sendMessage(nick.isApplied() ? ChatColor.GREEN + "Applied" : ChatColor.RED + "Unapplied");
        sender.sendMessage(nick.isActive() ? ChatColor.GREEN + "Active" : ChatColor.RED + "Inactive");
        if (nick.getProfile().isFrozen()) sender.sendMessage(ChatColor.YELLOW + "Frozen");
    }

    private static Rank getRank(String name) {
        Rank rank = null;
        for (Rank r : TGM.get().getTeamClient().retrieveRanks()) {
            if (r.getName().equalsIgnoreCase(name)) rank = r;
        }
        return rank;
    }

    private static void send(String message, ChatColor color, Player target) {
        String pattern = "\\[(/[a-zA-Z0-9 ]+)]";
        String formattedMessage = message.replaceAll(pattern, ChatColor.YELLOW + "$1" + color);
        target.sendMessage(color + formattedMessage);
    }

    private static double generateDouble(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max + 1);
    }

    private static int generateInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

}
