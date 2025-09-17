package net.positiverocket.penjaminv1.item.custom;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.registries.ForgeRegistries;
import net.positiverocket.penjaminv1.item.ModItems;
import net.positiverocket.penjaminv1.sound.ModSounds;
import net.positiverocket.penjaminv1.util.BatteryNbt;
import net.positiverocket.penjaminv1.util.CartNbt;
import net.positiverocket.penjaminv1.util.ColorNbt;

import java.util.function.Consumer;

public class PenjaminItem extends Item {
    public PenjaminItem(Properties properties) {
        super(properties);
    }

    private static final String NBT_RIP_ARMED = "RipArmed";
    private static void armRip(ItemStack s){ s.getOrCreateTag().putBoolean(NBT_RIP_ARMED, true); }
    private static boolean disarmRip(ItemStack s){
        var t = s.getOrCreateTag();
        boolean armed = t.getBoolean(NBT_RIP_ARMED);
        if (armed) t.putBoolean(NBT_RIP_ARMED, false);
        return armed;
    }


    @Override
    public int getUseDuration(ItemStack pStack) {
        return 300;
    }

    @Override public int getMaxStackSize(ItemStack stack) { return 1; }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {

            private static final HumanoidModel.ArmPose PULLING_POSE = HumanoidModel.ArmPose.create("PULLING", false, (model, entity, arm) -> {
                // === Snap instantly to pose when using ===
                float lift = (float) Math.toRadians(65.0F);     // ️ How much to raise arm
                float inward = (float) Math.toRadians(40.0F);   //  How much to rotate inward

                if (arm == HumanoidArm.RIGHT) {
                    model.rightArm.xRot = -lift;
                    model.rightArm.yRot = -inward;
                } else {
                    model.leftArm.xRot = -lift;
                    model.leftArm.yRot = inward;
                }
            });

            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
                if (!itemStack.isEmpty() &&
                        entityLiving.getUsedItemHand() == hand &&
                        entityLiving.getUseItemRemainingTicks() > 0) {
                    return PULLING_POSE;
                }
                return HumanoidModel.ArmPose.EMPTY;
            }

            @Override
            public boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm, ItemStack itemInHand,
                                                   float partialTick, float equipProcess, float swingProcess) {
                if (player.getUseItem() == itemInHand && player.isUsingItem()) {
                    if(arm == HumanoidArm.RIGHT){
                        int i = arm == HumanoidArm.RIGHT ? 1 : -1;

                        // === Base hand position (same as vanilla) ===
                        poseStack.translate(i * 0.56F, -0.52F, -0.72F); //  adjust for resting position

                        // === Snap into "pulling" position (like eating/drinking) ===
                        poseStack.translate(-0.50F, 0.05F, -0.15F);             // X: left/right Y: up/down, Z: toward/away from face
                        poseStack.mulPose(Axis.XP.rotationDegrees(20.0F)); //  Pitch arm up
                        poseStack.mulPose(Axis.YP.rotationDegrees(i * 15.0F)); //  Turn inward

                        return true;
                    }
                }
                return false;
            }
        });
    }



    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.CUSTOM;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (hand == InteractionHand.OFF_HAND) return InteractionResultHolder.pass(player.getItemInHand(hand));
        ItemStack stack = player.getItemInHand(hand);


        // must have charge + a loaded cart with uses
        if (BatteryNbt.isDepleted(stack) || !hasLoadedCart(stack)) {
            if (!level.isClientSide) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        net.minecraft.sounds.SoundEvents.REDSTONE_TORCH_BURNOUT, net.minecraft.sounds.SoundSource.PLAYERS, 0.5f, 1.2f);
            }
            return InteractionResultHolder.fail(stack);
        }

        armRip(stack);                 // <-- NEW
        player.startUsingItem(hand);

        if (!level.isClientSide) {
            level.playSound(player, player.getX(), player.getY(), player.getZ(),
                    net.minecraft.sounds.SoundEvents.GENERIC_DRINK, net.minecraft.sounds.SoundSource.PLAYERS, 0.6f, 1.0f);
        }
        return InteractionResultHolder.consume(stack);
    }


    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!level.isClientSide && disarmRip(stack)) {
            int heldTicks = getUseDuration(stack) - timeLeft;
            finalizeRipOnce(stack, level, entity, heldTicks);
        }
        super.releaseUsing(stack, level, entity, timeLeft);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        // Safety for timeout-style finish (if you ever set a finite duration)
        if (!level.isClientSide && disarmRip(stack)) {
            finalizeRipOnce(stack, level, entity, getUseDuration(stack));
        }
        return stack;
    }

    private void finalizeRipOnce(ItemStack stack, Level level, LivingEntity entity, int heldTicks) {
        // Run one-shot visuals/SFX and apply extract effects (no counters here)
        handleUseEffects(stack, level, entity, heldTicks);

        // ---- EXACTLY ONE consumption & conversion (server only) ----
        if (!level.isClientSide) {
            BatteryNbt.tryConsume(stack, 1);                   // 1/40 per rip
            boolean consumedCart = CartNbt.tryConsume(stack, 1); // 1/20 per rip

            if (consumedCart && CartNbt.isDepleted(stack)) {
                refundEmptyCart(level, entity);
                convertPenToBatteryAndGive(level, entity, stack); // preserves colour + remaining charge
                return; // pen stack is gone
            }

            if (entity instanceof Player p) {
                p.getCooldowns().addCooldown(this, 3 * 20);
            }
        }
    }



    private void handleUseEffects(ItemStack stack, Level level, LivingEntity entity, int heldTicks) {
        // --- Smoke particles (now works even when called server-side) ---
        spawnSmokeParticles(level, entity);

        // --- Cough RNG scaled by hold time (your logic) ---
        float tSec = heldTicks / 20f;
        int coughs = 0;
        var rand = level.random;

        if (tSec < 5f) {
            if (rand.nextFloat() < 0.40f) coughs = 1;
        } else if (tSec < 6f) {
            if (rand.nextFloat() < 0.50f) coughs = 1 + rand.nextInt(4); // 2–5
        } else if (tSec < 15f) {
            if (rand.nextFloat() < 0.85f) coughs = 2 + rand.nextInt(6); // 2–7
        } else {
            coughs = 2 + rand.nextInt(6); // 2–7
        }

        float dmg = (tSec >= 15f) ? 2.0F : 1.0F;
        int interval = Math.max(15, 40 - (int)tSec);

        if (coughs > 0 && !level.isClientSide && entity instanceof Player player) {
            var cough = ModSounds.COUGH.get();
            net.positiverocket.penjaminv1.common.CoughScheduler.schedule(player, coughs, interval, dmg, cough);
        }

        // --- Exhale sound (server broadcast) ---
        if (!level.isClientSide) {
            var exhale = ModSounds.EXHALE.get();
            float volume = 0.8f + level.random.nextFloat() * 0.4f; // 0.8 → 1.2
            float pitch  = 0.8f + level.random.nextFloat() * 0.4f; // 0.8 → 1.2
            level.playSound(
                    (entity instanceof Player p) ? p : null,
                    entity.getX(), entity.getY(), entity.getZ(),
                    exhale, net.minecraft.sounds.SoundSource.PLAYERS,
                    volume, pitch
            );
        }

        // --- Apply extract effects scaled by heldTicks (server only) ---
        if (!level.isClientSide) {
            applyExtractEffectsFromNbt(stack, level, entity, heldTicks);
        }
    }



    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUse) {
        // Only do sound logic every few ticks so we don't spam
        int held = getUseDuration(stack) - remainingUse;
        if (held <= 0) return;

        // Play every 8 ticks (≈ 0.4s)
        if (held % 8 != 0) return;

        // Scale volume/pitch with hold time up to 15s
        int maxTicks = 15 * 20; // 15s
        float t = Math.min(1.0f, held / (float) maxTicks);

        // starts soft, gets stronger
        float volume = 0.25f + t * 0.9f;   // 0.25 → 1.15
        float pitch  = 0.85f + t * 0.3f;   // 0.85 → 1.15

        var sound = ModSounds.INHALE.get();

        // Server-side broadcast so nearby players can hear it too
        level.playSound(
                (entity instanceof Player p) ? p : null,
                entity.getX(), entity.getY(), entity.getZ(),
                sound,
                net.minecraft.sounds.SoundSource.PLAYERS,
                volume, pitch
        );
    }


    private void spawnSmokeParticles(Level level, LivingEntity entity) {
        if (level.isClientSide) {
            // client-side: spawn locally
            for (int i = 0; i < 10; i++) {
                double xOffset = level.random.nextGaussian() * 0.02D;
                double yOffset = level.random.nextGaussian() * 0.02D;
                double zOffset = level.random.nextGaussian() * 0.02D;

                level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                        entity.getX() + entity.getBbWidth() * (level.random.nextDouble() - 0.5D),
                        entity.getEyeY(),
                        entity.getZ() + entity.getBbWidth() * (level.random.nextDouble() - 0.5D),
                        xOffset, yOffset, zOffset);
            }
        } else if (level instanceof net.minecraft.server.level.ServerLevel sl) {
            // server-side: broadcast to nearby clients
            // Use a single call with count & spread so it looks natural
            sl.sendParticles(
                    ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    entity.getX(), entity.getEyeY(), entity.getZ(),
                    10,                // count
                    entity.getBbWidth() * 0.5, // spreadX
                    0.1,               // spreadY
                    entity.getBbWidth() * 0.5, // spreadZ
                    0.02               // speed
            );
        }
    }


    @Override
    public void appendHoverText(ItemStack stack, @org.jetbrains.annotations.Nullable Level level,
                                java.util.List<net.minecraft.network.chat.Component> tooltip,
                                net.minecraft.world.item.TooltipFlag flag) {

        // ---- Colour line (unchanged) ----
        int rgb = net.positiverocket.penjaminv1.util.ColorNbt.get(stack);
        String hex = String.format("#%06X", (0xFFFFFF & rgb));
        tooltip.add(net.minecraft.network.chat.Component.literal("Colour: ")
                .withStyle(net.minecraft.ChatFormatting.GRAY)
                .append(net.minecraft.network.chat.Component.literal(hex).withStyle(net.minecraft.ChatFormatting.AQUA)));

        tooltip.add(Component.literal("Scales with draw length; Nausea at 15s+")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));

        // Effects line (inserted here, between Colour and Extract)
        String strain = FullCartItem.getStrainId(stack);
        if (!strain.isEmpty()) {
            var id = net.minecraft.resources.ResourceLocation.tryParse(strain);
            var extractItem = id != null ? net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(id) : null;

            if (extractItem != null) {
                var effects = getEffectsForExtract(extractItem); // <- uses the switch mapping from earlier
                if (!effects.isEmpty()) {
                    // Build the prefix
                    MutableComponent line = Component.literal("Effects: ")
                            .withStyle(ChatFormatting.GREEN);

                    // Join the effects into one component
                    MutableComponent joined = Component.empty();
                    for (int i = 0; i < effects.size(); i++) {
                        if (i > 0) {
                            joined.append(Component.literal(", ").withStyle(ChatFormatting.DARK_GRAY));
                        }
                        joined.append(effectToComponent(effects.get(i)));
                    }

                    // Add the joined list to the prefix and add to tooltip
                    line.append(joined.withStyle(ChatFormatting.GRAY));
                    tooltip.add(line);

                }
            }
        }

        // Extract line (unchanged, but now appears AFTER the new Effects line)
        if (!strain.isEmpty()) {
            var id = net.minecraft.resources.ResourceLocation.tryParse(strain);
            var item = id != null ? net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(id) : null;
            if (item != null) {
                tooltip.add(net.minecraft.network.chat.Component.literal("Extract")
                        .withStyle(net.minecraft.ChatFormatting.GREEN)
                        .append(net.minecraft.network.chat.Component.literal(": ").withStyle(net.minecraft.ChatFormatting.GRAY))
                        .append(new net.minecraft.world.item.ItemStack(item).getHoverName().copy().withStyle(net.minecraft.ChatFormatting.GRAY)));
            }
        }

        // ---- Charge line ----
        int charge = BatteryNbt.get(stack);
        String chargeText = (charge <= 0) ? "Depleted" : (charge + " / " + BatteryNbt.MAX);
        tooltip.add(Component.literal("Charge: ")
                .withStyle(net.minecraft.ChatFormatting.GRAY)
                .append(Component.literal(chargeText)
                        .withStyle(charge <= 0 ? net.minecraft.ChatFormatting.RED : net.minecraft.ChatFormatting.YELLOW)));

        // ---- Cart uses line (only if a cart is loaded) ----
        if (resolveLoadedStrain(stack) != null) {
            int uses = CartNbt.get(stack);
            String usesText = uses + " / " + CartNbt.MAX;
            tooltip.add(Component.literal("Cart Uses: ")
                    .withStyle(net.minecraft.ChatFormatting.GRAY)
                    .append(Component.literal(usesText)
                            .withStyle(uses > 0 ? net.minecraft.ChatFormatting.AQUA : net.minecraft.ChatFormatting.RED)));
        }

    }


    // --- BEGIN: Extract effect mapping helpers --- //

    private static java.util.List<MobEffectInstance> getEffectsForExtract(net.minecraft.world.item.Item extractItem) {
        // Switch on the registry path (e.g., "blue_ice_extract").
        String path = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(extractItem).getPath();

        return switch (path) {
            case "blue_ice_extract" -> java.util.List.of(
                    new MobEffectInstance(MobEffects.LUCK,           20 * 60, 0)
            );
            case "bubblegum_extract" -> java.util.List.of(
                    new MobEffectInstance(MobEffects.LEVITATION,     20 * 3,  0) // short & funny
            );
            case "bubble_kush_extract" -> java.util.List.of(
                    new MobEffectInstance(MobEffects.DIG_SPEED,      20 * 90, 0)
            );
            case "lemon_haze_extract" -> java.util.List.of(
                    new MobEffectInstance(MobEffects.JUMP,           20 * 60, 1)
            );
            case "purple_haze_extract" -> java.util.List.of(
                    new MobEffectInstance(MobEffects.NIGHT_VISION,   20 * 120, 0)
            );
            case "sour_diesel_extract" -> java.util.List.of(
                    new MobEffectInstance(MobEffects.MOVEMENT_SPEED,     20 * 90, 1),
                    new MobEffectInstance(MobEffects.DAMAGE_BOOST,       20 * 30, 0)
            );
            case "white_widow_extract" -> java.util.List.of(
                    new MobEffectInstance(MobEffects.REGENERATION,       20 * 20, 0),
                    new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,  20 * 40, 0)
            );
            default -> java.util.List.of(); // unknown strain = no effects
        };
    }

    // Scales extract effects by held time and applies them
    private static void applyExtractEffectsFromNbt(ItemStack penStack, Level level, LivingEntity entity, int heldTicks) {
        if (level.isClientSide) return;

        String strain = FullCartItem.getStrainId(penStack);
        if (strain == null || strain.isEmpty()) return;

        var rl = net.minecraft.resources.ResourceLocation.tryParse(strain);
        if (rl == null) return;

        var extractItem = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(rl);
        if (extractItem == null) return;

        // Base effects for this extract (your switch mapping)
        var base = getEffectsForExtract(extractItem);
        if (base.isEmpty()) return;

        // --- Scaling knobs ---
        float tSec = Math.min(15f, heldTicks / 20f);      // clamp at 15s
        float potency = tSec / 15f;                       // 0.0 .. 1.0
        float durationScale = 0.5f + 1.5f * potency;      // 0.5x at tap → 2.0x at full 15s

        // Stepwise amplifier bumps: +1 at 5s, +2 at 10s, +3 at 14s
        int ampBonus = 0;
        if (tSec >= 5f) ampBonus++;
        if (tSec >= 10f) ampBonus++;
        if (tSec >= 14f) ampBonus++;

        for (var inst : base) {
            int scaledDur = Math.max(20, Math.round(inst.getDuration() * durationScale));
            int scaledAmp = Math.min(4, inst.getAmplifier() + ampBonus); // cap so it doesn't get silly

            int munchDur = Math.max(20 * 6, Math.min(20 * 45, Math.round(scaledDur * 0.35f)));
            int munchAmp = (scaledDur >= 20 * 25) ? 1 : 0;

            entity.addEffect(new MobEffectInstance(
                    inst.getEffect(),
                    scaledDur,
                    scaledAmp,
                    inst.isAmbient(),
                    inst.isVisible(),
                    inst.showIcon()
            ));
            entity.addEffect(new MobEffectInstance(
                    MobEffects.HUNGER,
                    munchDur,
                    munchAmp,
                    true,
                    true
            ));
        }

        // Guaranteed nausea if they full-ripped (≥15s)
        if (tSec >= 15f) {
            entity.addEffect(new MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.CONFUSION,
                    15 * 20, // 15s
                    0
            ));
        }
    }

    // --- END: Extract effect mapping helpers --- //

    private static String formatTicks(int ticks) {
        int totalSec = Math.max(0, ticks / 20);
        int m = totalSec / 60, s = totalSec % 60;
        return String.format("%d:%02d", m, s);
    }

    private static String ampToRoman(int amp) { // amp 0 => I
        int n = Math.max(0, amp) + 1;
        // Cheap & cheerful for small values
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
        var name = inst.getEffect().getDisplayName().copy();
        String amp = ampToRoman(inst.getAmplifier());
        String dur = formatTicks(inst.getDuration());
        return Component.empty()
                .append(name)
                .append(Component.literal(" " + amp + " (" + dur + ")")
                        .withStyle(ChatFormatting.GRAY));
    }

    private static boolean hasLoadedCart(ItemStack pen) {
        String strain = FullCartItem.getStrainId(pen);
        return strain != null && !strain.isEmpty() && net.positiverocket.penjaminv1.util.CartNbt.get(pen) > 0;
    }

    /** Remove the attached cart (clear strain + uses), and give an Empty Cart to the user (or drop). */
    private static void unloadCartAndRefundEmpty(Level level, LivingEntity entity, ItemStack pen) {
        // Clear strain + uses
        var tag = pen.getOrCreateTag();
        tag.remove(FullCartItem.NBT_STRAIN_ID);
        CartNbt.set(pen, 0);

        // Give empty cart back
        ItemStack empty = new ItemStack(ModItems.EMPTY_CART.get());
        if (entity instanceof Player p && p.getInventory().add(empty)) {
            // added to inventory
        } else {
            entity.spawnAtLocation(empty);
        }

        // Little feedback
        level.playSound(
                (entity instanceof Player p) ? p : null,
                entity.getX(), entity.getY(), entity.getZ(),
                SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.PLAYERS, 0.7f, 1.0f
        );
    }

    /** Convenience: parse the loaded strain item from pen NBT (null if none). */
    @org.jetbrains.annotations.Nullable
    private static Item resolveLoadedStrain(ItemStack pen) {
        String s = FullCartItem.getStrainId(pen);
        if (s == null || s.isEmpty()) return null;
        ResourceLocation id = ResourceLocation.tryParse(s);
        return id != null ? ForgeRegistries.ITEMS.getValue(id) : null;
    }

    // Inside PenjaminItem
    private static boolean beginRipOnce(Level level, ItemStack stack) {
        long now = level.getGameTime();
        var tag = stack.getOrCreateTag();
        long last = tag.getLong("LastRipTick");
        if (last == now) return false;     // already processed this tick
        tag.putLong("LastRipTick", now);
        return true;
    }

    private static void refundEmptyCart(Level level, LivingEntity entity) {
        ItemStack empty = new ItemStack(ModItems.EMPTY_CART.get());
        if (entity instanceof Player p && p.getInventory().add(empty)) {
            // ok
        } else {
            entity.spawnAtLocation(empty);
        }
    }

    private static void convertPenToBatteryAndGive(Level level, LivingEntity entity, ItemStack pen) {
        int charge = BatteryNbt.get(pen);

        ItemStack battery = new ItemStack(ModItems.BATTERY.get()); // <-- rename if needed
        BatteryNbt.set(battery, charge);
        ColorNbt.set(battery, ColorNbt.get(pen));

        boolean given = false;
        if (entity instanceof Player p) {
            given = p.getInventory().add(battery);
        }
        if (!given) entity.spawnAtLocation(battery);

        // remove the pen from the world/inventory
        pen.shrink(1);

        // little feedback
        level.playSound(
                (entity instanceof Player p) ? p : null,
                entity.getX(), entity.getY(), entity.getZ(),
                net.minecraft.sounds.SoundEvents.ITEM_FRAME_REMOVE_ITEM,
                net.minecraft.sounds.SoundSource.PLAYERS, 0.7f, 1.0f
        );
    }
}
