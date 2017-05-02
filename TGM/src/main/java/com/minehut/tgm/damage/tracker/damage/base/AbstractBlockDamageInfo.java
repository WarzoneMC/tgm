package com.minehut.tgm.damage.tracker.damage.base;

import com.google.common.base.Preconditions;
import com.minehut.tgm.damage.tracker.base.AbstractDamageInfo;
import com.minehut.tgm.damage.tracker.damage.BlockDamageInfo;
import org.bukkit.block.BlockState;
import org.bukkit.entity.LivingEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AbstractBlockDamageInfo extends AbstractDamageInfo implements BlockDamageInfo {
    public AbstractBlockDamageInfo(@Nullable LivingEntity resolvedDamager, @Nonnull BlockState blockDamager) {
        super(resolvedDamager);

        Preconditions.checkNotNull(blockDamager, "block damager");

        this.blockDamager = blockDamager;
    }

    public @Nonnull
    BlockState getBlockDamager() {
        return this.blockDamager;
    }

    private final @Nonnull
    BlockState blockDamager;
}
