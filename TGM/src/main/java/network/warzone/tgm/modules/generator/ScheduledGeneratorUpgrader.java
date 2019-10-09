package network.warzone.tgm.modules.generator;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import network.warzone.tgm.TGM;
import network.warzone.tgm.parser.item.ItemDeserializer;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ScheduledGeneratorUpgrader extends GeneratorUpgrader {
    private Iterator<ScheduledGeneratorUpgrade> scheduledGeneratorUpgradeIterator;
    private ScheduledGeneratorUpgrade upcomingUpgrade;

    private int runnableID = -1;

    public ScheduledGeneratorUpgrader(List<ScheduledGeneratorUpgrade> scheduledGeneratorUpgrades) {
        this.scheduledGeneratorUpgradeIterator = scheduledGeneratorUpgrades.iterator();
        loadNextUpgrade();
    }

    @Override
    public void enable() {
        this.runnableID = Bukkit.getScheduler().scheduleSyncRepeatingTask(TGM.get(), () -> {
            if (hostGenerator == null || upcomingUpgrade == null) return;
            if (hostGenerator.getRunningTime() == upcomingUpgrade.getTime()) upgrade();
        }, 0L, 0L);
    }

    @Override
    public void upgrade() {
        generatorLevel++;
        applyUpgrade();
        loadNextUpgrade();
        hostGenerator.resetTimer();
    }

    private void applyUpgrade() {
        if (upcomingUpgrade.getItem() != null) hostGenerator.setItem(upcomingUpgrade.getItem());
        if (upcomingUpgrade.getInterval() > 0) hostGenerator.setInterval(upcomingUpgrade.getInterval());
        if (upcomingUpgrade.getBroadcast() != null) Bukkit.broadcastMessage(parseCurrentBroadcast(upcomingUpgrade.getBroadcast()));
    }

    private void loadNextUpgrade() {
        try {
            upcomingUpgrade = scheduledGeneratorUpgradeIterator.next();
        } catch (Exception e) {
            upcomingUpgrade = null;
        }
        if (upcomingUpgrade == null) unload();
    }

    @Override
    void unload() {
        if (runnableID > 0) {
            Bukkit.getScheduler().cancelTask(this.runnableID);
            runnableID = -1;
        }
    }

    public static ScheduledGeneratorUpgrader deserialize(JsonObject upgradeObject) {
        List<ScheduledGeneratorUpgrade> scheduledGeneratorUpgrades = new ArrayList<>();
        for (JsonElement upgradeSequenceElement : upgradeObject.get("sequence").getAsJsonArray()) {
            JsonObject sequenceObject = upgradeSequenceElement.getAsJsonObject();
            if (!sequenceObject.has("time")) continue;
            int time = sequenceObject.get("time").getAsInt();
            int interval = -1;
            if (sequenceObject.has("interval")) interval = sequenceObject.get("interval").getAsInt();
            ItemStack item = null;
            if (sequenceObject.has("item")) item = ItemDeserializer.parse(sequenceObject.get("item").getAsJsonObject());
            String message = null;
            if (sequenceObject.has("message")) message = sequenceObject.get("message").getAsString();
            scheduledGeneratorUpgrades.add(new ScheduledGeneratorUpgrade(time, interval, item, message));
        }
        return new ScheduledGeneratorUpgrader(scheduledGeneratorUpgrades);
    }
}
