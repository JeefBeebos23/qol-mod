package com.jeefbeebos23.qolmod.mixin;

import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.recipe.Recipe;
import net.minecraft.registry.RegistryKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractFurnaceBlockEntity.class)
public interface AbstractFurnaceBlockEntityMixin {
    @Accessor("recipesUsed")
    Reference2IntOpenHashMap<RegistryKey<Recipe<?>>> getRecipesUsed();

    @Accessor("recipesUsed")
    void setRecipesUsed(Reference2IntOpenHashMap<RegistryKey<Recipe<?>>> recipesUsed);
}
