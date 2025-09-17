package net.positiverocket.penjaminv1.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.positiverocket.penjaminv1.item.ModItems;
import net.positiverocket.penjaminv1.util.BatteryNbt;
import net.positiverocket.penjaminv1.util.CartNbt;
import net.positiverocket.penjaminv1.util.ColorNbt;

import javax.annotation.Nullable;
import java.util.List;

public class BatteryItem extends Item {
    public BatteryItem(Properties props) { super(props); }


    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResultHolder.pass(player.getItemInHand(hand));

        ItemStack battery = player.getItemInHand(hand);
        ItemStack cart    = player.getOffhandItem();

        //  Block combining when the battery is empty
        if (BatteryNbt.get(battery) <= 0) {
            if (!level.isClientSide) {
                player.displayClientMessage(
                        Component.literal("Battery is depleted — recharge on a Redstone Block.")
                                .withStyle(ChatFormatting.RED),
                        true
                );
                player.getCooldowns().addCooldown(this, 8); // debounce spam
                level.playSound(null, player.blockPosition(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 0.6f, 1.3f);
            }
            return InteractionResultHolder.pass(battery);
        }

        // Require YOUR full cart + StrainId
        if (cart.getItem() != ModItems.FULL_CART.get()) return InteractionResultHolder.pass(battery);
        if (!cart.hasTag() || !cart.getTag().contains(FullCartItem.NBT_STRAIN_ID, Tag.TAG_STRING)) {
            return InteractionResultHolder.pass(battery);
        }

        if (!level.isClientSide) {
            // READ current battery charge (e.g., 20/40 if half used)
            int charge = BatteryNbt.get(battery);   // <-- ADD

            // Build ONE pen with the right data
            ItemStack pen = new ItemStack(ModItems.BATTERY_WITH_CART.get());
            ColorNbt.set(pen, ColorNbt.get(battery));
            CartNbt.set(pen, CartNbt.MAX); // 20 uses for the new cart
            pen.getOrCreateTag().putString(FullCartItem.NBT_STRAIN_ID, cart.getTag().getString(FullCartItem.NBT_STRAIN_ID));

            // WRITE that same charge onto the new pen so it DOESN'T reset to 40/40
            BatteryNbt.set(pen, charge);            // <-- ADD

            // Safety: make each pen unique so it cannot merge even if something else made it stackable
            pen.getOrCreateTag().putUUID("UID", java.util.UUID.randomUUID());

            // Consume exactly ONE battery and ONE cart
            battery.shrink(1);
            cart.shrink(1);

            // Give exactly ONE pen to inventory (never replaces hand)
            if (!player.addItem(pen)) player.drop(pen, false);

            // Feedback + cooldown to block re-fires
            level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME,
                    SoundSource.PLAYERS, 0.7f, 1.2f);
            player.getCooldowns().addCooldown(this, 8);
        }

        player.swing(hand);
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        Player player = ctx.getPlayer();
        ItemStack stack = ctx.getItemInHand();
        BlockPos pos = ctx.getClickedPos();

        // Only care about redstone blocks
        if (!level.getBlockState(pos).is(Blocks.REDSTONE_BLOCK)) {
            return InteractionResult.PASS;
        }

        int charge = BatteryNbt.get(stack);

        // Only recharge if depleted (<= 0). Change to (< BatteryNbt.MAX) if you want a "top-up".
        if (charge > 0) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide) {
            // Full recharge
            BatteryNbt.set(stack, BatteryNbt.MAX);

            // Feedback
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8f, 1.4f);
            if (player != null) {
                player.getCooldowns().addCooldown(this, 8); // tiny debounce
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }


    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        int charge = net.positiverocket.penjaminv1.util.BatteryNbt.get(stack);
        String txt = (charge <= 0) ? "Depleted" : (charge + " / " + net.positiverocket.penjaminv1.util.BatteryNbt.MAX);
        tooltip.add(Component.literal("Charge: ")
                .withStyle(net.minecraft.ChatFormatting.GRAY)
                .append(Component.literal(txt)
                        .withStyle(charge <= 0 ? net.minecraft.ChatFormatting.RED : net.minecraft.ChatFormatting.YELLOW)));
        if(charge <= 0){
            tooltip.add(Component.literal("")
                    .withStyle(net.minecraft.ChatFormatting.GRAY)
                    .append(Component.literal("To recharge, right-click a ")
                            .withStyle(ChatFormatting.GRAY))
                    .append(Component.literal("Block of Redstone")
                            .withStyle(ChatFormatting.RED)));;
        }
    }


}
