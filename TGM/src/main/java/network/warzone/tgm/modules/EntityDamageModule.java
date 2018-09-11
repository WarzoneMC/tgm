package network.warzone.tgm.modules;

import network.warzone.tgm.TGM;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamManagerModule;
import network.warzone.tgm.user.PlayerContext;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.projectiles.ProjectileSource;

/**
 * Created by Jorge on 9/24/2017.
 */
public class EntityDamageModule extends MatchModule implements Listener {

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntityType() != EntityType.FISHING_HOOK &&
            event.getEntityType() != EntityType.SNOWBALL &&
            event.getEntityType() != EntityType.EGG) return;
        ProjectileSource shooter = event.getEntity().getShooter();
        if (shooter != null && shooter instanceof Player && event.getHitEntity() instanceof Damageable) {
            ((Damageable) event.getHitEntity()).damage(0.01, (Player) shooter);
            if (event.getHitEntity() instanceof Player) {
                Player damaged = (Player) event.getHitEntity();
                MatchTeam team = TGM.get().getModule(TeamManagerModule.class).getTeam(damaged);
                if (team != null && !team.isSpectator()) {
                    ((Player) shooter).sendActionBar(team.getColor() + damaged.getName() + ChatColor.DARK_GRAY + " [" + ChatColor.WHITE + (int)damaged.getHealth() + ChatColor.GRAY + "/" + damaged.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() + ChatColor.DARK_GRAY + "]");
                }
            }
        }

    }

}
