package net.positiverocket.penjaminv1.item;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.positiverocket.penjaminv1.Penjaminv1;
import net.positiverocket.penjaminv1.item.custom.BatteryItem;
import net.positiverocket.penjaminv1.item.custom.FullCartItem;
import net.positiverocket.penjaminv1.item.custom.PenjaminItem;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(ForgeRegistries.ITEMS, Penjaminv1.MODID);

    public static final RegistryObject<Item> BATTERY_WITH_CART = ITEMS.register("battery_with_cart",
            () -> new PenjaminItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> EMPTY_CART = ITEMS.register("empty_cart",
            () -> new Item(new Item.Properties().stacksTo(64)));
    public static final RegistryObject<Item> BATTERY = ITEMS.register("battery",
            () -> new BatteryItem(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> FULL_CART = ITEMS.register("full_cart",
            () -> new FullCartItem(new Item.Properties().stacksTo(16)));

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}