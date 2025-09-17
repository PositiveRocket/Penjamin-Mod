package net.positiverocket.penjaminv1.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;

public class FullCartItem extends Item {
    public static final String NBT_STRAIN_ID = "StrainId"; // e.g. "smokeleafindustry:blue_ice_extract"

    public FullCartItem(Properties props) { super(props); }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        Item strainItem = resolveStrainItem(stack);
        if (strainItem != null) {
            java.util.List<MobEffectInstance> effects = getEffectsForExtract(strainItem);
            if (!effects.isEmpty()) {
                MutableComponent head = Component.literal("Effects: ").withStyle(ChatFormatting.GREEN);
                MutableComponent joined = Component.empty().withStyle(ChatFormatting.GRAY);

                for (int i = 0; i < effects.size(); i++) {
                    if (i > 0) joined.append(Component.literal(", ").withStyle(ChatFormatting.DARK_GRAY));
                    joined.append(effectToComponent(effects.get(i)));
                }

                tooltip.add(head.append(joined));
            }
            tooltip.add(
                    Component.literal("Extract")
                            .withStyle(ChatFormatting.GREEN)
                            .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                            .append(new ItemStack(strainItem).getHoverName().copy().withStyle(ChatFormatting.GRAY))
            );

        }
    }


    private static @Nullable Item resolveStrainItem(ItemStack stack) {
        String s = getStrainId(stack);
        if (s.isEmpty()) return null;
        ResourceLocation id = ResourceLocation.tryParse(s);
        return id != null ? ForgeRegistries.ITEMS.getValue(id) : null;
    }

    public static void setStrainId(ItemStack stack, ItemLike extract) {
        stack.getOrCreateTag().putString(NBT_STRAIN_ID, BuiltInRegistries.ITEM.getKey(extract.asItem()).toString());
    }
    public static String getStrainId(ItemStack stack) {
        return stack.hasTag() ? stack.getTag().getString(NBT_STRAIN_ID) : "";
    }

    // Map extract items -> base effects (same mapping you used on the pen)
    private static java.util.List<MobEffectInstance> getEffectsForExtract(Item extractItem) {
        String path = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(extractItem).getPath();

        return switch (path) {
            case "blue_ice_extract"   -> java.util.List.of(new MobEffectInstance(MobEffects.LUCK,            20 * 60, 0));
            case "bubblegum_extract"  -> java.util.List.of(new MobEffectInstance(MobEffects.LEVITATION,      20 * 3,  0));
            case "bubble_kush_extract"-> java.util.List.of(new MobEffectInstance(MobEffects.DIG_SPEED,       20 * 90, 0));
            case "lemon_haze_extract" -> java.util.List.of(new MobEffectInstance(MobEffects.JUMP,            20 * 60, 1));
            case "purple_haze_extract"-> java.util.List.of(new MobEffectInstance(MobEffects.NIGHT_VISION,    20 * 120, 0));
            case "sour_diesel_extract"-> java.util.List.of(
                    new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 90, 1),
                    new MobEffectInstance(MobEffects.DAMAGE_BOOST,   20 * 30, 0)
            );
            case "white_widow_extract"-> java.util.List.of(
                    new MobEffectInstance(MobEffects.REGENERATION,      20 * 20, 0),
                    new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 40, 0)
            );
            default -> java.util.List.of();
        };
    }

    private static String formatTicks(int ticks) {
        int totalSec = Math.max(0, ticks / 20);
        int m = totalSec / 60, s = totalSec % 60;
        return String.format("%d:%02d", m, s);
    }

    private static String ampToRoman(int amp) { // amp 0 => I
        int n = Math.max(0, amp) + 1;
        return switch (n) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> "I+" + (n - 1);
        };
    }

    private static MutableComponent effectToComponent(MobEffectInstance inst) {
        MutableComponent name = inst.getEffect().getDisplayName().copy();
        String amp = ampToRoman(inst.getAmplifier());
        String dur = formatTicks(inst.getDuration());
        return Component.empty()
                .append(name)
                .append(Component.literal(" " + amp + " (" + dur + ")")
                        .withStyle(ChatFormatting.GRAY));
    }

}
