package com.memfixer.modules;

import com.memfixer.modules.collision.CollisionDeduplicator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Subsystem 15: CollisionDeduplicator Tests")
class CollisionDeduplicatorTest {

    @Test
    @DisplayName("Should return canonical FULL_CUBE singleton for standard 1x1x1 box")
    void testFullCubeSingleton() {
        AABB rawCube = new AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);
        AABB interned = CollisionDeduplicator.internAabb(rawCube);

        assertSame(CollisionDeduplicator.FULL_CUBE, interned);
    }

    @Test
    @DisplayName("Should deduplicate identical custom AABB coordinates")
    void testCustomAabbDeduplication() {
        AABB slab1 = new AABB(0.0, 0.0, 0.0, 1.0, 0.5, 1.0);
        AABB slab2 = new AABB(0.0, 0.0, 0.0, 1.0, 0.5, 1.0);

        assertNotSame(slab1, slab2);

        AABB interned1 = CollisionDeduplicator.internAabb(slab1);
        AABB interned2 = CollisionDeduplicator.internAabb(slab2);

        assertSame(interned1, interned2);
    }

    @Test
    @DisplayName("Should handle null AABB gracefully")
    void testNullAabbSafety() {
        assertNull(CollisionDeduplicator.internAabb(null));
    }

    @Test
    @DisplayName("Should return pre-allocated singletons for Shapes.empty() and Shapes.block()")
    void testShapePreallocatedSingletons() {
        List<AABB> emptyAabbs = CollisionDeduplicator.computeAabbs(Shapes.empty());
        assertSame(CollisionDeduplicator.EMPTY_LIST, emptyAabbs);
        assertTrue(emptyAabbs.isEmpty());

        List<AABB> blockAabbs = CollisionDeduplicator.computeAabbs(Shapes.block());
        assertSame(CollisionDeduplicator.FULL_CUBE_LIST, blockAabbs);
        assertEquals(1, blockAabbs.size());
        assertSame(CollisionDeduplicator.FULL_CUBE, blockAabbs.get(0));
    }

    @Test
    @DisplayName("Should be thread-safe under concurrent interning")
    void testConcurrentInterning() throws Exception {
        int threads = 8;
        int iterations = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<Callable<AABB>> tasks = new ArrayList<>();

        for (int i = 0; i < threads * iterations; i++) {
            tasks.add(() -> CollisionDeduplicator.internAabb(new AABB(0.0, 0.0, 0.0, 0.5, 0.5, 0.5)));
        }

        List<Future<AABB>> futures = executor.invokeAll(tasks);
        executor.shutdown();

        AABB first = futures.get(0).get();
        for (Future<AABB> future : futures) {
            assertSame(first, future.get());
        }
    }
}
