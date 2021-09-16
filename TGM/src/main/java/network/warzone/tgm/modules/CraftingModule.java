package network.warzone.tgm.modules;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.parser.item.ItemDeserializer;
import network.warzone.tgm.util.KeyUtil;
import network.warzone.tgm.util.Strings;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Listener;
import org.bukkit.inventory.*;

import java.util.*;

/**
 * Created by Jorge on 10/06/2019
 */
public class CraftingModule extends MatchModule implements Listener {

    private List<Recipe> recipes = new ArrayList<>();
    private List<Material> removedRecipes = new ArrayList<>();

    @Override
    public void load(Match match) {
        boolean removeAll = false;
        JsonElement c = match.getMapContainer().getMapInfo().getJsonObject().get("crafting");
        if (c != null && c.isJsonObject()) {
            JsonObject crafting = c.getAsJsonObject();
            if (crafting.has("remove")) {
                if (crafting.get("remove").isJsonArray()) {
                    for (JsonElement jsonElement : crafting.getAsJsonArray("remove")) {
                        if (!jsonElement.isJsonPrimitive()) continue;
                        Material mat = Material.valueOf(Strings.getTechnicalName(jsonElement.getAsString()));
                        if (mat == null) continue;
                        this.removedRecipes.add(mat);
                    }
                } else if (crafting.get("remove").isJsonPrimitive() && "*".equals(crafting.get("remove").getAsString())) {
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
        else if (!this.removedRecipes.isEmpty()) removeRecipes();
    }

    @Override
    public void unload() {
        Bukkit.resetRecipes();
    }

    private void removeRecipes() {
        List<Recipe> backup = new ArrayList<>();
        for (Iterator<Recipe> it = Bukkit.recipeIterator(); it.hasNext();) {
            Recipe recipe = it.next();
            if (this.removedRecipes.contains(recipe.getResult().getType())) continue;
            backup.add(recipe);
        }
        Bukkit.getServer().clearRecipes();
        for (Recipe r : backup)
            Bukkit.getServer().addRecipe(r);
    }

    private static Recipe parseRecipe(JsonObject jsonObject) {
        String type = jsonObject.get("type").getAsString();
        ItemStack result = ItemDeserializer.parse(jsonObject.get("result"));
        NamespacedKey namespacedKey = KeyUtil.tgm(result.getType().name() + new Date().getTime());
        switch (type) {
            case "shapeless":
                ShapelessRecipe shapelessRecipe = new ShapelessRecipe(namespacedKey, result);
                for (JsonElement element : jsonObject.getAsJsonArray("ingredients")) {
                    RecipeChoice ingredient = parseRecipeIngredient(element);
                    if (ingredient == null) continue;
                    shapelessRecipe.addIngredient(ingredient);
                }
                return shapelessRecipe;
            case "shaped":
                ShapedRecipe shapedRecipe = new ShapedRecipe(namespacedKey, result);
                JsonArray shapeArray = jsonObject.getAsJsonArray("shape");
                String[] shape = new String[shapeArray.size()];
                for (int i = 0; i < shapeArray.size(); i++) {
                    shape[i] = shapeArray.get(i).getAsString();
                }
                shapedRecipe.shape(shape);
                for (Map.Entry<String, JsonElement> entry : jsonObject.getAsJsonObject("ingredients").entrySet()) {
                    try {
                        char key = entry.getKey().charAt(0);
                        RecipeChoice ingredient = parseRecipeIngredient(entry.getValue());
                        if (ingredient == null) continue;
                        shapedRecipe.setIngredient(key, ingredient);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return shapedRecipe;
            case "furnace":
                RecipeChoice furnaceIngredient = parseRecipeIngredient(jsonObject.get("ingredient"));
                if (furnaceIngredient == null) return null;
                int furnaceExp = 0;
                int furnaceCookingTime = 200;
                if (jsonObject.has("experience")) furnaceExp = jsonObject.get("experience").getAsInt();
                if (jsonObject.has("cookingTime")) furnaceCookingTime = jsonObject.get("cookingTime").getAsInt();
                return new FurnaceRecipe(namespacedKey, result, furnaceIngredient, furnaceExp, furnaceCookingTime);
            case "smoking":
                RecipeChoice smokingIngredient = parseRecipeIngredient(jsonObject.get("ingredient"));
                if (smokingIngredient == null) return null;
                int smokingExp = 0;
                int smokingCookingTime = 100;
                if (jsonObject.has("experience")) smokingExp = jsonObject.get("experience").getAsInt();
                if (jsonObject.has("cookingTime")) smokingCookingTime = jsonObject.get("cookingTime").getAsInt();
                return new SmokingRecipe(namespacedKey, result, smokingIngredient, smokingExp, smokingCookingTime);
            case "blasting":
                RecipeChoice blastingIngredient = parseRecipeIngredient(jsonObject.get("ingredient"));
                if (blastingIngredient == null) return null;
                int blastingExp = 0;
                int blastingCookingTime = 100;
                if (jsonObject.has("experience")) blastingExp = jsonObject.get("experience").getAsInt();
                if (jsonObject.has("cookingTime")) blastingCookingTime = jsonObject.get("cookingTime").getAsInt();
                return new BlastingRecipe(namespacedKey, result, blastingIngredient, blastingExp, blastingCookingTime);
            case "campfire":
                RecipeChoice campfireIngredient = parseRecipeIngredient(jsonObject.get("ingredient"));
                if (campfireIngredient == null) return null;
                int campfireExp = 0;
                int campfireCookingTime = 600;
                if (jsonObject.has("experience")) campfireExp = jsonObject.get("experience").getAsInt();
                if (jsonObject.has("cookingTime")) campfireCookingTime = jsonObject.get("cookingTime").getAsInt();
                return new CampfireRecipe(namespacedKey, result, campfireIngredient, campfireExp, campfireCookingTime);
            case "stonecutting":
                RecipeChoice stonecuttingIngredient = parseRecipeIngredient(jsonObject.get("ingredient"));
                if (stonecuttingIngredient == null) return null;
                return new StonecuttingRecipe(namespacedKey, result, stonecuttingIngredient);
            default:
                return null;
        }
    }

    private static RecipeChoice parseRecipeIngredient(JsonElement jsonElement) {
        if (jsonElement.isJsonObject()) {
            ItemStack item = ItemDeserializer.parse(jsonElement);
            return new RecipeChoice.ExactChoice(item);
        } else if (jsonElement.isJsonPrimitive()) {
            Material material = Material.valueOf(Strings.getTechnicalName(jsonElement.getAsString()));
            return new RecipeChoice.MaterialChoice(material);
        }
        return null;
    }

    public void addRemovedRecipe(Material material) {
        if (!this.removedRecipes.contains(material)) {
            this.removedRecipes.add(material);
            this.removeRecipes();
        }
    }

}
