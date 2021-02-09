package network.warzone.tgm.modules.ctf.objective;

import network.warzone.tgm.TGM;
import network.warzone.tgm.modules.flag.FlagSubscriber;
import network.warzone.tgm.modules.flag.MatchFlag;
import network.warzone.tgm.modules.scoreboard.ScoreboardManagerModule;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.user.PlayerContext;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

/**
 * Controls different objectives of CTF as a FlagSubscriber
 * Also more than likely handles scoreboard
 * Created by yikes on 12/15/2019
 */
public abstract class CTFController implements FlagSubscriber, Listener {
    private CTFControllerSubscriber subscriber;
    protected List<MatchFlag> allFlags;
    protected TeamManagerModule teamManagerModule;
    protected ScoreboardManagerModule scoreboardManagerModule;
    protected List<PotionEffect> effects;
    public CTFController(CTFControllerSubscriber subscriber, List<MatchFlag> allFlags, List<PotionEffect> effects) {
        this.subscriber = subscriber;
        this.allFlags = allFlags;
        this.effects = effects;
        this.teamManagerModule = TGM.get().getModule(TeamManagerModule.class);
        this.scoreboardManagerModule = TGM.get().getModule(ScoreboardManagerModule.class);
        TGM.registerEvents(this);
    }

    @Override
    public void pickup(MatchFlag flag, Player stealer) {
        stealer.getInventory().setHelmet(flag.generateBannerItem());
        for (PotionEffect effect : effects) {
            stealer.addPotionEffect(effect);
        }
        MatchTeam team = teamManagerModule.getTeam(stealer);
        if (flag.getTeam() == null) {
            Bukkit.broadcastMessage(team.getColor() + stealer.getName() + ChatColor.WHITE + " picked up " + ChatColor.BOLD + flag.getName());
        } else {
            Bukkit.broadcastMessage(team.getColor() + stealer.getName() + ChatColor.WHITE
                    + " picked up " + flag.getTeam().getColor() + ChatColor.BOLD + flag.getTeam().getAlias()
                    + ChatColor.WHITE + "'s " + ChatColor.BOLD + flag.getName());
        }
        playSoundForTeam(team);
    }

    @Override
    public void drop(MatchFlag flag, Player stealer, Player attacker) {
        MatchTeam team = teamManagerModule.getTeam(stealer);
        if (team == null) team = teamManagerModule.getSpectators();
        if (team == null) return;
        if (flag.getTeam() == null) {
            Bukkit.broadcastMessage(team.getColor() + stealer.getName() + ChatColor.WHITE + " dropped " + ChatColor.BOLD + flag.getName());
        } else {
            Bukkit.broadcastMessage(team.getColor() + stealer.getName() + ChatColor.WHITE
                    + " dropped " + flag.getTeam().getColor() + ChatColor.BOLD + flag.getTeam().getAlias()
                    + ChatColor.WHITE + "'s " + ChatColor.BOLD + flag.getName());
        }
    }

    @Override
    public void capture(MatchFlag flag, Player capturer) {
        capturer.getInventory().setHelmet(new ItemStack(Material.AIR));
        for (PotionEffect effect : effects) {
            capturer.removePotionEffect(effect.getType());
        }
        MatchTeam capturerTeam = teamManagerModule.getTeam(capturer);
        if (flag.getTeam() == null) {
            Bukkit.broadcastMessage(capturerTeam.getColor() + capturer.getName() + ChatColor.WHITE + " captured " + ChatColor.BOLD + flag.getName());
        } else {
            Bukkit.broadcastMessage(capturerTeam.getColor() + capturer.getName() + ChatColor.WHITE
                    + " captured " + flag.getTeam().getColor() + ChatColor.BOLD + flag.getTeam().getAlias()
                    + ChatColor.WHITE + "'s " + ChatColor.BOLD + flag.getName());
        }
        playSoundForTeam(capturerTeam);
    }

    public void unload() {
        TGM.unregisterEvents(this);
    }

    public final void gameOver(MatchTeam team) {
        subscriber.gameOver(team);
    }

    private void playSoundForTeam(MatchTeam successTeam) {
        for (MatchTeam otherTeam : teamManagerModule.getTeams()) {
            for (PlayerContext playerContext : otherTeam.getMembers()) {
                if (otherTeam.isSpectator() || otherTeam.equals(successTeam)) {
                    playerContext.getPlayer().playSound(playerContext.getPlayer().getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 0.7f, 2f);
                } else {
                    playerContext.getPlayer().playSound(playerContext.getPlayer().getLocation(), Sound.ENTITY_BLAZE_DEATH, 0.8f, 0.8f);
                }
            }
        }
    }
}
