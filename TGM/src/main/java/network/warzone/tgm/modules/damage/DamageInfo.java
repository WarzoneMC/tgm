package network.warzone.tgm.modules.damage;

import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * Created by luke on 12/13/15.
 */
public class DamageInfo {

    EntityDamageEvent event;

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

    public LivingEntity getHurtEntity() {
        return hurtEntity;
    }

    public LivingEntity getDamagerEntity() {
        return damagerEntity;
    }

    public Player getHurtPlayer() {
        return hurtPlayer;
    }

    public Player getDamagerPlayer() {
        return damagerPlayer;
    }

    public Entity getHurtNonLivingEntity() {
        return hurtNonLivingEntity;
    }

    public Projectile getProjectile() {
        return projectile;
    }

    public TNTPrimed getTnt() {
        return tnt;
    }
}
