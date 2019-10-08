package network.warzone.tgm.modules.generator;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

@Getter
public class Generator {
    private String name;
    @Setter private ItemStack item;
    private Location location;
    private int limit;
    @Setter private int interval;
    private GeneratorHologram generatorHologram;
    private GeneratorUpgrader generatorUpgrader;

    private int currentInterval;
    private int runningTime = 0;
    private GeneratorTimeHandle generatorTimeHandle;

    public Generator(String name, ItemStack item, Location location, int limit, int interval, GeneratorHologram generatorHologram, GeneratorUpgrader generatorUpgrader) {
        this.name = name;
        this.item = item;
        this.location = location;
        this.limit = limit;
        this.interval = interval;
        this.currentInterval = interval;
        this.generatorHologram = generatorHologram;
        this.generatorUpgrader = generatorUpgrader;
        this.generatorUpgrader.setHostGenerator(this);

        if (generatorHologram != null) setupTimeHandle();
    }

    private void setupTimeHandle() {
        if (generatorHologram.getTimeDisplay() == TimeDisplay.TICKS) {
            generatorTimeHandle = () -> generatorHologram.displayContent(name, item.getType(), currentInterval, generatorUpgrader.getGeneratorLevel());
        } else if (generatorHologram.getTimeDisplay() == TimeDisplay.MINUTES) {
            generatorTimeHandle = () -> {
                int elapsedTime = interval - currentInterval;
                if (elapsedTime % 1200 == 0) generatorHologram.displayContent(name, item.getType(), currentInterval, generatorUpgrader.getGeneratorLevel());
            };
        } else {
            generatorTimeHandle = () -> {
                int elapsedTime = interval - currentInterval;
                if (elapsedTime % 20 == 0) generatorHologram.displayContent(name, item.getType(), currentInterval, generatorUpgrader.getGeneratorLevel());
            };
        }
    }

    void tick() {
        if (currentInterval > 0) {
            currentInterval--;
            runningTime++;
        } else {
            perform();
            currentInterval = interval;
        }
        if (generatorHologram != null) generatorTimeHandle.process();
    }

    private void perform() {
        int nearbySimilarItems = location.getWorld().getNearbyEntitiesByType(Item.class, location, 2, (theEntity) -> theEntity.getItemStack().getType() == item.getType()).size();
        if (limit > 0 && nearbySimilarItems >= limit) return;
        location.getWorld().dropItemNaturally(location, item).setVelocity(new Vector(0, 0, 0));
    }
}
