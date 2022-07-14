package re.zarex.soundmuffler.mixin;

import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import re.zarex.soundmuffler.SoundMufflerClientMod;
import re.zarex.soundmuffler.SoundMufflerMod;

@Mixin(SoundSystem.class)
public class SoundSystemMixin {

    @Redirect(method = "Lnet/minecraft/client/sound/SoundSystem;play(Lnet/minecraft/client/sound/SoundInstance;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sound/SoundInstance;getVolume()F"))
    private float injectedGetVolume(SoundInstance sound) {
        if (SoundMufflerClientMod.isMufflerClose(sound.getX(), sound.getY(), sound.getZ()))
        {
            return 0f;
        }

        return sound.getVolume();
    }

    @Inject(method = "Lnet/minecraft/client/sound/SoundSystem;getAdjustedVolume(Lnet/minecraft/client/sound/SoundInstance;)F", at = @At("HEAD"), cancellable = true)
    private void getAdjustedVolume(SoundInstance sound, CallbackInfoReturnable<Float> cir) {
        if (SoundMufflerClientMod.isMufflerClose(sound.getX(), sound.getY(), sound.getZ()))
        {
            cir.setReturnValue(0.0f);
            cir.cancel();
        }
    }

}
