package network.warzone.tgm.damage.tracker.damage.base;

import com.google.common.base.Preconditions;
import network.warzone.tgm.damage.tracker.base.AbstractDamageInfo;
import network.warzone.tgm.damage.tracker.damage.FallDamageInfo;
import org.bukkit.entity.LivingEntity;

import javax.annotation.Nullable;

public class SimpleFallDamageInfo extends AbstractDamageInfo implements FallDamageInfo {
    public SimpleFallDamageInfo(@Nullable LivingEntity resolvedDamager, float fallDistance) {
        super(resolvedDamager);

        Preconditions.checkArgument(fallDistance >= 0, "fall distance must be >= 0");

        this.fallDistance = fallDistance;
    }

    public float getFallDistance() {
        return this.fallDistance;
    }

    private final float fallDistance;
}
