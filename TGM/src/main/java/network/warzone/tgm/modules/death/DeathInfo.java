package network.warzone.tgm.modules.death;

import lombok.ToString;
import network.warzone.tgm.modules.team.MatchTeam;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

@ToString
public class DeathInfo {

    public Player player;
    public Player killer;
    public ItemStack item;
    public EntityDamageEvent.DamageCause cause;
    public String playerName;
    public String killerName;
    public MatchTeam playerTeam;
    public MatchTeam killerTeam;
    public Location playerLocation;
    public Location killerLocation;
    public long stampKill;

    public DeathInfo(Player player) {
        this.player = player;
    }

}
