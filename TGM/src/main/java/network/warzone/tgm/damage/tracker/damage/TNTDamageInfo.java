package network.warzone.tgm.damage.tracker.damage;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.TNTPrimed;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TNTDamageInfo extends ExplosiveDamageInfo {
    public TNTDamageInfo(@Nonnull TNTPrimed explosive, @Nullable LivingEntity resolvedDamager) {
        super(explosive, resolvedDamager);
    }

    @Override
    public @Nonnull
    TNTPrimed getExplosive() {
        return (TNTPrimed) this.explosive;
    }

    @Override
    public @Nonnull
    String toString() {
        return "TNTDamageInfo{explosive=" + this.explosive + ",damager=" + this.resolvedDamager + "}";
    }
}
