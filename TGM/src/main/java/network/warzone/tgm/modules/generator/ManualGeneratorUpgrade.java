package network.warzone.tgm.modules.generator;

import org.bukkit.inventory.ItemStack;

public class ManualGeneratorUpgrade extends GeneratorUpgrade {
    public ManualGeneratorUpgrade(int interval, ItemStack item, String broadcast, String holoContent) {
        super(interval, item, broadcast, holoContent);
    }
}
