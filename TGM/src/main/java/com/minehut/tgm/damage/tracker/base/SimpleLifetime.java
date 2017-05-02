package com.minehut.tgm.damage.tracker.base;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.minehut.tgm.damage.tracker.Damage;
import com.minehut.tgm.damage.tracker.DamageInfo;
import com.minehut.tgm.damage.tracker.Lifetime;
import org.joda.time.Instant;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

public class SimpleLifetime implements Lifetime {
    private Instant start;
    private Instant end;
    private final List<Damage> damage = Lists.newArrayList();

    public @Nullable
    Instant getStart() {
        return this.start;
    }

    public void setStart(@Nonnull Instant start) {
        Preconditions.checkNotNull(start, "start");

        this.start = start;
    }

    public @Nullable
    Instant getEnd() {
        return this.end;
    }

    public void setEnd(@Nonnull Instant end) {
        Preconditions.checkNotNull(end, "end");

        this.end = end;
    }

    public @Nonnull
    List<Damage> getDamage() {
        return Collections.unmodifiableList(this.damage);
    }

    public @Nonnull
    ListIterator<Damage> getDamageFirst() {
        return Collections.unmodifiableList(this.damage).listIterator();
    }

    public @Nonnull
    ListIterator<Damage> getDamageLast() {
        return Collections.unmodifiableList(this.damage).listIterator(this.damage.size());
    }

    public @Nullable
    Damage getFirstDamage() {
        if(!this.damage.isEmpty()) {
            return this.damage.get(0);
        } else {
            return null;
        }
    }

    public @Nullable
    Damage getLastDamage() {
        if(!this.damage.isEmpty()) {
            return this.damage.get(this.damage.size() - 1);
        } else {
            return null;
        }
    }

    public @Nullable
    Damage getLastDamage(@Nonnull Class<? extends DamageInfo> damageInfoClass) {
        Preconditions.checkNotNull(damageInfoClass, "damage info class");

        for(ListIterator<Damage> it = this.getDamageLast(); it.hasPrevious(); ) {
            Damage damage = it.previous();
            if(damageInfoClass.isInstance(damage.getInfo())) {
                return damage;
            }
        }

        return null;
    }

    public void addDamage(@Nonnull Damage damage) {
        Preconditions.checkNotNull(damage, "damage");

        this.damage.add(damage);
    }
}
