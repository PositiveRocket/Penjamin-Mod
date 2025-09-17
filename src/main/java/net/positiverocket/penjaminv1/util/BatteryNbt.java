// util/BatteryNbt.java
package net.positiverocket.penjaminv1.util;

import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public final class BatteryNbt {
    public static final String KEY = "BatteryCharge";
    public static final int MAX = 40;

    private BatteryNbt() {}

    public static int get(ItemStack stack) {
        var tag = stack.getTag();
        if (tag == null || !tag.contains(KEY, Tag.TAG_INT)) return MAX; // default full
        int v = tag.getInt(KEY);
        return Math.max(0, Math.min(MAX, v));
    }

    public static void set(ItemStack stack, int value) {
        stack.getOrCreateTag().putInt(KEY, Math.max(0, Math.min(MAX, value)));
    }

    /** Returns false if not enough charge. */
    public static boolean tryConsume(ItemStack stack, int amount) {
        int cur = get(stack);
        if (cur < amount) return false;
        set(stack, cur - amount);
        return true;
    }

    public static void recharge(ItemStack stack, int amount) {
        set(stack, Math.min(MAX, get(stack) + amount));
    }

    public static boolean isDepleted(ItemStack stack) {
        return get(stack) <= 0;
    }
}
