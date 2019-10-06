package network.warzone.tgm.modules.death;

import lombok.ToString;
import network.warzone.tgm.modules.team.MatchTeam;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

@ToString
public class DeathInfo {
    public DeathInfo(Player player) {
        this.player = player;
    }
    public Player player, killer;
    public ItemStack item;
    public EntityDamageEvent.DamageCause cause;
    public String playerName, killerName;
    public MatchTeam playerTeam, killerTeam;
    public Location playerLocation, killerLocation;
    public long stampKill;
}
