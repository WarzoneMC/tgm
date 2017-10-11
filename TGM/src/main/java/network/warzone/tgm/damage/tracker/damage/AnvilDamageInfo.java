package network.warzone.tgm.damage.tracker.damage;

import network.warzone.tgm.damage.tracker.base.AbstractDamageInfo;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AnvilDamageInfo extends AbstractDamageInfo {

    public AnvilDamageInfo(@Nullable FallingBlock anvil, @Nullable LivingEntity resolvedDamager, @Nullable OfflinePlayer offlinePlayer) {
        super(resolvedDamager);
        
        this.anvil = anvil;
        this.offlinePlayer = offlinePlayer;
    }
    
    public @Nonnull
    FallingBlock getAnvil() {
        return this.anvil;
    }
    
    public @Nullable
    OfflinePlayer getOffinePlayer() {
        return this.offlinePlayer;
    }

    protected final @Nonnull
    FallingBlock anvil;
    protected final @Nullable
    OfflinePlayer offlinePlayer;
    
    @Override
    public @Nonnull
    String toString() {
        return "AnvilDamageInfo{anvil=" + this.anvil + ",damager=" + this.resolvedDamager + ",offlinePlayer=" + this.offlinePlayer + "}";
    }
}
