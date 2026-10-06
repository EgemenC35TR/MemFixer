package com.memfixer.modules;

import com.google.common.collect.ImmutableSortedMap;
import com.memfixer.modules.state.StatePropertyDeduplicator;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Subsystem 14: StatePropertyDeduplicator Tests")
class StatePropertyDeduplicatorTest {

    private final BooleanProperty propWaterlogged = BooleanProperty.create("waterlogged");
    private final BooleanProperty propFacing = BooleanProperty.create("facing");

    @Test
    @DisplayName("Should return canonical ImmutableSortedMap for identical property maps")
    @SuppressWarnings("unchecked")
    void testPropertyMapDeduplication() {
        ImmutableSortedMap<String, Property<?>> map1 = ImmutableSortedMap.<String, Property<?>>naturalOrder()
                .put("waterlogged", propWaterlogged)
                .put("facing", propFacing)
                .build();

        ImmutableSortedMap<String, Property<?>> map2 = ImmutableSortedMap.<String, Property<?>>naturalOrder()
                .put("waterlogged", propWaterlogged)
                .put("facing", propFacing)
                .build();

        assertNotSame(map1, map2);
        assertEquals(map1, map2);

        com.llamalad7.mixinextras.injector.wrapoperation.Operation<ImmutableSortedMap<String, Property<?>>> op =
                args -> ImmutableSortedMap.copyOf((java.util.Map<String, Property<?>>) args[0]);

        ImmutableSortedMap<String, Property<?>> interned1 = StatePropertyDeduplicator.canonicalize(map1, op);
        ImmutableSortedMap<String, Property<?>> interned2 = StatePropertyDeduplicator.canonicalize(map2, op);

        assertSame(interned1, interned2);
    }

    @Test
    @DisplayName("Should return empty map safely when null or empty is passed")
    void testNullSafety() {
        com.llamalad7.mixinextras.injector.wrapoperation.Operation<ImmutableSortedMap<String, Property<?>>> op =
                args -> ImmutableSortedMap.of();

        ImmutableSortedMap<String, Property<?>> empty1 = StatePropertyDeduplicator.canonicalize(null, op);
        ImmutableSortedMap<String, Property<?>> empty2 = StatePropertyDeduplicator.canonicalize(java.util.Map.of(), op);

        assertTrue(empty1.isEmpty());
        assertTrue(empty2.isEmpty());
    }

    @Test
    @DisplayName("Should be thread-safe during parallel registration")
    @SuppressWarnings("unchecked")
    void testConcurrentMapInterning() throws Exception {
        int threads = 8;
        int iterations = 500;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<Callable<ImmutableSortedMap<String, Property<?>>>> tasks = new ArrayList<>();

        com.llamalad7.mixinextras.injector.wrapoperation.Operation<ImmutableSortedMap<String, Property<?>>> op =
                args -> ImmutableSortedMap.copyOf((java.util.Map<String, Property<?>>) args[0]);

        for (int i = 0; i < threads * iterations; i++) {
            tasks.add(() -> {
                ImmutableSortedMap<String, Property<?>> map = ImmutableSortedMap.<String, Property<?>>naturalOrder()
                        .put("waterlogged", propWaterlogged)
                        .build();
                return StatePropertyDeduplicator.canonicalize(map, op);
            });
        }

        List<Future<ImmutableSortedMap<String, Property<?>>>> futures = executor.invokeAll(tasks);
        executor.shutdown();

        ImmutableSortedMap<String, Property<?>> first = futures.get(0).get();
        for (Future<ImmutableSortedMap<String, Property<?>>> future : futures) {
            assertSame(first, future.get());
        }
    }
}
