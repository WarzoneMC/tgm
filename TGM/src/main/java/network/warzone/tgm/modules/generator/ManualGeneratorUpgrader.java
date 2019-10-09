package network.warzone.tgm.modules.generator;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import network.warzone.tgm.parser.item.ItemDeserializer;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ManualGeneratorUpgrader extends GeneratorUpgrader {
    private Iterator<ManualGeneratorUpgrade> manualGeneratorUpgrades;
    private ManualGeneratorUpgrade upcomingUpgrade;

    public ManualGeneratorUpgrader(List<ManualGeneratorUpgrade> manualGeneratorUpgrades) {
        this.manualGeneratorUpgrades = manualGeneratorUpgrades.iterator();
        loadNextUpgrade();
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
            upcomingUpgrade = manualGeneratorUpgrades.next();
        } catch (Exception e) {
            upcomingUpgrade = null;
        }
    }

    public static ManualGeneratorUpgrader deserialize(JsonObject upgradeObject) {
        List<ManualGeneratorUpgrade> manualGeneratorUpgrades = new ArrayList<>();
        for (JsonElement manualSequenceElement : upgradeObject.get("sequence").getAsJsonArray()) {
            JsonObject sequenceObject = manualSequenceElement.getAsJsonObject();
            int interval = -1;
            if (sequenceObject.has("interval")) interval = sequenceObject.get("interval").getAsInt();
            ItemStack item = null;
            if (sequenceObject.has("item")) item = ItemDeserializer.parse(sequenceObject.get("item").getAsJsonObject());
            String message = null;
            if (sequenceObject.has("message")) message = sequenceObject.get("message").getAsString();
            manualGeneratorUpgrades.add(new ManualGeneratorUpgrade(interval, item, message));
        }
        return new ManualGeneratorUpgrader(manualGeneratorUpgrades);
    }
}
