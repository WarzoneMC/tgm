package network.warzone.tgm.damage.tracker;

import org.bukkit.entity.LivingEntity;

import javax.annotation.Nullable;

/**
 * Provides more detailed information about a damage instance.
 *
 * Subclasses should be completely immutable.
 */
public interface DamageInfo {
    /**
     * Gets the living entity most responsible for this damage.
     *
     * @return Resolved damager or null if none exists
     */
    @Nullable
    LivingEntity getResolvedDamager();
}
