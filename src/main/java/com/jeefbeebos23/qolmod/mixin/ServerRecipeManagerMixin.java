package com.jeefbeebos23.qolmod.mixin;

import com.jeefbeebos23.qolmod.config.QolConfig;
import com.jeefbeebos23.qolmod.features.SlabRecipeFeature;
import net.minecraft.recipe.PreparedRecipes;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Mixin(ServerRecipeManager.class)
public class ServerRecipeManagerMixin {

    @Shadow
    private PreparedRecipes preparedRecipes;

    @Inject(method = "apply(Lnet/minecraft/recipe/PreparedRecipes;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)V",
            at = @At("RETURN"))
    private void injectSlabRecipes(PreparedRecipes preparedRecipes, ResourceManager resourceManager, Profiler profiler, CallbackInfo ci) {
        if (!QolConfig.getInstance().slabRecipeEnabled) return;

        Collection<RecipeEntry<?>> existing = this.preparedRecipes.recipes();
        List<RecipeEntry<?>> allRecipes = new ArrayList<>(existing);
        allRecipes.addAll(SlabRecipeFeature.buildRecipes());
        this.preparedRecipes = PreparedRecipes.of(allRecipes);
    }
}
