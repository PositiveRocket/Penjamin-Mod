package net.positiverocket.penjaminv1.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistries;
import net.positiverocket.penjaminv1.Penjaminv1;
import net.positiverocket.penjaminv1.item.ModItems;
import net.positiverocket.penjaminv1.util.ColorNbt;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class PenjaminRecipeProvider extends RecipeProvider {
    // flip to false if you don't want recipe-book unlocks generated
    private static final boolean GENERATE_ADVANCEMENTS = true;

    public PenjaminRecipeProvider(PackOutput output) { super(output); }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> out) {
        for (DyeColor dc : DyeColor.values()) {
            String name = dc.getName(); // e.g. "orange"
            ResourceLocation id = new ResourceLocation(Penjaminv1.MODID, "battery_dyed/battery_" + name);

            // forge:dyes/<color> so modded dyes work
            TagKey<Item> dyeTag = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "dyes/" + name));
            int color = dc.getTextColor();

            // pattern " I "/" R "/" D "
            List<String> pattern = List.of(" I ", " R ", " D ");
            Map<Character, Ingredient> i = new LinkedHashMap<>();
            i.put('I', Ingredient.of(Items.IRON_INGOT));
            i.put('R', Ingredient.of(Items.REDSTONE_BLOCK));
            i.put('D', Ingredient.of(dyeTag));

            ResourceLocation resultItem = ForgeRegistries.ITEMS.getKey(ModItems.BATTERY.get());

            // Use the exact key your mod reads:
            String key = ColorNbt.KEY; // this is "Colour" in your mod
            String nbt = "{display:{color:" + (color & 0xFFFFFF) + "}}";



            ResourceLocation advId = new ResourceLocation(Penjaminv1.MODID, "recipes/battery_dyed/battery_" + name);

            out.accept(new ShapedWithNbt(id, pattern, i, resultItem, 1, nbt,
                    GENERATE_ADVANCEMENTS ? advId : null));
        }
    }

    /** Writes a vanilla shaped recipe with 'result.nbt' and (optionally) an advancement. */
    private static final class ShapedWithNbt implements FinishedRecipe {
        private final ResourceLocation id;
        private final List<String> pattern;
        private final Map<Character, Ingredient> key;
        private final ResourceLocation result;
        private final int count;
        private final String nbtSnbt;
        private final ResourceLocation advIdOrNull;

        ShapedWithNbt(ResourceLocation id, List<String> pattern, Map<Character, Ingredient> key,
                      ResourceLocation result, int count, String nbtSnbt, ResourceLocation advIdOrNull) {
            this.id = id;
            this.pattern = pattern;
            this.key = key;
            this.result = result;
            this.count = count;
            this.nbtSnbt = nbtSnbt;
            this.advIdOrNull = advIdOrNull;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            json.addProperty("type", "minecraft:crafting_shaped");
            json.addProperty("category", "misc");

            JsonArray pat = new JsonArray();
            for (String row : pattern) pat.add(row);
            json.add("pattern", pat);

            JsonObject keyObj = new JsonObject();
            for (Map.Entry<Character, Ingredient> e : key.entrySet()) {
                keyObj.add(String.valueOf(e.getKey()), e.getValue().toJson());
            }
            json.add("key", keyObj);

            JsonObject resultObj = new JsonObject();
            resultObj.addProperty("item", result.toString());
            if (count != 1) resultObj.addProperty("count", count);
            resultObj.addProperty("nbt", nbtSnbt); // <- SNBT string is the important bit
            json.add("result", resultObj);
        }

        @Override public ResourceLocation getId() { return id; }
        @Override public RecipeSerializer<?> getType() { return RecipeSerializer.SHAPED_RECIPE; }

        // ====== Advancements (optional but needed for vanilla recipe book) ======
        @Override
        public JsonObject serializeAdvancement() {
            if (advIdOrNull == null) return null;

            // Build an advancement JSON by hand (no ambiguous hasItems() calls)
            // Unlock when you obtain ANY of iron OR redstone block OR the matching dye tag.
            JsonObject adv = new JsonObject();
            adv.addProperty("parent", "minecraft:recipes/root");

            JsonObject criteria = new JsonObject();

            criteria.add("has_iron", invChangedItems(List.of(itemObj("minecraft:iron_ingot"))));
            criteria.add("has_redstone_block", invChangedItems(List.of(itemObj("minecraft:redstone_block"))));

            // derive dye name (last path segment after underscore)
            String dyeName = this.id.getPath().substring(this.id.getPath().lastIndexOf('_') + 1);
            criteria.add("has_dye", invChangedItems(List.of(tagObj("forge:dyes/" + dyeName))));

            adv.add("criteria", criteria);

            // requirements: any ONE of the criteria
            JsonArray any = new JsonArray();
            any.add("has_iron");
            any.add("has_redstone_block");
            any.add("has_dye");
            JsonArray reqs = new JsonArray();
            reqs.add(any);
            adv.add("requirements", reqs);

            // rewards: grant the recipe
            JsonObject rewards = new JsonObject();
            JsonArray recipes = new JsonArray();
            recipes.add(this.id.toString());
            rewards.add("recipes", recipes);
            adv.add("rewards", rewards);

            return adv;
        }

        @Override public ResourceLocation getAdvancementId() { return advIdOrNull; }

        // helpers to craft small advancement JSON nodes
        private static JsonObject invChangedItems(List<JsonObject> items) {
            JsonObject obj = new JsonObject();
            obj.addProperty("trigger", "minecraft:inventory_changed");
            JsonObject cond = new JsonObject();
            JsonArray arr = new JsonArray();
            for (JsonObject it : items) arr.add(it);
            cond.add("items", arr);
            obj.add("conditions", cond);
            return obj;
        }
        private static JsonObject itemObj(String id) {
            JsonObject o = new JsonObject();
            o.addProperty("item", id);
            return o;
        }
        private static JsonObject tagObj(String tag) {
            JsonObject o = new JsonObject();
            o.addProperty("tag", tag);
            return o;
        }
    }
}
