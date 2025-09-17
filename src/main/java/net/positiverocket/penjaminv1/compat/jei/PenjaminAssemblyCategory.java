// src/main/java/net/positiverocket/penjaminv1/compat/jei/PenjaminAssemblyCategory.java
package net.positiverocket.penjaminv1.compat.jei;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import net.positiverocket.penjaminv1.Penjaminv1;
import net.positiverocket.penjaminv1.item.ModItems;

import java.util.List;

public final class PenjaminAssemblyCategory implements IRecipeCategory<PenjaminAssemblyCategory.Entry> {
    public static final RecipeType<Entry> TYPE =
            RecipeType.create(Penjaminv1.MODID, "penjamin_assembly", Entry.class);

    // layout
    private static final int SLOT = 18, PAD = 8, W = 176, H = 60;
    private final IDrawable bg, slotBg, icon;
    private final Component title = Component.literal("Assemble Penjamin");

    public PenjaminAssemblyCategory(IGuiHelper gui) {
        this.bg     = gui.createBlankDrawable(W, H);
        this.slotBg = gui.getSlotDrawable();
        this.icon   = gui.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModItems.BATTERY_WITH_CART.get()));
    }

    @Override public RecipeType<Entry> getRecipeType() { return TYPE; }
    @Override public Component getTitle() { return title; }
    @Override public IDrawable getBackground() { return bg; }
    @Override public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder b, Entry r, IFocusGroup f) {
        // Left column: Battery (main hand), Full Cart (off-hand)
        int leftX = PAD;
        b.addSlot(RecipeIngredientRole.INPUT, leftX, PAD)
                .setBackground(slotBg, 0, 0)
                .addItemStacks(r.batteries());

        b.addSlot(RecipeIngredientRole.INPUT, leftX, PAD + SLOT + 4)
                .setBackground(slotBg, 0, 0)
                .addItemStacks(r.fullCarts());

        // Right: output Penjamin
        int rightX = W - PAD - SLOT;
        int outY   = (H - SLOT) / 2;
        b.addSlot(RecipeIngredientRole.OUTPUT, rightX, outY)
                .setBackground(slotBg, 0, 0)
                .addItemStack(r.output());
    }

    @Override
    public void draw(Entry recipe, IRecipeSlotsView slots, GuiGraphics g, double mx, double my) {
        var font = Minecraft.getInstance().font;

        // labels
        g.drawString(font, "Main hand", PAD + SLOT + 6, PAD + 4, 0xFFAAAAAA, false);
        g.drawString(font, "Off-hand",  PAD + SLOT + 6, PAD + SLOT + 8, 0xFFAAAAAA, false);

        // big arrow
        float s = 2.2f;
        int ax = (W / 2) - 4, ay = (H / 2) - 5;
        g.pose().pushPose();
        g.pose().scale(s, s, 1f);
        g.drawString(font, "→", Math.round(ax / s), Math.round(ay / s), 0xFFA0A0A0, false);
        g.pose().popPose();

        // hint line

        g.drawString(font, "Right-click to assemble",
                PAD , H - 10, 0xFFB0B0B0, false);
    }

    /** Batteries + Full Carts → Penjamin output (for JEI display only). */
    public static record Entry(List<ItemStack> batteries, List<ItemStack> fullCarts, ItemStack output) {}
}
