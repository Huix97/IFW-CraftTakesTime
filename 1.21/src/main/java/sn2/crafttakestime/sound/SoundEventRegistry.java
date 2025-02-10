package sn2.crafttakestime.sound;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;


import static sn2.crafttakestime.CraftTakesTime.MODID;

public class SoundEventRegistry {

    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, MODID);

    public static Holder<SoundEvent> craftingSound = SOUNDS.register("crafting", () ->
            SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "crafting")));
    public static Holder<SoundEvent> finishSound = SOUNDS.register("finish", () ->
            SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "finish")));
}
