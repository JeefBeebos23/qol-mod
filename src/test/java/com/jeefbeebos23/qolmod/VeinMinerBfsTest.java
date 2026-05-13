package com.jeefbeebos23.qolmod;

import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

public class VeinMinerBfsTest {

    record Pos(int x, int y, int z) {}

    private Set<Pos> bfs(Pos origin, int max, Set<Pos> sameType) {
        Set<Pos> result = new java.util.LinkedHashSet<>();
        java.util.Queue<Pos> queue = new java.util.LinkedList<>();
        queue.add(origin);
        result.add(origin);
        while (!queue.isEmpty() && result.size() < max) {
            Pos p = queue.poll();
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        Pos n = new Pos(p.x() + dx, p.y() + dy, p.z() + dz);
                        if (!result.contains(n) && sameType.contains(n)) {
                            result.add(n);
                            queue.add(n);
                        }
                    }
                }
            }
        }
        return result;
    }

    @Test
    void findsAllConnectedBlocks() {
        Set<Pos> ore = Set.of(new Pos(0,0,0), new Pos(1,0,0), new Pos(2,0,0));
        Set<Pos> found = bfs(new Pos(0,0,0), 64, ore);
        assertEquals(3, found.size());
        assertTrue(found.containsAll(ore));
    }

    @Test
    void respectsMaxBlocks() {
        Set<Pos> ore = new HashSet<>();
        for (int i = 0; i < 10; i++) ore.add(new Pos(i, 0, 0));
        Set<Pos> found = bfs(new Pos(0,0,0), 3, ore);
        assertEquals(3, found.size());
    }

    @Test
    void returnsOnlyOriginWhenIsolated() {
        Set<Pos> ore = Set.of(new Pos(0,0,0));
        Set<Pos> found = bfs(new Pos(0,0,0), 64, ore);
        assertEquals(1, found.size());
    }

    @Test
    void findsDiagonallyConnectedBlocks() {
        Set<Pos> ore = Set.of(new Pos(0,0,0), new Pos(1,1,1));
        Set<Pos> found = bfs(new Pos(0,0,0), 64, ore);
        assertEquals(2, found.size());
    }
}
