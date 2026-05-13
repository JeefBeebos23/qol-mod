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

    // Shift+RMB drag: deposit 1 item from cursor into each hovered slot
    private final List<Slot> qol$dragSlots = new ArrayList<>();
    private boolean qol$isShiftRightDragging = false;

    // Shift+LMB drag: quick-move (shift-click) all hovered slots
    private final List<Slot> qol$leftDragSlots = new ArrayList<>();
    private boolean qol$isShiftLeftDragging = false;

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(MouseButtonEvent event, boolean bl, CallbackInfoReturnable<Boolean> cir) {
        if (!QolConfig.getInstance().mouseTweaksEnabled) return;
        if (event.button() == 0 && Minecraft.getInstance().options.keyShift.isDown()
                && menu.getCarried().isEmpty()) {
            // Suppress the initial shift-click so the drag handles all slots uniformly.
            // We record the slot here and process it on release.
            Slot slot = getHoveredSlot(event.x(), event.y());
            if (slot != null && slot.hasItem()) {
                qol$leftDragSlots.add(slot);
                qol$isShiftLeftDragging = true;
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
    private void onDrag(MouseButtonEvent event, double deltaX, double deltaY,
                        CallbackInfoReturnable<Boolean> cir) {
        if (!QolConfig.getInstance().mouseTweaksEnabled) return;
        boolean shiftHeld = Minecraft.getInstance().options.keyShift.isDown();
        if (event.button() == 0 && shiftHeld && qol$isShiftLeftDragging) {
            Slot slot = getHoveredSlot(event.x(), event.y());
            if (slot != null && slot.hasItem() && !qol$leftDragSlots.contains(slot)) {
                qol$leftDragSlots.add(slot);
            }
            cir.setReturnValue(true);
        } else if (event.button() == 1 && shiftHeld) {
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
        if (event.button() == 0 && qol$isShiftLeftDragging) {
            for (Slot slot : qol$leftDragSlots) {
                if (slot.hasItem()) {
                    slotClicked(slot, slot.index, 0, ContainerInput.QUICK_MOVE);
                }
            }
            qol$leftDragSlots.clear();
            qol$isShiftLeftDragging = false;
        } else if (event.button() == 1 && qol$isShiftRightDragging) {
            for (Slot slot : qol$dragSlots) {
                if (menu.getCarried().isEmpty()) break;
                slotClicked(slot, slot.index, 1, ContainerInput.PICKUP);
            }
            qol$dragSlots.clear();
            qol$isShiftRightDragging = false;
        }
    }
}
