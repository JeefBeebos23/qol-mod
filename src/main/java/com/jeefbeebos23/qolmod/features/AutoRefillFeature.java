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

    private static final Map<UUID, Snapshot> prevState = new ConcurrentHashMap<>();

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
        Snapshot prev = prevState.get(player.getUUID());
        Snapshot curr = Snapshot.of(inv);

        if (prev != null) {
            boolean changed = false;
            for (int i = 0; i < 9; i++) {
                if (prev.ids[i] != null && curr.ids[i] == null
                        && curr.totalOf(prev.ids[i]) < prev.totalOf(prev.ids[i])) {
                    if (doRefill(inv, i, prev.ids[i])) changed = true;
                }
            }
            // Offhand is inventory slot 40, stored at index 40 in snapshot
            if (prev.ids[40] != null && curr.ids[40] == null
                    && curr.totalOf(prev.ids[40]) < prev.totalOf(prev.ids[40])) {
                if (doRefill(inv, 40, prev.ids[40])) changed = true;
            }
            if (changed) curr = Snapshot.of(inv);
        }

        prevState.put(player.getUUID(), curr);
    }

    // Moves the first matching stack from main inventory (slots 9-35) to targetSlot.
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

    // Snapshot of all 41 inventory slots (0-40) with item IDs and counts.
    private record Snapshot(String[] ids, int[] counts) {

        static Snapshot of(Inventory inv) {
            String[] ids = new String[41];
            int[] counts = new int[41];
            for (int i = 0; i < 41; i++) {
                ItemStack s = inv.getItem(i);
                if (!s.isEmpty()) {
                    Identifier key = BuiltInRegistries.ITEM.getKey(s.getItem());
                    if (key != null) {
                        ids[i] = key.toString();
                        counts[i] = s.getCount();
                    }
                }
            }
            return new Snapshot(ids, counts);
        }

        int totalOf(String itemId) {
            int total = 0;
            for (int i = 0; i < 41; i++) {
                if (itemId.equals(ids[i])) total += counts[i];
            }
            return total;
        }
    }
}
