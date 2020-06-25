package network.warzone.tgm.modules.scoreboard;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleScoreboard {

    private static Map<String, OfflinePlayer> cache = new HashMap<>();

    private Scoreboard scoreboard;
    private String title;
    private Map<String, Integer> scores;
    private Objective obj;
    private List<Team> teams;
    private List<Integer> removed;
    private Set<String> updated;

    private final UUID invalidUserUUID = UUID.nameUUIDFromBytes("InvalidUsername".getBytes(Charsets.UTF_8));
    private Constructor<?> craftOfflinePlayerConstructor;

    public SimpleScoreboard(String title) {
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.title = ChatColor.translateAlternateColorCodes('&', title);
        this.scores = new ConcurrentHashMap<>();
        this.teams = Collections.synchronizedList(Lists.newArrayList());
        this.removed = Lists.newArrayList();
        this.updated = Collections.synchronizedSet(new HashSet<>());
    }

    public void add(String text, Integer score) {
        String formattedText = ChatColor.translateAlternateColorCodes('&', text);
        while (scores.containsKey(formattedText)) formattedText += ChatColor.RESET;

        if (remove(score, formattedText, false) || !scores.containsValue(score)) {
            updated.add(formattedText);
        }

        scores.put(formattedText, score);
    }

    public void removeAll(Set<Integer> exclusions) {
        for (Map.Entry<String, Integer> score : scores.entrySet()) {
            if (exclusions.contains(score.getValue())) continue;
            remove(score.getValue());
        }
    }

    public boolean remove(Integer score, String text) {
        return remove(score, text, true);
    }

    public boolean remove(Integer score, String n, boolean b) {
        String toRemove = get(score, n);

        if (toRemove == null)
            return false;

        scores.remove(toRemove);

        if (b) removed.add(score);

        return true;
    }

    public boolean remove(Integer score) {
        return remove(score, true);
    }

    public boolean remove(Integer score, boolean b) {
        String toRemove = get(score);

        if (toRemove == null)
            return false;

        scores.remove(toRemove);
        removed.add(score);
        if  (b) removed.add(score);

        return true;
    }

    public String get(int score, String n) {
        String str = null;

        for (Map.Entry<String, Integer> entry : scores.entrySet()) {
            if (entry.getValue().equals(score) &&
                    !entry.getKey().equals(n)) {
                str = entry.getKey();
            }
        }

        return str;
    }

    public String get(int score) {
        String str = null;

        for (Map.Entry<String, Integer> entry : scores.entrySet()) {
            if (entry.getValue().equals(score)) {
                str = entry.getKey();
            }
        }

        return str;
    }

    private Map.Entry<Team, OfflinePlayer> createTeam(String text, int pos) {
        Team team;
        ChatColor color = ChatColor.values()[pos];
        OfflinePlayer result;

        if (!cache.containsKey(color.toString())) cache.put(color.toString(), getOfflinePlayerSkipLookup(color.toString()));

        result = cache.get(color.toString());

        try {
            team = scoreboard.registerNewTeam("text-" + (teams.size() + 1));
        } catch (IllegalArgumentException e) {
            team = scoreboard.getTeam("text-" + (teams.size()));
        }

        applyText(team, text, result);

        teams.add(team);
        return new AbstractMap.SimpleEntry<>(team, result);
    }

    private void applyText(Team team, String text, OfflinePlayer result) {
        Iterator<String> iterator = Splitter.fixedLength(16).split(text).iterator();
        String prefix = iterator.next();

        team.setPrefix(prefix);

        if(!team.hasPlayer(result)) team.addPlayer(result);

        if (text.length() > 16) {
            String prefixColor = ChatColor.getLastColors(prefix);
            String suffix = iterator.next();

            if (prefix.endsWith(String.valueOf(ChatColor.COLOR_CHAR))) {
                prefix = prefix.substring(0, prefix.length() - 1);
                team.setPrefix(prefix);
                prefixColor = ChatColor.getByChar(suffix.charAt(0)).toString();
                suffix = suffix.substring(1);
            }

            if (prefixColor == null)
                prefixColor = "";

            if (suffix.length() > 16) {
                suffix = suffix.substring(0, (13 - prefixColor.length())); // cut off suffix, done if text is over 30 characters
            }

            team.setSuffix(("".equals(prefixColor) ? ChatColor.RESET : prefixColor) + suffix);
        }
    }

    public void update() {
        if (updated.isEmpty()) {
            return;
        }

        if (obj == null) {
            obj = scoreboard.registerNewObjective((title.length() > 16 ? title.substring(0, 15) : title), "dummy");
            obj.setDisplayName(title);
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        }

        removed.stream().forEach((remove) -> {
            for (String s : scoreboard.getEntries()) {
                Score score = obj.getScore(s);
                
                if (score == null) continue;
                if (score.getScore() != remove) continue;

                scoreboard.resetScores(s);
            }
        });

        removed.clear();

        int index = scores.size();

        for (Map.Entry<String, Integer> text : scores.entrySet()) {
            Team t = scoreboard.getTeam(ChatColor.values()[text.getValue()].toString());
            Map.Entry<Team, OfflinePlayer> team;

            if(!updated.contains(text.getKey())) continue;

            if(t != null) {
                String color = ChatColor.values()[text.getValue()].toString();

                if (!cache.containsKey(color)) cache.put(color, getOfflinePlayerSkipLookup(color));

                team = new AbstractMap.SimpleEntry<>(t, cache.get(color));
                applyText(team.getKey(), text.getKey(), team.getValue());
                index -= 1;

                continue;
            } else {
                team = createTeam(text.getKey(), text.getValue());
            }

            Integer score = text.getValue() != null ? text.getValue() : index;

            obj.getScore(team.getValue()).setScore(score);
            index -= 1;
        }

        updated.clear();
    }

    public void setTitle(String title) {
        this.title = ChatColor.translateAlternateColorCodes('&', title);

        if(obj != null) obj.setDisplayName(this.title);
    }

    public void reset() {
        for (Team t : teams) t.unregister();
        teams.clear();
        scores.clear();
    }

    public Scoreboard getScoreboard() {
        return scoreboard;
    }

    public void send(Player... players) {
        for (Player p : players)
            p.setScoreboard(scoreboard);
    }

    @SuppressWarnings("deprecation")
    private OfflinePlayer getOfflinePlayerSkipLookup(String name) {
        try {
            if (craftOfflinePlayerConstructor == null) {
                Class<?> craftOfflinePlayerClass = Class.forName(Bukkit.getServer().getClass().getName().replace("CraftServer", "CraftOfflinePlayer"));
                craftOfflinePlayerConstructor = craftOfflinePlayerClass.getDeclaredConstructor(Bukkit.getServer().getClass(), GameProfile.class);
                craftOfflinePlayerConstructor.setAccessible(true);
            }
            GameProfile gameProfile = new GameProfile(invalidUserUUID, name);
            Object craftOfflinePlayer = craftOfflinePlayerConstructor.newInstance(Bukkit.getServer(), gameProfile);
            return (OfflinePlayer) craftOfflinePlayer;
        } catch (Throwable t) { // Fallback if fail
            return Bukkit.getOfflinePlayer(name);
        }
    }
}