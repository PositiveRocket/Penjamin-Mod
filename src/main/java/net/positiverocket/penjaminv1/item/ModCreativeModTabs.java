package net.positiverocket.penjaminv1.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.positiverocket.penjaminv1.Penjaminv1;
import net.positiverocket.penjaminv1.item.custom.FullCartItem;
import net.positiverocket.penjaminv1.util.ColorNbt;

public class ModCreativeModTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS=
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Penjaminv1.MODID);

    public static final RegistryObject<CreativeModeTab> PENJAMIN_TAB = CREATIVE_MODE_TABS.register("penjamin_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.BATTERY_WITH_CART.get()))
                .title(Component.translatable("creativetab.penjamin_tab"))
                    .displayItems((itemDisplayParameters, output) -> {

                        for (DyeColor dc : DyeColor.values()) {
                            int rgb = dc.getTextColor();

                            ItemStack BATTERY = new ItemStack(ModItems.BATTERY.get());
                            ColorNbt.set(BATTERY, rgb);
                            output.accept(BATTERY);
                        }

                        for (DyeColor dc : DyeColor.values()) {
                            int rgb = dc.getTextColor();

                            ItemStack BATTERY_WITH_CART = new ItemStack(ModItems.BATTERY_WITH_CART.get());
                            ColorNbt.set(BATTERY_WITH_CART, rgb);
                            output.accept(BATTERY_WITH_CART);
                        }

                        // --- Full Carts for every extract strain in the tag ---
                        var itemLookup   = itemDisplayParameters.holders().lookupOrThrow(Registries.ITEM);
                        var EXTRACT_TAG  = TagKey.create(Registries.ITEM, new ResourceLocation(Penjaminv1.MODID, "smokeleaf_extracts"));

                        itemLookup.get(EXTRACT_TAG).ifPresent(namedSet -> {
                            var strains = namedSet.stream()
                                    .map(net.minecraft.core.Holder::value)
                                    .sorted(java.util.Comparator.comparing(i -> new ItemStack(i).getHoverName().getString()))
                                    .toList();

                            for (var strainItem : strains) {
                                ItemStack full = new ItemStack(ModItems.FULL_CART.get());
                                FullCartItem.setStrainId(full, strainItem);
                                output.accept(full);
                            }
                        });
                        
                        output.accept(ModItems.EMPTY_CART.get());

                    })
                    .build());

    public static void register(IEventBus eventBus){
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
