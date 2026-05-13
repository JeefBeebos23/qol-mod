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
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.concurrent.atomic.AtomicInteger;

public class FurnaceXpFeature {

    /** Client-side cache: the most recently synced stored-XP for the open furnace. */
    public static volatile int clientStoredXp = 0;

    // -------------------------------------------------------------------------
    // Registration (runs on both logical server and integrated-server client)
    // -------------------------------------------------------------------------

    public static void register() {
        // Register all payload types once (both C2S and S2C)
        PayloadTypeRegistry.playC2S().register(FurnaceXpRequestPayload.ID, FurnaceXpRequestPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(FurnaceXpCollectPayload.ID, FurnaceXpCollectPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(FurnaceXpSyncPayload.ID, FurnaceXpSyncPayload.CODEC);

        // Handle XP request: compute stored XP from recipesUsed map and send to client
        ServerPlayNetworking.registerGlobalReceiver(FurnaceXpRequestPayload.ID, (payload, ctx) ->
            ctx.server().execute(() -> {
                if (!QolConfig.getInstance().furnaceXpEnabled) return;
                AbstractFurnaceBlockEntity furnace = getFurnaceFromHandler(ctx.player());
                if (furnace == null) return;
                int xp = computeStoredXp(furnace, ctx.player().getServerWorld());
                ServerPlayNetworking.send(ctx.player(), new FurnaceXpSyncPayload(xp));
            })
        );

        // Handle collect request: spawn XP orbs at the furnace, clear recipesUsed
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
                // Replicate vanilla dropExperience logic per recipe entry
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

                // Clear the accumulated recipes and notify client
                acc.setRecipesUsed(new Reference2IntOpenHashMap<>());
                furnace.markDirty();
                ServerPlayNetworking.send(ctx.player(), new FurnaceXpSyncPayload(0));
            })
        );
    }

    // -------------------------------------------------------------------------
    // Client-only registration
    // -------------------------------------------------------------------------

    public static void registerClient() {
        // Receive XP value from server and cache it for the screen mixin to read
        ClientPlayNetworking.registerGlobalReceiver(FurnaceXpSyncPayload.ID, (payload, ctx) ->
            ctx.client().execute(() -> clientStoredXp = payload.xp())
        );
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Gets the AbstractFurnaceBlockEntity from the player's currently open screen
     * handler. Returns null if the handler is not a furnace handler or its inventory
     * is not the actual block entity (e.g., client-side dummy inventory).
     */
    private static AbstractFurnaceBlockEntity getFurnaceFromHandler(net.minecraft.server.network.ServerPlayerEntity player) {
        if (!(player.currentScreenHandler instanceof AbstractFurnaceScreenHandler furnaceHandler)) return null;
        // On the server side, the handler's inventory IS the AbstractFurnaceBlockEntity
        net.minecraft.inventory.Inventory inv = ((AbstractFurnaceScreenHandlerAccessor) furnaceHandler).getInventory();
        if (inv instanceof AbstractFurnaceBlockEntity furnace) {
            return furnace;
        }
        return null;
    }

    /**
     * Computes the total stored XP from the furnace's recipesUsed map.
     * Uses the same formula as vanilla's dropExperience, but sums deterministically
     * (using floor) for display purposes.
     */
    public static int computeStoredXp(AbstractFurnaceBlockEntity furnace, ServerWorld world) {
        AbstractFurnaceBlockEntityMixin acc = (AbstractFurnaceBlockEntityMixin) furnace;
        AtomicInteger total = new AtomicInteger(0);
        acc.getRecipesUsed().reference2IntEntrySet().forEach(entry -> {
            world.getRecipeManager().get(entry.getKey()).ifPresent((RecipeEntry<?> recipe) -> {
                if (recipe.value() instanceof AbstractCookingRecipe cookingRecipe) {
                    float experience = cookingRecipe.getExperience();
                    int count = entry.getIntValue();
                    // Use floor for display (consistent with vanilla lower bound)
                    total.addAndGet(MathHelper.floor((float) count * experience));
                }
            });
        });
        return total.get();
    }
}
