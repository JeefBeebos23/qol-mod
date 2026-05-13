package com.jeefbeebos23.qolmod.mixin;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractFurnaceMenu.class)
public interface AbstractFurnaceScreenHandlerAccessor {
    @Accessor("container")
    Container getInventory();
}
