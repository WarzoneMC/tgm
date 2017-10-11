package network.warzone.tgm.damage.tracker.damage.base;

import network.warzone.tgm.damage.tracker.base.AbstractDamageInfo;
import network.warzone.tgm.damage.tracker.damage.LavaDamageInfo;
import org.bukkit.entity.LivingEntity;

import javax.annotation.Nullable;

public class SimpleLavaDamageInfo extends AbstractDamageInfo implements LavaDamageInfo {
    public SimpleLavaDamageInfo(@Nullable LivingEntity resolvedDamager) {
        super(resolvedDamager);
    }
}
