package com.jeefbeebos23.qolmod.features;

import com.jeefbeebos23.qolmod.config.QolConfig;
import com.jeefbeebos23.qolmod.mixin.AbstractFurnaceBlockEntityMixin;
import com.jeefbeebos23.qolmod.mixin.AbstractFurnaceScreenHandlerAccessor;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.phys.Vec3;

public class FurnaceXpFeature {

    public static volatile int clientStoredXp = 0;

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(FurnaceXpRequestPayload.ID, FurnaceXpRequestPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(FurnaceXpCollectPayload.ID, FurnaceXpCollectPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(FurnaceXpSyncPayload.ID, FurnaceXpSyncPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(FurnaceXpRequestPayload.ID, (payload, ctx) ->
            ctx.server().execute(() -> {
                if (!QolConfig.getInstance().furnaceXpEnabled) return;
                AbstractFurnaceBlockEntity furnace = getFurnaceFromHandler(ctx.player());
                if (furnace == null) return;
                int xp = computeStoredXp(furnace, ctx.player().level());
                ServerPlayNetworking.send(ctx.player(), new FurnaceXpSyncPayload(xp));
            })
        );

        ServerPlayNetworking.registerGlobalReceiver(FurnaceXpCollectPayload.ID, (payload, ctx) ->
            ctx.server().execute(() -> {
                if (!QolConfig.getInstance().furnaceXpEnabled) return;
                ServerLevel sw = ctx.player().level();
                AbstractFurnaceBlockEntity furnace = getFurnaceFromHandler(ctx.player());
                if (furnace == null) return;
                AbstractFurnaceBlockEntityMixin acc = (AbstractFurnaceBlockEntityMixin) furnace;
                Reference2IntOpenHashMap<ResourceKey<Recipe<?>>> recipesUsed = acc.getRecipesUsed();
                if (recipesUsed.isEmpty()) return;

                Vec3 center = furnace.getBlockPos().getCenter();
                recipesUsed.reference2IntEntrySet().forEach(entry -> {
                    sw.getServer().getRecipeManager().byKey(entry.getKey()).ifPresent(recipe -> {
                        if (recipe.value() instanceof AbstractCookingRecipe cookingRecipe) {
                            float experience = cookingRecipe.experience();
                            int count = entry.getIntValue();
                            int xpAmount = Mth.floor((float) count * experience);
                            float remainder = Mth.frac((float) count * experience);
                            if (remainder != 0.0F && Math.random() < (double) remainder) {
                                xpAmount++;
                            }
                            if (xpAmount > 0) {
                                ExperienceOrb.award(sw, center, xpAmount);
                            }
                        }
                    });
                });

                acc.setRecipesUsed(new Reference2IntOpenHashMap<>());
                furnace.setChanged();
                ServerPlayNetworking.send(ctx.player(), new FurnaceXpSyncPayload(0));
            })
        );
    }

    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(FurnaceXpSyncPayload.ID, (payload, ctx) ->
            ctx.client().execute(() -> clientStoredXp = payload.xp())
        );
    }

    private static AbstractFurnaceBlockEntity getFurnaceFromHandler(ServerPlayer player) {
        if (!(player.containerMenu instanceof AbstractFurnaceMenu furnaceHandler)) return null;
        Container inv = ((AbstractFurnaceScreenHandlerAccessor) furnaceHandler).getInventory();
        if (inv instanceof AbstractFurnaceBlockEntity furnace) {
            return furnace;
        }
        return null;
    }

    public static int computeStoredXp(AbstractFurnaceBlockEntity furnace, ServerLevel world) {
        AbstractFurnaceBlockEntityMixin acc = (AbstractFurnaceBlockEntityMixin) furnace;
        int[] total = {0};
        acc.getRecipesUsed().reference2IntEntrySet().forEach(entry -> {
            world.getServer().getRecipeManager().byKey(entry.getKey()).ifPresent((RecipeHolder<?> recipe) -> {
                if (recipe.value() instanceof AbstractCookingRecipe cookingRecipe) {
                    int count = entry.getIntValue();
                    total[0] += Mth.floor((float) count * cookingRecipe.experience());
                }
            });
        });
        return total[0];
    }
}
