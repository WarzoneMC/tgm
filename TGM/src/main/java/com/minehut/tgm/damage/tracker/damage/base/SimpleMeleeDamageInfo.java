package com.minehut.tgm.damage.tracker.damage.base;

import com.google.common.base.Preconditions;
import com.minehut.tgm.damage.tracker.base.AbstractDamageInfo;
import com.minehut.tgm.damage.tracker.damage.MeleeDamageInfo;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;

import javax.annotation.Nonnull;

public class SimpleMeleeDamageInfo extends AbstractDamageInfo implements MeleeDamageInfo {
    public SimpleMeleeDamageInfo(@Nonnull LivingEntity attacker, @Nonnull Material weaponMaterial) {
        super(attacker);

        Preconditions.checkNotNull(attacker, "attacker");
        Preconditions.checkNotNull(weaponMaterial, "weapon material");

        this.weaponMaterial = weaponMaterial;
    }

    public @Nonnull
    LivingEntity getAttacker() {
        return this.resolvedDamager;
    }

    public @Nonnull
    Material getWeapon() {
        return this.weaponMaterial;
    }

    private final @Nonnull
    Material weaponMaterial;
}
