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
import net.minecraft.world.item.Items;

import net.positiverocket.penjaminv1.Penjaminv1;
import net.positiverocket.penjaminv1.item.ModItems;

public final class CartInfusionCategory implements IRecipeCategory<CartInfusionCategory.Entry> {
    public static final RecipeType<Entry> TYPE =
            RecipeType.create(Penjaminv1.MODID, "cart_infusion", Entry.class);

    // layout constants
    private static final int SLOT = 18;
    private static final int PAD  = 8;
    private static final int W    = 156; // wider background
    private static final int H    = 48;

    private final IDrawable bg;
    private final IDrawable slotBg;
    private final IDrawable icon;
    private final Component title = Component.literal("Cart Infusion");

    public CartInfusionCategory(IGuiHelper gui) {
        this.bg     = gui.createBlankDrawable(W, H);
        this.icon   = gui.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Items.BREWING_STAND));
        this.slotBg = gui.getSlotDrawable();
    }

    @Override public RecipeType<Entry> getRecipeType() { return TYPE; }
    @Override public Component getTitle() { return title; }
    @Override public IDrawable getBackground() { return bg; }
    @Override public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder b, Entry r, IFocusGroup focuses) {
        int leftX  = PAD;
        int inY1   = PAD;
        int inY2   = PAD + SLOT + 2;
        int rightX = W - PAD - SLOT;
        int outY   = (H - SLOT) / 2;

        b.addSlot(RecipeIngredientRole.INPUT, leftX, inY1)
                .setBackground(slotBg, 0, 0)
                .addItemStack(new ItemStack(ModItems.EMPTY_CART.get()));

        b.addSlot(RecipeIngredientRole.INPUT, leftX, inY2)
                .setBackground(slotBg, 0, 0)
                .addItemStack(r.extract());

        b.addSlot(RecipeIngredientRole.OUTPUT, rightX, outY)
                .setBackground(slotBg, 0, 0)
                .addItemStack(r.result());
    }

    @Override
    public void draw(Entry recipe, IRecipeSlotsView slots, GuiGraphics g, double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        float scale = 2.0f;                 // bigger arrow
        int ax = (W / 2) - 4;               // center-ish
        int ay = (H / 2) - 4;

        // labels
        g.drawString(font, "Empty Cart", PAD + SLOT + 6, PAD + 4, 0xFFAAAAAA, false);
        g.drawString(font, "Extract",  PAD + SLOT + 6, PAD + SLOT + 8, 0xFFAAAAAA, false);

        g.pose().pushPose();
        g.pose().scale(scale, scale, 1f);
        g.drawString(font, "→", Math.round(ax / scale), Math.round(ay / scale), 0xFFA0A0A0, false);
        g.pose().popPose();
    }

    /** Simple data holder for JEI. */
    public static record Entry(ItemStack extract, ItemStack result) {}
}
