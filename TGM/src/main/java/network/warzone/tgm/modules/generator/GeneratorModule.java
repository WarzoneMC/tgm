package network.warzone.tgm.modules.generator;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.modules.tasked.TaskedModule;
import network.warzone.tgm.parser.item.ItemDeserializer;
import network.warzone.tgm.util.Parser;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yikes on 10/12/19.
 */
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
        for (Generator generator : generators) generator.enable();
    }

    @Override
    public void tick() {
        if (!matchStarted) return;
        for (Generator generator : generators) generator.tick();
    }

    private static Generator deserializeGenerator(World matchWorld, JsonObject generatorObject) {
        assert generatorObject.has("id") : "Generator needs an ID specified";
        assert generatorObject.has("item") : "Generator needs an item specified";
        assert generatorObject.has("location") : "Generator needs a location specified";
        assert generatorObject.has("interval") : "Generator needs a spawnRate specified";
        String generatorID = generatorObject.get("id").getAsString();
        ItemStack generatorItem = ItemDeserializer.parse(generatorObject.get("item").getAsJsonObject());
        Location generatorLocation = Parser.convertLocation(matchWorld, generatorObject.get("location"));
        int generatorLimit = 0;
        if (generatorObject.has("limit")) generatorLimit = generatorObject.get("limit").getAsInt();
        int generatorRange = -1;
        if (generatorObject.has("range")) generatorRange = generatorObject.get("range").getAsInt();
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
        return new Generator(generatorID, generatorItem, generatorLocation, generatorLimit, generatorRange, generatorInterval, generatorHologram, generatorUpgrader);
    }

    @Override
    public void disable() {
        for (Generator generator : generators) {
            if (generator.getGeneratorUpgrader() != null) {
                generator.getGeneratorUpgrader().unload();
            }
        }
        matchStarted = false;
        generators = null;
    }

}
