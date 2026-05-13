package com.jeefbeebos23.qolmod.mixin;

import com.jeefbeebos23.qolmod.config.QolConfig;
import com.jeefbeebos23.qolmod.features.MouseTweaksMoveOnePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
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

    // Shift+LMB drag: quick-move (shift-click) each hovered slot to the opposite container
    private final List<Slot> qol$leftDragSlots = new ArrayList<>();
    private boolean qol$isShiftLeftDragging = false;

    // Shift+RMB drag: move exactly 1 item from each hovered slot to the opposite container
    private final List<Integer> qol$rightDragIndices = new ArrayList<>();
    private boolean qol$isShiftRightDragging = false;

    @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
    private void onDrag(MouseButtonEvent event, double deltaX, double deltaY,
                        CallbackInfoReturnable<Boolean> cir) {
        if (!QolConfig.getInstance().mouseTweaksEnabled) return;
        boolean shiftHeld = Minecraft.getInstance().options.keyShift.isDown();
        boolean carriedEmpty = menu.getCarried().isEmpty();

        if (event.button() == 0 && shiftHeld && carriedEmpty) {
            Slot slot = getHoveredSlot(event.x(), event.y());
            if (slot != null && slot.hasItem() && !qol$leftDragSlots.contains(slot)) {
                qol$leftDragSlots.add(slot);
            }
            qol$isShiftLeftDragging = true;
            cir.setReturnValue(true);
        } else if (event.button() == 1 && shiftHeld && carriedEmpty) {
            Slot slot = getHoveredSlot(event.x(), event.y());
            if (slot != null && slot.hasItem() && !qol$rightDragIndices.contains(slot.index)) {
                qol$rightDragIndices.add(slot.index);
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
            if (!qol$rightDragIndices.isEmpty()) {
                ClientPlayNetworking.send(new MouseTweaksMoveOnePayload(new ArrayList<>(qol$rightDragIndices)));
            }
            qol$rightDragIndices.clear();
            qol$isShiftRightDragging = false;
        }
    }
}
