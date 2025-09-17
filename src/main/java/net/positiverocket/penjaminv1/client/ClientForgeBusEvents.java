package net.positiverocket.penjaminv1.client;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

import net.positiverocket.penjaminv1.Penjaminv1;
import net.positiverocket.penjaminv1.item.ModItems;
import net.positiverocket.penjaminv1.util.ColorNbt;

@Mod.EventBusSubscriber(modid = Penjaminv1.MODID, value = Dist.CLIENT) // <-- FORGE bus (default)
public final class ClientForgeBusEvents {

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent e) {
        ItemStack st = e.getItemStack();

        // Only these items, and only if a colour is actually present
        if (!(st.is(ModItems.BATTERY.get()) || st.is(ModItems.BATTERY_WITH_CART.get()) || st.is(ModItems.FULL_CART.get())))
            return;
        if (!ColorNbt.has(st)) return; // <- prevents the Full Cart case you saw

        int rgb = ColorNbt.get(st);
        DyeColor nearest = nearestDye(rgb);
        String name = toTitle(nearest.getName().replace('_', ' '));

        boolean advanced = e.getFlags().isAdvanced(); // F3+H
        MutableComponent line = Component.literal("Colour: ")
                .withStyle(net.minecraft.ChatFormatting.GRAY)
                .append(Component.literal(name).withStyle(s -> s.withColor(rgb)));

        if (advanced) {
            line.append(Component.literal(" (" + String.format("#%06X", rgb) + ")")
                    .withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
        }

        // Remove old colour lines we may have added
        e.getToolTip().removeIf(c -> c.getString().startsWith("Colour:") || c.getString().startsWith("Color:"));

        // Find insert position:
        // default = right under the name (index 1)
        int insertAt = Math.min(1, e.getToolTip().size());
        // but if an "Extract:" line exists, put colour right *after* it
        for (int i = 0; i < e.getToolTip().size(); i++) {
            String s = e.getToolTip().get(i).getString();
            if (s.startsWith("Extract")) { insertAt = i + 1; break; }
        }


        e.getToolTip().add(insertAt, line);
    }




    private static DyeColor nearestDye(int rgb) {
        int r = (rgb >> 16) & 0xFF, g = (rgb >> 8) & 0xFF, b = rgb & 0xFF;
        DyeColor best = DyeColor.WHITE; long bestD = Long.MAX_VALUE;
        for (DyeColor d : DyeColor.values()) {
            int c = d.getTextColor();
            int dr = r - ((c >> 16) & 0xFF), dg = g - ((c >> 8) & 0xFF), db = b - (c & 0xFF);
            long d2 = (long)dr*dr + (long)dg*dg + (long)db*db;
            if (d2 < bestD) { bestD = d2; best = d; }
        }
        return best;
    }
    private static String toTitle(String s) {
        String[] p = s.split(" "); StringBuilder out = new StringBuilder();
        for (int i=0;i<p.length;i++){ if(p[i].isEmpty()) continue;
            out.append(Character.toUpperCase(p[i].charAt(0)));
            if (p[i].length()>1) out.append(p[i].substring(1));
            if (i+1<p.length) out.append(' ');
        }
        return out.toString();
    }
}
