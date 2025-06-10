package io.github.frostzie.skyfall.mixin;

import io.github.frostzie.skyfall.features.dev.SoundDetector;
import io.github.frostzie.skyfall.features.foraging.TuneFrequency;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundSystem.class)
public class SoundSystemMixin {

    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;)V", at = @At("TAIL"))
    private void onSoundPlay(SoundInstance soundInstance, CallbackInfo ci) {
        try {
            SoundDetector.INSTANCE.onSoundPlay(soundInstance);
            TuneFrequency.INSTANCE.onSoundPlay(soundInstance); // Added TuneFrequency integration
        } catch (Exception e) {
            // Silently catch exceptions to prevent crashes
        }
    }

    @Inject(method = "stop(Lnet/minecraft/client/sound/SoundInstance;)V", at = @At("HEAD"))
    private void onSoundStop(SoundInstance soundInstance, CallbackInfo ci) {
        try {
            SoundDetector.INSTANCE.onSoundStop(soundInstance);
            TuneFrequency.INSTANCE.onSoundStop(soundInstance); // Added TuneFrequency integration
        } catch (Exception e) {
            // Silently catch exceptions to prevent crashes
        }
    }

    @Inject(method = "stopAll()V", at = @At("HEAD"))
    private void onStopAll(CallbackInfo ci) {
        try {
            SoundDetector.INSTANCE.clearAllSounds();
        } catch (Exception e) {
            // Silently catch exceptions to prevent crashes
        }
    }
}