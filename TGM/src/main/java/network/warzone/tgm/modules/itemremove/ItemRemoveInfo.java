package network.warzone.tgm.modules.itemremove;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.Material;

/**
 * Created by MSH on 03/25/2020
 */
@AllArgsConstructor @Data
public class ItemRemoveInfo {
    private final Material material;
    // Sources
    private boolean preventingDeathDrop;
    private boolean preventingPlayerDrop;
    private boolean preventingItemSpawn;

    public ItemRemoveInfo(Material material) {
        this.material = material;
        this.preventingDeathDrop = true;
        this.preventingPlayerDrop = false;
        this.preventingItemSpawn = false;
    }

}
