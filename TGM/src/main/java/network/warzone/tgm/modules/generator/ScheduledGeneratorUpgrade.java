package network.warzone.tgm.modules.generator;

import lombok.Getter;
import org.bukkit.inventory.ItemStack;

public class ScheduledGeneratorUpgrade extends GeneratorUpgrade {
    @Getter
    private int time;
    public ScheduledGeneratorUpgrade(int time, int interval, ItemStack item, String broadcast, String holoContent) {
        super(interval, item, broadcast, holoContent);
        this.time = time;
    }
}
