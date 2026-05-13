package com.jeefbeebos23.qolmod.mixin;

import com.jeefbeebos23.qolmod.features.ZoomFeature;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public class GameRendererMixin {

    @Inject(method = "calculateFov", at = @At("RETURN"), cancellable = true)
    private void overrideFov(float tickDelta, CallbackInfoReturnable<Float> cir) {
        if (ZoomFeature.isZooming()) {
            cir.setReturnValue(ZoomFeature.getSmoothedFov(cir.getReturnValue()));
        } else {
            ZoomFeature.resetFov();
        }
    }
}
