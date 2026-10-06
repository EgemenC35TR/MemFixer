package com.memfixer.modules.collision;

import com.google.common.collect.Lists;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * High-performance canonical pool and caching engine for VoxelShape bounding boxes and bounds.
 * Eliminates repetitive heap allocation churn during raycasting, collision queries, and rendering.
 */
public final class CollisionDeduplicator {
    private static final Logger LOGGER = LogManager.getLogger("MemFixer/Collision");

    public static final AABB FULL_CUBE = new AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);
    public static final List<AABB> FULL_CUBE_LIST = List.of(FULL_CUBE);
    public static final List<AABB> EMPTY_LIST = List.of();

    private static final ConcurrentHashMap<AABB, AABB> AABB_POOL = new ConcurrentHashMap<>();

    private static final AtomicInteger TOTAL_REQUESTS = new AtomicInteger(0);
    private static final AtomicInteger CACHE_HITS = new AtomicInteger(0);

    static {
        AABB_POOL.put(FULL_CUBE, FULL_CUBE);
    }

    private CollisionDeduplicator() {}

    public static void recordHit() {
        TOTAL_REQUESTS.incrementAndGet();
        CACHE_HITS.incrementAndGet();
    }

    /**
     * Interns an AABB into the canonical pool, returning a shared singleton instance.
     */
    public static AABB internAabb(AABB aabb) {
        if (aabb == null) {
            return null;
        }
        if (aabb.equals(FULL_CUBE)) {
            CACHE_HITS.incrementAndGet();
            return FULL_CUBE;
        }
        AABB existing = AABB_POOL.putIfAbsent(aabb, aabb);
        if (existing != null) {
            CACHE_HITS.incrementAndGet();
            return existing;
        }
        return aabb;
    }

    /**
     * Computes the unmodifiable list of interned bounding boxes for a given VoxelShape.
     */
    public static List<AABB> computeAabbs(VoxelShape shape) {
        TOTAL_REQUESTS.incrementAndGet();

        if (shape.isEmpty()) {
            CACHE_HITS.incrementAndGet();
            return EMPTY_LIST;
        }

        if (shape == Shapes.block()) {
            CACHE_HITS.incrementAndGet();
            return FULL_CUBE_LIST;
        }

        List<AABB> list = Lists.newArrayList();
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            AABB raw = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
            list.add(internAabb(raw));
        });

        return List.copyOf(list);
    }

    /**
     * Computes the interned outer bounds AABB for a given VoxelShape.
     */
    public static AABB computeBounds(VoxelShape shape) {
        TOTAL_REQUESTS.incrementAndGet();
        if (shape == Shapes.block()) {
            CACHE_HITS.incrementAndGet();
            return FULL_CUBE;
        }
        AABB raw = new AABB(
            shape.min(Direction.Axis.X),
            shape.min(Direction.Axis.Y),
            shape.min(Direction.Axis.Z),
            shape.max(Direction.Axis.X),
            shape.max(Direction.Axis.Y),
            shape.max(Direction.Axis.Z)
        );
        return internAabb(raw);
    }

    public static int getTotalRequests() {
        return TOTAL_REQUESTS.get();
    }

    public static int getCacheHits() {
        return CACHE_HITS.get();
    }

    public static int getCanonicalAabbsCount() {
        return AABB_POOL.size();
    }

    public static void logStats() {
        if (!com.memfixer.config.MemFixerConfig.ENABLE_LOGGING) return;
        LOGGER.info("[MemFixer/Collision] Bounding box pool: {} canonical AABBs cached. Requests: {}, Cache hits: {}",
            AABB_POOL.size(), TOTAL_REQUESTS.get(), CACHE_HITS.get());
    }
}
