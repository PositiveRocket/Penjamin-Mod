package net.positiverocket.penjaminv1.common;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "penjaminv1", bus = Mod.EventBusSubscriber.Bus.FORGE) // server logic
public final class CoughScheduler {
    private CoughScheduler() {}
    private static final Map<UUID, State> COUGHS = new HashMap<>();

    public static void schedule(Player p, int count, int intervalTicks, float damagePerCough,
                                net.minecraft.sounds.SoundEvent coughSound) {
        COUGHS.put(p.getUUID(), new State(count, intervalTicks, intervalTicks, damagePerCough, coughSound));
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent e) {
        if (e.side != LogicalSide.SERVER || e.phase != TickEvent.Phase.END) return;
        State s = COUGHS.get(e.player.getUUID());
        if (s == null) return;

        s.ticksUntilNext--;
        if (s.ticksUntilNext <= 0) {
            // deal damage (1.0F = half-heart, 2.0F = one heart)
            e.player.hurt(e.player.damageSources().generic(), s.damage);

            // randomize cough volume/pitch a bit
            float vol = 0.8f + e.player.level().random.nextFloat() * 0.4f; // 0.8–1.2
            float pit = 0.9f + e.player.level().random.nextFloat() * 0.2f; // 0.9–1.1

            if (s.coughSound != null) {
                e.player.level().playSound(null, e.player.getX(), e.player.getY(), e.player.getZ(),
                        s.coughSound, SoundSource.PLAYERS, vol, pit);
            }

            s.remaining--;
            s.ticksUntilNext = s.interval;
            if (s.remaining <= 0) {
                COUGHS.remove(e.player.getUUID());
            }
        }
    }

    private static final class State {
        int remaining;
        final int interval;
        int ticksUntilNext;
        final float damage;
        final net.minecraft.sounds.SoundEvent coughSound;

        State(int remaining, int interval, int ticksUntilNext, float damage,
              net.minecraft.sounds.SoundEvent coughSound) {
            this.remaining = remaining;
            this.interval = interval;
            this.ticksUntilNext = ticksUntilNext;
            this.damage = damage;
            this.coughSound = coughSound;
        }
    }
}
