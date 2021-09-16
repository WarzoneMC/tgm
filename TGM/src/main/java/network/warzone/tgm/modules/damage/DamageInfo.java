package network.warzone.tgm.modules.damage;

import lombok.Getter;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * Created by luke on 12/13/15.
 */
@Getter
public class DamageInfo {

    private EntityDamageEvent event;

    private LivingEntity hurtEntity = null;
    private LivingEntity damagerEntity = null;

    private Player hurtPlayer = null;
    private Player damagerPlayer = null;

    private Entity hurtNonLivingEntity = null;

    private Projectile projectile;
    private TNTPrimed tnt;

    public DamageInfo(EntityDamageEvent event) {
        this.event = event;

        if (!(event.getEntity() instanceof LivingEntity)) {
            this.hurtNonLivingEntity = event.getEntity();
            return;
        }

        this.hurtEntity = (LivingEntity) event.getEntity();
        if (hurtEntity instanceof Player) {
            this.hurtPlayer = (Player) hurtEntity;
        }

        if (event instanceof EntityDamageByEntityEvent) {
            Entity e = ((EntityDamageByEntityEvent) event).getDamager();
            if (e instanceof LivingEntity) {
                this.damagerEntity = (LivingEntity) e;

                if (damagerEntity instanceof Player) {
                    this.damagerPlayer = (Player) damagerEntity;
                }
            }

            else if (e instanceof Projectile) {
                this.projectile = (Projectile) e;
                if (projectile.getShooter() instanceof LivingEntity) {
                    this.damagerEntity = (LivingEntity) projectile.getShooter();

                    if (damagerEntity instanceof Player) {
                        this.damagerPlayer = (Player) damagerEntity;
                    }
                }
            }

            else if (e instanceof TNTPrimed) {
                this.tnt = (TNTPrimed) e;
                //todo: tnt tracking
            }
        }
    }
}
