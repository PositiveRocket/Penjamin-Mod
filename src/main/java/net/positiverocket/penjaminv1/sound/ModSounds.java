package net.positiverocket.penjaminv1.sound;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.positiverocket.penjaminv1.Penjaminv1;

public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, Penjaminv1.MODID);

    public static final RegistryObject<SoundEvent> INHALE =
            SOUNDS.register("inhale",
                    () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Penjaminv1.MODID, "inhale")));

    public static final RegistryObject<SoundEvent> EXHALE =
            SOUNDS.register("exhale",
                    () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Penjaminv1.MODID, "exhale")));

    public static final RegistryObject<SoundEvent> COUGH =
            SOUNDS.register("cough",
                    () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Penjaminv1.MODID, "cough")));

}
