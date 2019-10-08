package network.warzone.tgm.modules.generator;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.modules.tasked.TaskedModule;
import network.warzone.tgm.parser.item.ItemDeserializer;
import network.warzone.tgm.util.Parser;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GeneratorModule extends MatchModule implements TaskedModule {
    private List<Generator> generators = new ArrayList<>();
    private boolean matchStarted = false;

    public static boolean hasGenerators(JsonObject mapJson) {
        return (mapJson.has("generators") && mapJson.get("generators").getAsJsonArray().size() > 0);
    }

    @Override
    public void load(Match match) {
        World matchWorld = match.getWorld();
        for (JsonElement generatorElement : match.getMapContainer().getMapInfo().getJsonObject().get("generators").getAsJsonArray()) {
            Generator parsedGenerator = deserializeGenerator(matchWorld, generatorElement.getAsJsonObject());
            generators.add(parsedGenerator);
        }
    }

    @Override
    public void enable() {
        matchStarted = true;
    }

    @Override
    public void tick() {
        if (!matchStarted) return;
        for (Generator generator : generators) generator.tick();
    }

    private static Generator deserializeGenerator(World matchWorld, JsonObject generatorObject) {
        assert generatorObject.has("item") : "Generator needs an item specified";
        assert generatorObject.has("location") : "Generator needs a location specified";
        assert generatorObject.has("interval") : "Generator needs a spawnRate specified";
        String generatorName = ChatColor.YELLOW + "Generator";
        if (generatorObject.has("name")) generatorName = ChatColor.translateAlternateColorCodes('&', generatorObject.get("name").getAsString());
        ItemStack generatorItem = ItemDeserializer.parse(generatorObject.get("item").getAsJsonObject());
        Location generatorLocation = Parser.convertLocation(matchWorld, generatorObject.get("location"));
        int generatorLimit = 0;
        if (generatorObject.has("limit")) generatorLimit = generatorObject.get("limit").getAsInt();
        int generatorInterval = generatorObject.get("interval").getAsInt();
        GeneratorHologram generatorHologram = null;
        if (generatorObject.has("hologram")) generatorHologram = GeneratorHologram.deserialize(generatorObject.get("hologram").getAsJsonObject(), generatorLocation);
        GeneratorUpgrader generatorUpgrader = null;
        if (generatorObject.has("upgrades") && generatorObject.get("upgrades").getAsJsonObject().has("sequence") && generatorObject.get("upgrades").getAsJsonObject().get("sequence").getAsJsonArray().size() > 0) {
            JsonObject upgradeObject = generatorObject.get("upgrades").getAsJsonObject();
            String upgradeType = "manual";
            if (upgradeObject.has("type")) upgradeType = upgradeObject.get("type").getAsString();

            if (upgradeType.equalsIgnoreCase("scheduled")) generatorUpgrader = ScheduledGeneratorUpgrader.deserialize(upgradeObject);
            else generatorUpgrader = ManualGeneratorUpgrader.deserialize(upgradeObject);

        }
        return new Generator(generatorName, generatorItem, generatorLocation, generatorLimit, generatorInterval, generatorHologram, generatorUpgrader);
    }

    @Override
    public void unload() {
        for (Generator generator : generators) {
            generator.getGeneratorUpgrader().unload();
        }
    }

}
