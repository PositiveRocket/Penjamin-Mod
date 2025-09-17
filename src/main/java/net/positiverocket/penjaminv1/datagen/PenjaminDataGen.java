package net.positiverocket.penjaminv1.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.positiverocket.penjaminv1.Penjaminv1;

@Mod.EventBusSubscriber(modid = Penjaminv1.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class PenjaminDataGen {
    @SubscribeEvent
    public static void gather(GatherDataEvent e) {
        DataGenerator gen = e.getGenerator();
        PackOutput out = gen.getPackOutput();
        if (e.includeServer()) {
            gen.addProvider(true, new PenjaminRecipeProvider(out));
        }
    }
}
