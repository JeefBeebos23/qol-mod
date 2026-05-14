package com.jeefbeebos23.qolmod.mixin;

import com.jeefbeebos23.qolmod.config.QolConfig;
import com.jeefbeebos23.qolmod.features.MouseTweaksMoveOnePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mixin(AbstractContainerScreen.class)
public abstract class HandledScreenMixin {

    @Shadow private Slot getHoveredSlot(double x, double y) { return null; }
    @Shadow protected abstract void slotClicked(Slot slot, int slotId, int button, ContainerInput actionType);
    @Shadow protected AbstractContainerMenu menu;

    private final Set<Integer> qol$lmbDraggedIndices = new HashSet<>();
    private final Set<Integer> qol$rmbDraggedIndices = new HashSet<>();

    // Intercept Shift+RMB click to move 1 item instead of vanilla's full-stack quick-move
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(MouseButtonEvent event, boolean bl, CallbackInfoReturnable<Boolean> cir) {
        if (!QolConfig.getInstance().mouseTweaksEnabled) return;
        if (event.button() != 1 || !event.hasShiftDown() || !menu.getCarried().isEmpty()) return;
        Slot slot = getHoveredSlot(event.x(), event.y());
        if (slot == null || !slot.hasItem()) return;
        qol$rmbDraggedIndices.add(slot.index);
        ClientPlayNetworking.send(new MouseTweaksMoveOnePayload(List.of(slot.index)));
        cir.setReturnValue(true);
    }

    @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
    private void onDrag(MouseButtonEvent event, double deltaX, double deltaY,
                        CallbackInfoReturnable<Boolean> cir) {
        if (!QolConfig.getInstance().mouseTweaksEnabled) return;
        if (!event.hasShiftDown() || !menu.getCarried().isEmpty()) return;

        Slot slot = getHoveredSlot(event.x(), event.y());
        if (event.button() == 0) {
            cir.setReturnValue(true);
            if (slot != null && slot.hasItem() && qol$lmbDraggedIndices.add(slot.index)) {
                slotClicked(slot, slot.index, 0, ContainerInput.QUICK_MOVE);
            }
        } else if (event.button() == 1) {
            cir.setReturnValue(true);
            if (slot != null && slot.hasItem() && qol$rmbDraggedIndices.add(slot.index)) {
                ClientPlayNetworking.send(new MouseTweaksMoveOnePayload(List.of(slot.index)));
            }
        }
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"))
    private void onRelease(MouseButtonEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (event.button() == 0) qol$lmbDraggedIndices.clear();
        else if (event.button() == 1) qol$rmbDraggedIndices.clear();
    }
}
