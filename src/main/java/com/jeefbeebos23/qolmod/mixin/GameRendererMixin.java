package com.jeefbeebos23.qolmod.mixin;

import com.jeefbeebos23.qolmod.features.ZoomFeature;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void overrideFov(Camera camera,
                              float tickDelta, boolean changingFov,
                              CallbackInfoReturnable<Float> cir) {
        if (ZoomFeature.isZooming()) {
            cir.setReturnValue(ZoomFeature.getSmoothedFov(cir.getReturnValue()));
        } else {
            ZoomFeature.resetFov();  // reset so next zoom press starts from current FOV
        }
    }
}
