package com.memfixer.modules.state;

import com.google.common.collect.ImmutableSortedMap;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.memfixer.config.MemFixerConfig;
import net.minecraft.world.level.block.state.properties.Property;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * High-performance canonicalization engine for StateDefinition property maps.
 * Pools identical ImmutableSortedMap instances across thousands of block and fluid definitions,
 * eliminating redundant string/property arrays and comparator references across modpacks.
 */
public final class StatePropertyDeduplicator {
    private static final Logger LOGGER = LogManager.getLogger("MemFixer/StatePropertyDeduplicator");

    private static final Map<ImmutableSortedMap<String, Property<?>>, ImmutableSortedMap<String, Property<?>>> MAP_POOL = new ConcurrentHashMap<>(256);
    private static final AtomicInteger TOTAL_REQUESTS = new AtomicInteger(0);
    private static final AtomicInteger DEDUP_HITS = new AtomicInteger(0);

    private StatePropertyDeduplicator() {}

    public static ImmutableSortedMap<String, Property<?>> canonicalize(
            Map<String, Property<?>> map,
            Operation<ImmutableSortedMap<String, Property<?>>> original
    ) {
        if (!MemFixerConfig.DEDUPLICATE_STATE_DEFINITIONS) {
            return original.call(map);
        }

        if (map == null || map.isEmpty()) {
            return ImmutableSortedMap.of();
        }

        TOTAL_REQUESTS.incrementAndGet();
        ImmutableSortedMap<String, Property<?>> created = original.call(map);
        ImmutableSortedMap<String, Property<?>> existing = MAP_POOL.putIfAbsent(created, created);
        if (existing != null) {
            DEDUP_HITS.incrementAndGet();
            return existing;
        }
        return created;
    }

    public static void logStats() {
        if (!MemFixerConfig.ENABLE_LOGGING) return;
        int hits = DEDUP_HITS.get();
        if (hits > 0) {
            LOGGER.info("[MemFixer/StatePropertyDeduplicator] StateDefinition property maps deduplicated: {} canonical maps ({} hits across {} definitions).",
                    MAP_POOL.size(), hits, TOTAL_REQUESTS.get());
        }
    }

    public static void compactPools() {
        logStats();
    }

    public static int getCanonicalMapsCount() {
        return MAP_POOL.size();
    }

    public static int getDedupHits() {
        return DEDUP_HITS.get();
    }

    public static int getTotalRequests() {
        return TOTAL_REQUESTS.get();
    }
}
