package network.warzone.tgm.damage.tracker.trackers.base;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import network.warzone.tgm.damage.tracker.base.AbstractTracker;
import network.warzone.tgm.damage.tracker.trackers.ExplosiveTracker;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Map;

public class SimpleExplosiveTracker extends AbstractTracker implements ExplosiveTracker {
    private final Map<Block, Player> placedBlocks = Maps.newHashMap();
    private final Map<TNTPrimed, Player> ownedTNTs = Maps.newHashMap();

    public boolean hasOwner(@Nonnull TNTPrimed entity) {
        Preconditions.checkNotNull(entity, "tnt entity");

        return this.ownedTNTs.containsKey(entity);
    }

    public @Nullable
    Player getOwner(@Nonnull TNTPrimed entity) {
        Preconditions.checkNotNull(entity, "tnt entity");

        return this.ownedTNTs.get(entity);
    }

    public @Nullable
    Player setOwner(@Nonnull TNTPrimed entity, @Nullable Player player) {
        Preconditions.checkNotNull(entity, "tnt entity");

        if(player != null) {
            return this.ownedTNTs.put(entity, player);
        } else {
            return this.ownedTNTs.remove(entity);
        }
    }

    public boolean hasPlacer(@Nonnull Block block) {
        Preconditions.checkNotNull(block, "block");

        return this.placedBlocks.containsKey(block);
    }

    public @Nullable
    Player getPlacer(@Nonnull Block block) {
        Preconditions.checkNotNull(block, "block");

        return this.placedBlocks.get(block);
    }

    public @Nullable
    Player setPlacer(@Nonnull Block block, @Nullable Player player) {
        Preconditions.checkNotNull(block, "block");

        if(player != null) {
            return this.placedBlocks.put(block, player);
        } else {
            return this.placedBlocks.remove(block);
        }
    }

    public void clear(@Nonnull World world) {
        Preconditions.checkNotNull(world, "world");

        // clear information about blocks in that world
        for(Iterator<Map.Entry<Block, Player>> it = this.placedBlocks.entrySet().iterator(); it.hasNext(); ) {
            Block block = it.next().getKey();
            if(block.getWorld().equals(world)) {
                it.remove();
            }
        }

        // clear information about entities in that world
        for(Iterator<Map.Entry<TNTPrimed, Player>> it = this.ownedTNTs.entrySet().iterator(); it.hasNext(); ) {
            TNTPrimed tnt = it.next().getKey();
            if(tnt.getWorld().equals(world)) {
                it.remove();
            }
        }
    }
}
