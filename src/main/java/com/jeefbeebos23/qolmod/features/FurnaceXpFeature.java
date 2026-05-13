package com.jeefbeebos23.qolmod.features;

import com.jeefbeebos23.qolmod.config.QolConfig;
import com.jeefbeebos23.qolmod.mixin.AbstractFurnaceBlockEntityMixin;
import com.jeefbeebos23.qolmod.mixin.AbstractFurnaceScreenHandlerAccessor;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.screen.AbstractFurnaceScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class FurnaceXpFeature {

    public static volatile int clientStoredXp = 0;

    public static void register() {
        PayloadTypeRegistry.playC2S().register(FurnaceXpRequestPayload.ID, FurnaceXpRequestPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(FurnaceXpCollectPayload.ID, FurnaceXpCollectPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(FurnaceXpSyncPayload.ID, FurnaceXpSyncPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(FurnaceXpRequestPayload.ID, (payload, ctx) ->
            ctx.server().execute(() -> {
                if (!QolConfig.getInstance().furnaceXpEnabled) return;
                AbstractFurnaceBlockEntity furnace = getFurnaceFromHandler(ctx.player());
                if (furnace == null) return;
                int xp = computeStoredXp(furnace, ctx.player().getServerWorld());
                ServerPlayNetworking.send(ctx.player(), new FurnaceXpSyncPayload(xp));
            })
        );

        ServerPlayNetworking.registerGlobalReceiver(FurnaceXpCollectPayload.ID, (payload, ctx) ->
            ctx.server().execute(() -> {
                if (!QolConfig.getInstance().furnaceXpEnabled) return;
                ServerWorld sw = ctx.player().getServerWorld();
                AbstractFurnaceBlockEntity furnace = getFurnaceFromHandler(ctx.player());
                if (furnace == null) return;
                AbstractFurnaceBlockEntityMixin acc = (AbstractFurnaceBlockEntityMixin) furnace;
                Reference2IntOpenHashMap<RegistryKey<Recipe<?>>> recipesUsed = acc.getRecipesUsed();
                if (recipesUsed.isEmpty()) return;

                Vec3d center = furnace.getPos().toCenterPos();
                recipesUsed.reference2IntEntrySet().forEach(entry -> {
                    sw.getRecipeManager().get(entry.getKey()).ifPresent(recipe -> {
                        if (recipe.value() instanceof AbstractCookingRecipe cookingRecipe) {
                            float experience = cookingRecipe.getExperience();
                            int count = entry.getIntValue();
                            int xpAmount = MathHelper.floor((float) count * experience);
                            float remainder = MathHelper.fractionalPart((float) count * experience);
                            if (remainder != 0.0F && Math.random() < (double) remainder) {
                                xpAmount++;
                            }
                            if (xpAmount > 0) {
                                ExperienceOrbEntity.spawn(sw, center, xpAmount);
                            }
                        }
                    });
                });

                acc.setRecipesUsed(new Reference2IntOpenHashMap<>());
                furnace.markDirty();
                ServerPlayNetworking.send(ctx.player(), new FurnaceXpSyncPayload(0));
            })
        );
    }

    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(FurnaceXpSyncPayload.ID, (payload, ctx) ->
            ctx.client().execute(() -> clientStoredXp = payload.xp())
        );
    }

    private static AbstractFurnaceBlockEntity getFurnaceFromHandler(ServerPlayerEntity player) {
        if (!(player.currentScreenHandler instanceof AbstractFurnaceScreenHandler furnaceHandler)) return null;
        net.minecraft.inventory.Inventory inv = ((AbstractFurnaceScreenHandlerAccessor) furnaceHandler).getInventory();
        if (inv instanceof AbstractFurnaceBlockEntity furnace) {
            return furnace;
        }
        return null;
    }

    public static int computeStoredXp(AbstractFurnaceBlockEntity furnace, ServerWorld world) {
        AbstractFurnaceBlockEntityMixin acc = (AbstractFurnaceBlockEntityMixin) furnace;
        int[] total = {0};
        acc.getRecipesUsed().reference2IntEntrySet().forEach(entry -> {
            world.getRecipeManager().get(entry.getKey()).ifPresent((RecipeEntry<?> recipe) -> {
                if (recipe.value() instanceof AbstractCookingRecipe cookingRecipe) {
                    int count = entry.getIntValue();
                    total[0] += MathHelper.floor((float) count * cookingRecipe.getExperience());
                }
            });
        });
        return total[0];
    }
}
