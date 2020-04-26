package network.warzone.tgm.modules.damage;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.modules.team.TeamManagerModule;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.projectiles.ProjectileSource;

/**
 * Created by Jorge on 9/24/2017.
 */
public class EntityDamageModule extends MatchModule implements Listener {

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        ProjectileSource shooter = event.getEntity().getShooter();
        if (!(shooter instanceof Player)) return;
        if (event.getEntityType() != EntityType.FISHING_HOOK &&
            event.getEntityType() != EntityType.SNOWBALL &&
            event.getEntityType() != EntityType.EGG) return;
        if (event.getHitEntity() instanceof Damageable) {
            ((Damageable) event.getHitEntity()).damage(0.01, (Player) shooter);
        }

    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if(event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) {
            ProjectileSource shooter = ((Projectile) event.getDamager()).getShooter();
            if(!(shooter instanceof Player && event.getEntity() instanceof Player)) return;
            Player damaged = (Player) event.getEntity();
            Player playerShooter = (Player) shooter;
            MatchTeam damagedTeam = TGM.get().getModule(TeamManagerModule.class).getTeam(damaged);
            if(damagedTeam == null) return;
            if(!damagedTeam.isSpectator() && (damaged.getHealth() - event.getFinalDamage() >= 0)) playerShooter.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(damagedTeam.getColor() + damaged.getName() + ChatColor.DARK_GRAY + " [" + ChatColor.WHITE + ((int)damaged.getHealth() - (int) event.getFinalDamage()) + ChatColor.GRAY + "/" + damaged.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() + ChatColor.DARK_GRAY + "]"));
        }
    }

}
