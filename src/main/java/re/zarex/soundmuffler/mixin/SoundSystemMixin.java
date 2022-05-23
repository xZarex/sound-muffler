package re.zarex.soundmuffler.mixin;

import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import re.zarex.soundmuffler.SoundMufflerClientMod;

@Mixin(SoundSystem.class)
public class SoundSystemMixin {

    @Inject(method = "Lnet/minecraft/client/sound/SoundSystem;getAdjustedVolume(Lnet/minecraft/client/sound/SoundInstance;)F", at = @At("HEAD"), cancellable = true)
    private void getAdjustedVolume(SoundInstance sound, CallbackInfoReturnable<Float> cir) {
        if (SoundMufflerClientMod.isMufflerClose(sound.getX(), sound.getY(), sound.getZ()))
        {
            cir.setReturnValue(0.0f);
            cir.cancel();
        }
    }

}
