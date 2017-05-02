package com.minehut.tgm.damage.tracker;

import org.bukkit.entity.LivingEntity;

import javax.annotation.Nonnull;

public interface LifetimeManager {
    @Nonnull
    Lifetime getLifetime(@Nonnull LivingEntity entity);

    @Nonnull
    Lifetime setLifetime(@Nonnull LivingEntity entity, @Nonnull Lifetime lifetime);

    @Nonnull
    Lifetime newLifetime(@Nonnull LivingEntity entity);

    @Nonnull
    Lifetime endLifetime(@Nonnull LivingEntity entity);
}
