package com.jeefbeebos23.qolmod.mixin;

import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractFurnaceBlockEntity.class)
public interface AbstractFurnaceBlockEntityMixin {
    @Accessor("recipesUsed")
    Reference2IntOpenHashMap<ResourceKey<Recipe<?>>> getRecipesUsed();

    @Accessor("recipesUsed")
    void setRecipesUsed(Reference2IntOpenHashMap<ResourceKey<Recipe<?>>> recipesUsed);
}
