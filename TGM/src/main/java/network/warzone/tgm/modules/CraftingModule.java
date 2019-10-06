package network.warzone.tgm.modules;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import network.warzone.tgm.TGM;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.util.Parser;
import network.warzone.tgm.util.Strings;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapelessRecipe;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Jorge on 10/06/2019
 */
public class CraftingModule extends MatchModule implements Listener {

    private List<Recipe> recipes = new ArrayList<>();
    private List<Material> removedRecipes = new ArrayList<>();

    @Override
    public void load(Match match) {
        this.removedRecipes.add(Material.COMPASS);
        boolean removeAll = false;
        JsonElement c = match.getMapContainer().getMapInfo().getJsonObject().get("crafting");
        if (c != null && c.isJsonObject()) {
            JsonObject crafting = c.getAsJsonObject();
            if (crafting.has("remove")) {
                if (crafting.get("remove").isJsonArray()) {
                    for (JsonElement jsonElement : crafting.getAsJsonArray("remove")) {
                        if (!jsonElement.isJsonPrimitive()) continue;
                        Material mat = Material.valueOf(jsonElement.getAsString());
                        if (mat == null) continue;
                        this.removedRecipes.add(mat);
                    }
                } else if (crafting.get("remove").isJsonPrimitive() && crafting.get("remove").getAsString().equals("*")) {
                    removeAll = true;
                }
            }
            if (crafting.has("recipes") && crafting.get("recipes").isJsonArray()) {
                for (JsonElement jsonElement : crafting.getAsJsonArray("recipes")) {
                    if (!jsonElement.isJsonObject()) continue;
                    Recipe recipe = parseRecipe(jsonElement.getAsJsonObject());
                    if (recipe != null) this.recipes.add(recipe);
                }
                this.recipes.forEach(Bukkit::addRecipe);
            }
        }
        if (removeAll) Bukkit.clearRecipes();
        else removeRecipes();
    }

    @Override
    public void unload() {
        Bukkit.resetRecipes();
    }

    private void removeRecipes() {
        List<Recipe> backup = new ArrayList<>();
        for (Material material : this.removedRecipes) {
            ItemStack item = new ItemStack(material);
            Iterator<Recipe> iterator = Bukkit.getServer().recipeIterator();

            while (iterator.hasNext()) {
                Recipe recipe = iterator.next();
                ItemStack result = recipe.getResult();
                if (!result.isSimilar(item)) {
                    backup.add(recipe);
                }
            }
        }

        Bukkit.getServer().clearRecipes();
        for (Recipe r : backup)
            Bukkit.getServer().addRecipe(r);
    }

    private static Recipe parseRecipe(JsonObject jsonObject) {
        String type = jsonObject.get("type").getAsString();
        ItemStack result = Parser.parseItemStack(jsonObject.get("result").getAsJsonObject());
        switch (type) {
            case "shapeless":
                ShapelessRecipe recipe = new ShapelessRecipe(getKey(result.getType().name() + new Date().getTime()), result);
                for (JsonElement element : jsonObject.getAsJsonArray("ingredients")) {
                    RecipeChoice ingredient = parseRecipeIngredient(element);
                    if (ingredient == null) continue;
                    recipe.addIngredient(ingredient);
                }
                return recipe;
            // TODO: All of these
            case "shaped":
            case "furnace":
            case "smoking":
            case "blasting":
            case "campfire":
            case "stonecutting":
            default:
                return null;
        }
    }

    private static RecipeChoice parseRecipeIngredient(JsonElement jsonElement) {
        if (jsonElement.isJsonObject()) {
            ItemStack item = Parser.parseItemStack(jsonElement.getAsJsonObject());
            return new RecipeChoice.ExactChoice(item);
        } else if (jsonElement.isJsonPrimitive()) {
            Material material = Material.valueOf(Strings.getTechnicalName(jsonElement.getAsString()));
            return new RecipeChoice.MaterialChoice(material);
        }
        return null;
    }

    // Will be moved to TGM class
    public static NamespacedKey getKey(String name) {
        return new NamespacedKey(TGM.get(), name);
    }

}
