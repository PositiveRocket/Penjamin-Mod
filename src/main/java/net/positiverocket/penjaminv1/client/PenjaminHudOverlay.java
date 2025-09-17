package net.positiverocket.penjaminv1.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.positiverocket.penjaminv1.item.custom.PenjaminItem;

import java.util.Locale;

@Mod.EventBusSubscriber(modid = "penjaminv1", value = Dist.CLIENT)
public final class PenjaminHudOverlay {

    @SubscribeEvent
    public static void onRenderLast(RenderGuiOverlayEvent.Post event) {
        // Run after *all* HUD bits so our text isn't covered
        if (!event.getOverlay().id().equals(VanillaGuiOverlay.EXPERIENCE_BAR.id())) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        // Only when actively using PenjaminItem
        ItemStack using = player.getUseItem();
        if (using.isEmpty() || !(using.getItem() instanceof PenjaminItem)) return;
        int remaining = player.getUseItemRemainingTicks();
        if (remaining <= 0) return;

        int heldTicks = using.getUseDuration() - remaining;
        String text = String.format(java.util.Locale.ROOT, "%.1fs", heldTicks / 20f);

        GuiGraphics gg = event.getGuiGraphics();
        int sw = event.getWindow().getGuiScaledWidth();
        int sh = event.getWindow().getGuiScaledHeight();

        // Hotbar geometry
        int hotbarLeft = sw / 2 - 91;
        int hotbarTop  = sh - 22;
        int slot = player.getInventory().selected;

        // Item icon position inside slot (3,3) with 16x16 icon
        int iconX = hotbarLeft + 3 + slot * 20;
        int iconY = hotbarTop  + 3;

        // Draw directly on the icon: small z-push so we’re above everything
        gg.pose().pushPose();
        gg.pose().translate(0, 0, 200); // render on top

        // Optional: tiny dark label background sized to text
        int tw = mc.font.width(text);
        int x = iconX + 8 - tw / 2; // center over icon
        int y = iconY + 1;          // near top of icon
        gg.fill(x - 2, y - 2, x + tw + 2, y + 9, 0xAA000000);

        gg.drawString(mc.font, text, x, y, 0xFFFFFF, true);
        gg.pose().popPose();
    }

}
