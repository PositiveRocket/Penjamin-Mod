package net.positiverocket.penjaminv1.recipe;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.positiverocket.penjaminv1.Penjaminv1;
import net.positiverocket.penjaminv1.item.ModItems;
import net.positiverocket.penjaminv1.item.custom.FullCartItem;

public class CartInfusionBrewingRecipe implements IBrewingRecipe {
    private final Item input = ModItems.EMPTY_CART.get();
    private final TagKey<Item> extractTag = TagKey.create(Registries.ITEM, new ResourceLocation(Penjaminv1.MODID, "smokeleaf_extracts"));

    @Override
    public boolean isInput(ItemStack inputStack) {
        return inputStack.getItem() == input;
    }

    @Override
    public boolean isIngredient(ItemStack ingredient) {
        return ingredient.is(extractTag);
    }

    @Override
    public ItemStack getOutput(ItemStack inputStack, ItemStack ingredient) {
        if (!isInput(inputStack) || !isIngredient(ingredient)) return ItemStack.EMPTY;
        ItemStack out = new ItemStack(ModItems.FULL_CART.get());
        FullCartItem.setStrainId(out, ingredient.getItem());

        return out;
    }
}
