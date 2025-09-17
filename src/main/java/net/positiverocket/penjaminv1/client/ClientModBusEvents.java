package net.positiverocket.penjaminv1.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.positiverocket.penjaminv1.Penjaminv1;
import net.positiverocket.penjaminv1.item.ModItems;
import net.positiverocket.penjaminv1.util.ColorNbt;

@Mod.EventBusSubscriber(modid = Penjaminv1.MODID,
        bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModBusEvents {

    @SubscribeEvent
    public static void onRegisterItemColors(RegisterColorHandlersEvent.Item e) {
        e.register((stack, idx) -> idx == 0 ? ColorNbt.get(stack) : 0xFFFFFFFF,
                ModItems.BATTERY_WITH_CART.get(),
                ModItems.BATTERY.get());
    }
}
