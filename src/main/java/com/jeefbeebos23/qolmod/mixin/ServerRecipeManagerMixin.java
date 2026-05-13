package com.jeefbeebos23.qolmod.mixin;

import com.jeefbeebos23.qolmod.config.QolConfig;
import com.jeefbeebos23.qolmod.features.SlabRecipeFeature;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Mixin(RecipeManager.class)
public class ServerRecipeManagerMixin {

    @Shadow
    private RecipeMap recipes;

    @Inject(
        method = "apply(Lnet/minecraft/world/item/crafting/RecipeMap;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
        at = @At("RETURN")
    )
    private void injectSlabRecipes(RecipeMap recipeMap, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci) {
        if (!QolConfig.getInstance().slabRecipeEnabled) return;

        Collection<RecipeHolder<?>> existing = this.recipes.values();
        List<RecipeHolder<?>> allRecipes = new ArrayList<>(existing);
        allRecipes.addAll(SlabRecipeFeature.buildRecipes());
        this.recipes = RecipeMap.create(allRecipes);
    }
}
