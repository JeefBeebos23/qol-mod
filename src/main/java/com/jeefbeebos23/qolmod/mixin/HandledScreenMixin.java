package com.jeefbeebos23.qolmod.mixin;

import com.jeefbeebos23.qolmod.config.QolConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(AbstractContainerScreen.class)
public abstract class HandledScreenMixin {

    @Shadow private Slot getHoveredSlot(double x, double y) { return null; }
    @Shadow protected abstract void slotClicked(Slot slot, int slotId, int button, ContainerInput actionType);
    @Shadow protected AbstractContainerMenu menu;

    private final List<Slot> qol$dragSlots = new ArrayList<>();
    private boolean qol$isShiftRightDragging = false;

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void onScroll(double mouseX, double mouseY, double horizontalAmount,
                          double verticalAmount, CallbackInfoReturnable<Boolean> cir) {
        if (!QolConfig.getInstance().mouseTweaksEnabled) return;
        Slot slot = getHoveredSlot(mouseX, mouseY);
        if (slot == null || !slot.hasItem()) return;
        if (!menu.getCarried().isEmpty()) return;
        if (verticalAmount < 0) {
            slotClicked(slot, slot.index, 0, ContainerInput.QUICK_MOVE);
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
    private void onDrag(MouseButtonEvent event, double deltaX, double deltaY,
                        CallbackInfoReturnable<Boolean> cir) {
        if (!QolConfig.getInstance().mouseTweaksEnabled) return;
        if (event.button() == 1 && Minecraft.getInstance().options.keyShift.isDown()) {
            Slot slot = getHoveredSlot(event.x(), event.y());
            if (slot != null && !qol$dragSlots.contains(slot)) {
                qol$dragSlots.add(slot);
            }
            qol$isShiftRightDragging = true;
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"))
    private void onRelease(MouseButtonEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (event.button() == 1 && qol$isShiftRightDragging) {
            for (Slot slot : qol$dragSlots) {
                if (menu.getCarried().isEmpty()) break;
                slotClicked(slot, slot.index, 1, ContainerInput.PICKUP);
            }
            qol$dragSlots.clear();
            qol$isShiftRightDragging = false;
        }
    }
}
