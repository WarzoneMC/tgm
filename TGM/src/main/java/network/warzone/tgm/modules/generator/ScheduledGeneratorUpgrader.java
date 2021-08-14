package network.warzone.tgm.modules.generator;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import network.warzone.tgm.TGM;
import network.warzone.tgm.parser.item.ItemDeserializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScheduledGeneratorUpgrader extends GeneratorUpgrader {
    private List<ScheduledGeneratorUpgrade> scheduledGeneratorUpgrades;
    private HashMap<Integer, Integer> taskMap = new HashMap<>();

    public ScheduledGeneratorUpgrader(List<ScheduledGeneratorUpgrade> scheduledGeneratorUpgrades) {
        this.scheduledGeneratorUpgrades = scheduledGeneratorUpgrades;
    }

    @Override
    public void enable() {
        for (ScheduledGeneratorUpgrade scheduledGeneratorUpgrade : scheduledGeneratorUpgrades) {
            if (taskMap.containsKey(scheduledGeneratorUpgrade.getTime())) continue;
            taskMap.put(scheduledGeneratorUpgrade.getTime(),
                    Bukkit.getScheduler().runTaskLater(TGM.get(), () -> {
                        upgrade(scheduledGeneratorUpgrade);
                    }, scheduledGeneratorUpgrade.getTime()).getTaskId()
            );
        }
    }

    private void upgrade(ScheduledGeneratorUpgrade scheduledGeneratorUpgrade) {
        generatorLevel++;
        applyUpgrade(scheduledGeneratorUpgrade);
        hostGenerator.resetTimer();
    }

    private void applyUpgrade(ScheduledGeneratorUpgrade scheduledGeneratorUpgrade) {
        if (scheduledGeneratorUpgrade.getItem() != null) hostGenerator.setItem(scheduledGeneratorUpgrade.getItem());
        if (scheduledGeneratorUpgrade.getInterval() > 0) hostGenerator.setInterval(scheduledGeneratorUpgrade.getInterval());
        if (scheduledGeneratorUpgrade.getBroadcast() != null) Bukkit.broadcastMessage(parseCurrentBroadcast(scheduledGeneratorUpgrade.getBroadcast()));
        if (scheduledGeneratorUpgrade.getHoloContent() != null && hostGenerator.getGeneratorHologram() != null) hostGenerator.getGeneratorHologram().setBaseContent(scheduledGeneratorUpgrade.getHoloContent());
    }

    @Override
    protected void unload() {
        int timeRan = hostGenerator.getRunningTime();
        for (Map.Entry<Integer, Integer> mapEntry : taskMap.entrySet()) {
            if (mapEntry.getKey() < timeRan) continue;
            Bukkit.getScheduler().cancelTask(mapEntry.getValue());
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
            if (sequenceObject.has("message")) message = ChatColor.translateAlternateColorCodes('&', sequenceObject.get("message").getAsString());
            String holoContent = null;
            if (sequenceObject.has("holoContent")) holoContent = ChatColor.translateAlternateColorCodes('&', sequenceObject.get("holoContent").getAsString());
            scheduledGeneratorUpgrades.add(new ScheduledGeneratorUpgrade(time, interval, item, message, holoContent));
        }
        return new ScheduledGeneratorUpgrader(scheduledGeneratorUpgrades);
    }
}
