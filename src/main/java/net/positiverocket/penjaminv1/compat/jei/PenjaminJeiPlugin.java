// src/main/java/net/positiverocket/penjaminv1/compat/jei/PenjaminJeiPlugin.java
package net.positiverocket.penjaminv1.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

import net.positiverocket.penjaminv1.Penjaminv1;
import net.positiverocket.penjaminv1.item.ModItems;
import net.positiverocket.penjaminv1.item.custom.FullCartItem;
import net.positiverocket.penjaminv1.util.ColorNbt;

@JeiPlugin
public final class PenjaminJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID =
            new ResourceLocation(Penjaminv1.MODID, "jei_plugin");

    // Extracts tag -> “Empty Cart + Extract = Full Cart”
    private static final TagKey<Item> EXTRACTS_TAG =
            TagKey.create(Registries.ITEM, new ResourceLocation(Penjaminv1.MODID, "smokeleaf_extracts"));

    @Override
    public ResourceLocation getPluginUid() { return UID; }


    // --- Subtypes so NBT variants don't collapse ---
    @Override
    public void registerItemSubtypes(ISubtypeRegistration reg) {
        IIngredientSubtypeInterpreter<ItemStack> byFullTag = (ItemStack stack, UidContext ctx) -> {
            CompoundTag t = stack.getTag();
            if (t == null || t.isEmpty()) return IIngredientSubtypeInterpreter.NONE;
            // strip volatile stuff if any
            t = t.copy();
            t.remove("Damage");
            t.remove("RepairCost");
            return t.toString();
        };

        reg.registerSubtypeInterpreter(ModItems.BATTERY.get(), byFullTag);
        reg.registerSubtypeInterpreter(ModItems.EMPTY_CART.get(), byFullTag);
        reg.registerSubtypeInterpreter(ModItems.FULL_CART.get(), byFullTag);
        reg.registerSubtypeInterpreter(ModItems.BATTERY_WITH_CART.get(), (ItemStack stack, UidContext ctx) -> {
            if (ctx == UidContext.Ingredient) {
                CompoundTag t = stack.getTag();
                if (t == null || t.isEmpty()) return IIngredientSubtypeInterpreter.NONE;
                t = t.copy();
                t.remove("Damage");
                t.remove("RepairCost");
                return t.toString(); // show each NBT variant separately in JEI list
            }
            // For recipe lookups (outputs), treat all Penjamins as the same item so R matches your category
            return IIngredientSubtypeInterpreter.NONE;
        });

    }

    // --- Category: Cart Infusion (brewing stand) ---
    @Override
    public void registerCategories(IRecipeCategoryRegistration reg) {
        reg.addRecipeCategories(new CartInfusionCategory(reg.getJeiHelpers().getGuiHelper()));
        reg.addRecipeCategories(new PenjaminAssemblyCategory(reg.getJeiHelpers().getGuiHelper()));
    }

    // Build recipes without accessing the world; use JEI's ingredient list
    @Override
    public void registerRecipes(IRecipeRegistration reg) {
        IIngredientManager mgr = reg.getIngredientManager();
        List<CartInfusionCategory.Entry> entries = new ArrayList<>();

        for (ItemStack s : mgr.getAllIngredients(VanillaTypes.ITEM_STACK)) {
            if (!s.isEmpty() && s.is(EXTRACTS_TAG)) {
                ItemStack extract = new ItemStack(s.getItem());
                ItemStack out = new ItemStack(ModItems.FULL_CART.get());
                FullCartItem.setStrainId(out, s.getItem());
                entries.add(new CartInfusionCategory.Entry(extract, out));
            }
        }

        reg.addRecipes(CartInfusionCategory.TYPE, entries);

        // All colored batteries
        var batteries = new ArrayList<ItemStack>();
        for (DyeColor dc : DyeColor.values()) {
            ItemStack s = new ItemStack(ModItems.BATTERY.get());
            ColorNbt.set(s, dc.getTextColor());
            batteries.add(s);
        }

        // All full carts (one per extract strain)
        var fullCarts = new ArrayList<ItemStack>();
        for (ItemStack st : mgr.getAllIngredients(VanillaTypes.ITEM_STACK)) {
            if (!st.isEmpty() && st.is(EXTRACTS_TAG)) {
                ItemStack fc = new ItemStack(ModItems.FULL_CART.get());
                FullCartItem.setStrainId(fc, st.getItem());
                fullCarts.add(fc);
            }
        }

        // Output: a representative Penjamin (no NBT so it matches any, since we removed its subtype)
        ItemStack out = new ItemStack(ModItems.BATTERY_WITH_CART.get());

        reg.addRecipes(PenjaminAssemblyCategory.TYPE,
                java.util.List.of(new PenjaminAssemblyCategory.Entry(batteries, fullCarts, new ItemStack(ModItems.BATTERY_WITH_CART.get()))));

    }

    // Brewing stand filters to this category
    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration reg) {
        reg.addRecipeCatalyst(new ItemStack(Items.BREWING_STAND), CartInfusionCategory.TYPE);
    }

    // --- Add colored batteries to the ingredient list so they show up individually ---
    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        IIngredientManager mgr = runtime.getIngredientManager();
        List<ItemStack> stacks = new ArrayList<>();

        for (DyeColor dc : DyeColor.values()) {
            ItemStack s = new ItemStack(ModItems.BATTERY.get());
            ColorNbt.set(s, dc.getTextColor());
            stacks.add(s);
        }

        if (!stacks.isEmpty()) {
            mgr.addIngredientsAtRuntime(VanillaTypes.ITEM_STACK, stacks);
        }
    }


}
