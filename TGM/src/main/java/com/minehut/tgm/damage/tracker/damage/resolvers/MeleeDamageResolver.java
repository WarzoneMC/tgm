package com.minehut.tgm.damage.tracker.damage.resolvers;

import com.minehut.tgm.damage.tracker.DamageInfo;
import com.minehut.tgm.damage.tracker.DamageResolver;
import com.minehut.tgm.damage.tracker.Lifetime;
import com.minehut.tgm.damage.tracker.damage.base.SimpleMeleeDamageInfo;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MeleeDamageResolver implements DamageResolver {
    public @Nullable
    DamageInfo resolve(@Nonnull LivingEntity entity, @Nonnull Lifetime lifetime, @Nonnull EntityDamageEvent damageEvent) {
        if(damageEvent instanceof EntityDamageByEntityEvent && damageEvent.getCause() == DamageCause.ENTITY_ATTACK) {
            EntityDamageByEntityEvent entityEvent = (EntityDamageByEntityEvent) damageEvent;

            if(entityEvent.getDamager() instanceof LivingEntity) {
                LivingEntity attacker = (LivingEntity) entityEvent.getDamager();

                Material weaponMaterial;
                ItemStack held = attacker.getEquipment().getItemInHand();
                if(held != null) {
                    weaponMaterial = held.getType();
                } else {
                    weaponMaterial = Material.AIR;
                }

                return new SimpleMeleeDamageInfo(attacker, weaponMaterial);
            }
        }

        return null;
    }
}
