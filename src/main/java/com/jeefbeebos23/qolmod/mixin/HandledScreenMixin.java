package com.jeefbeebos23.qolmod.mixin;

import com.jeefbeebos23.qolmod.config.QolConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {

    @Shadow private Slot getSlotAt(double x, double y) { return null; }
    @Shadow protected abstract void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType);
    @Shadow protected ScreenHandler handler;

    private final List<Slot> qol$dragSlots = new ArrayList<>();
    private boolean qol$isShiftRightDragging = false;

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void onScroll(double mouseX, double mouseY, double horizontalAmount,
                          double verticalAmount, CallbackInfoReturnable<Boolean> cir) {
        if (!QolConfig.getInstance().mouseTweaksEnabled) return;
        Slot slot = getSlotAt(mouseX, mouseY);
        if (slot == null || !slot.hasStack()) return;
        if (!handler.getCursorStack().isEmpty()) return;
        if (verticalAmount < 0) {
            onMouseClick(slot, slot.id, 0, SlotActionType.QUICK_MOVE);
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
    private void onDrag(double mouseX, double mouseY, int button,
                        double deltaX, double deltaY,
                        CallbackInfoReturnable<Boolean> cir) {
        if (!QolConfig.getInstance().mouseTweaksEnabled) return;
        if (button == 1 && MinecraftClient.getInstance().options.sneakKey.isPressed()) {
            Slot slot = getSlotAt(mouseX, mouseY);
            if (slot != null && !qol$dragSlots.contains(slot)) {
                qol$dragSlots.add(slot);
            }
            qol$isShiftRightDragging = true;
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"))
    private void onRelease(double mouseX, double mouseY, int button,
                           CallbackInfoReturnable<Boolean> cir) {
        if (button == 1 && qol$isShiftRightDragging) {
            for (Slot slot : qol$dragSlots) {
                if (handler.getCursorStack().isEmpty()) break;
                onMouseClick(slot, slot.id, 1, SlotActionType.PICKUP);
            }
            qol$dragSlots.clear();
            qol$isShiftRightDragging = false;
        }
    }
}
