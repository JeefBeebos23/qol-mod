package com.jeefbeebos23.qolmod.features;

import com.jeefbeebos23.qolmod.config.QolConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AutoRefillFeature {

    // Item IDs for slots [0-8] (hotbar) and [9] (offhand), null = empty
    private static final Map<UUID, String[]> prevState = new ConcurrentHashMap<>();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(AutoRefillFeature::onTick);
    }

    private static void onTick(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!QolConfig.getInstance().autoRefillEnabled) {
                prevState.remove(player.getUUID());
                continue;
            }
            tick(player);
        }
    }

    private static void tick(ServerPlayer player) {
        Inventory inv = player.getInventory();
        String[] prev = prevState.get(player.getUUID());
        String[] curr = snapshot(inv);

        if (prev != null) {
            boolean changed = false;
            for (int i = 0; i < 9; i++) {
                if (prev[i] != null && curr[i] == null && doRefill(inv, i, prev[i])) {
                    changed = true;
                }
            }
            // Offhand is index 9, inventory slot 40
            if (prev[9] != null && curr[9] == null && doRefill(inv, 40, prev[9])) {
                changed = true;
            }
            if (changed) curr = snapshot(inv);
        }

        prevState.put(player.getUUID(), curr);
    }

    // Finds the first stack of itemId in main inventory (slots 9-35) and moves it to targetSlot.
    private static boolean doRefill(Inventory inv, int targetSlot, String itemId) {
        for (int i = 9; i < 36; i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;
            Identifier key = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (key != null && key.toString().equals(itemId)) {
                inv.setItem(targetSlot, stack.copy());
                inv.setItem(i, ItemStack.EMPTY);
                return true;
            }
        }
        return false;
    }

    private static String[] snapshot(Inventory inv) {
        String[] state = new String[10];
        for (int i = 0; i < 9; i++) {
            state[i] = itemId(inv.getItem(i));
        }
        state[9] = itemId(inv.getItem(40));
        return state;
    }

    private static String itemId(ItemStack stack) {
        if (stack.isEmpty()) return null;
        Identifier key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return key != null ? key.toString() : null;
    }
}
