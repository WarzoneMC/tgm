package network.warzone.tgm.modules.generator;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

@Getter
public class Generator {
    private String id;
    @Setter private ItemStack item;
    private Location location;
    private int limit;
    private int range;
    @Setter private int interval;
    private GeneratorHologram generatorHologram;
    private GeneratorUpgrader generatorUpgrader;

    private int currentInterval;
    private int runningTime = 0;
    private GeneratorTimeHandle generatorTimeHandle;

    public Generator(String id, ItemStack item, Location location, int limit, int range, int interval, GeneratorHologram generatorHologram, GeneratorUpgrader generatorUpgrader) {
        this.id = id;
        this.item = item;
        this.location = location;
        this.limit = limit;
        this.range = range;
        this.interval = interval;
        this.currentInterval = interval;
        this.generatorHologram = generatorHologram;
        this.generatorUpgrader = generatorUpgrader;

        if (this.generatorUpgrader != null) this.generatorUpgrader.setHostGenerator(this);
        if (this.generatorHologram != null) setupTimeHandle();
    }

    public void enable() {
        if (this.generatorHologram != null) {
            generatorHologram.makeVisible();
            generatorHologram.displayContent(item.getType(), currentInterval, generatorUpgrader.getGeneratorLevel());
        }
        if (this.generatorUpgrader != null) generatorUpgrader.enable();
    }

    private void setupTimeHandle() {
        if (generatorHologram.getTimeDisplay() == TimeDisplay.TICKS) {
            generatorTimeHandle = () -> generatorHologram.displayContent(item.getType(), currentInterval, generatorUpgrader.getGeneratorLevel());
        } else if (generatorHologram.getTimeDisplay() == TimeDisplay.MINUTES) {
            generatorTimeHandle = () -> {
                int elapsedTime = interval - currentInterval;
                if (elapsedTime % 1200 == 0) generatorHologram.displayContent(item.getType(), currentInterval, generatorUpgrader.getGeneratorLevel());
            };
        } else {
            generatorTimeHandle = () -> {
                int elapsedTime = interval - currentInterval;
                if (elapsedTime % 20 == 0) generatorHologram.displayContent(item.getType(), currentInterval, generatorUpgrader.getGeneratorLevel());
            };
        }
    }

    protected void tick() {
        if (currentInterval > 0) {
            currentInterval--;
            runningTime++;
        } else {
            perform();
            resetTimer();
        }
        if (generatorHologram != null) generatorTimeHandle.process();
    }

    protected void resetTimer() {
        currentInterval = interval;
    }

    private void perform() {
        if (range >= 0 && location.getWorld().getNearbyPlayers(location, range).size() == 0) return;
        if (limit > 0) {
            int nearbySimilarItems = location.getWorld().getNearbyEntitiesByType(Item.class, location, 2, (theEntity) -> theEntity.getItemStack().getType() == item.getType()).stream().mapToInt((itemEntity) -> itemEntity.getItemStack().getAmount()).sum();
            if (nearbySimilarItems >= limit) return;
        }
        location.getWorld().dropItemNaturally(location, item).setVelocity(new Vector(0, 0, 0));
    }
}
