package network.warzone.tgm.modules.killstreak;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;


class SoundKillstreakAction implements KillstreakAction {
    private Sound sound;
    private SoundTarget soundTarget;
    private float volume;
    private float pitch;

    public SoundKillstreakAction(Sound sound, SoundTarget soundTarget, float volume, float pitch) {
        this.sound = sound;
        this.soundTarget = soundTarget;
        this.volume = volume;
        this.pitch = pitch;
    }

    @Override
    public void apply(Player killer) {
        if(soundTarget == SoundTarget.EVERYONE) {
            for(Player p : Bukkit.getOnlinePlayers()) {
                p.playSound(p.getLocation().clone().add(0.0, 100.0, 0.0), sound, volume, pitch);
            }
        } else {
            if(killer == null) return;
            killer.playSound(killer.getLocation().clone().add(0.0, 100.0, 0.0), sound, volume, pitch);
        }
    }

    enum SoundTarget {
        EVERYONE, PLAYER
    }
}
