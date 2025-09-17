package net.positiverocket.penjaminv1.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public final class ColorNbt {
    private static final String TAG_ROOT = "display";
    private static final String TAG_COLOR = "color";
    public static final int DEFAULT = 0x5AC8FA; // pick your default
    public static final String KEY = "Color";

    public static int get(ItemStack s) {
        CompoundTag t = s.getTagElement(TAG_ROOT);
        return (t != null && t.contains(TAG_COLOR, 3)) ? t.getInt(TAG_COLOR) : DEFAULT;
    }
    public static void set(ItemStack s, int rgb) {
        s.getOrCreateTagElement(TAG_ROOT).putInt(TAG_COLOR, rgb & 0xFFFFFF);
    }

    public static boolean has(ItemStack stack) {
        CompoundTag tag = stack.getTagElement("display");
        return tag != null && tag.contains("color", 3);
    }
    public static void copy(ItemStack from, ItemStack to) {
        if (from.hasTag() && from.getTag().contains(KEY, net.minecraft.nbt.Tag.TAG_INT)) {
            set(to, get(from));
        }
    }

}
