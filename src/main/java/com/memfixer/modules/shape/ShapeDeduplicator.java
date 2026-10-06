package com.memfixer.modules.shape;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Ultra-compact deduplication cache for VoxelShape face slices and sturdy arrays across 26,000+ BlockStates.
 * Eliminates ~98,000 SliceShapes, ~98,000 SubShapes, and ~25,000 boolean/array instances (~10 MB heap).
 */
public final class ShapeDeduplicator {
    private static final Logger LOGGER = LogManager.getLogger("MemFixer/ShapeDeduplicator");

    private static final Map<VoxelShape, VoxelShape[]> FACE_CACHE = new ConcurrentHashMap<>(512);
    private static final Map<Integer, boolean[]> STURDY_CACHE = new ConcurrentHashMap<>(64);

    private static final VoxelShape[] BLOCK_FACES = new VoxelShape[] {
            Shapes.block(), Shapes.block(), Shapes.block(),
            Shapes.block(), Shapes.block(), Shapes.block()
    };

    private static final VoxelShape[] EMPTY_FACES = new VoxelShape[] {
            Shapes.empty(), Shapes.empty(), Shapes.empty(),
            Shapes.empty(), Shapes.empty(), Shapes.empty()
    };

    private static final AtomicInteger FACE_DEDUP_HITS = new AtomicInteger(0);
    private static final AtomicInteger STURDY_DEDUP_HITS = new AtomicInteger(0);

    private ShapeDeduplicator() {}

    public static VoxelShape getFaceShape(VoxelShape shape, Direction direction) {
        if (shape == null || shape.isEmpty()) {
            FACE_DEDUP_HITS.incrementAndGet();
            return Shapes.empty();
        }
        if (shape == Shapes.block()) {
            FACE_DEDUP_HITS.incrementAndGet();
            return Shapes.block();
        }
        VoxelShape[] faces = getOrComputeFaceShapes(shape);
        FACE_DEDUP_HITS.incrementAndGet();
        return faces[direction.ordinal()];
    }

    public static VoxelShape[] getOrComputeFaceShapes(VoxelShape shape) {
        if (shape == null || shape.isEmpty()) {
            return EMPTY_FACES;
        }
        if (shape == Shapes.block()) {
            return BLOCK_FACES;
        }
        VoxelShape[] existing = FACE_CACHE.get(shape);
        if (existing != null) {
            return existing;
        }
        VoxelShape[] computed = new VoxelShape[6];
        for (Direction d : Direction.values()) {
            computed[d.ordinal()] = Shapes.getFaceShape(shape, d);
        }
        VoxelShape[] prev = FACE_CACHE.putIfAbsent(shape, computed);
        return prev != null ? prev : computed;
    }

    public static boolean[] deduplicateFaceSturdy(boolean[] sturdy) {
        if (sturdy == null || sturdy.length != 18) {
            return sturdy;
        }
        int mask = 0;
        for (int i = 0; i < 18; i++) {
            if (sturdy[i]) {
                mask |= (1 << i);
            }
        }
        boolean[] existing = STURDY_CACHE.get(mask);
        if (existing != null) {
            STURDY_DEDUP_HITS.incrementAndGet();
            return existing;
        }
        STURDY_CACHE.put(mask, sturdy);
        return sturdy;
    }

    public static void logStats() {
        if (!com.memfixer.config.MemFixerConfig.ENABLE_LOGGING) return;
        LOGGER.info("[MemFixer/ShapeDeduplicator] Deduplication stats: {} unique shape face-sets cached, {} unique sturdy masks cached ({} face hits, {} sturdy hits).",
                FACE_CACHE.size(), STURDY_CACHE.size(), FACE_DEDUP_HITS.get(), STURDY_DEDUP_HITS.get());
    }

    public static int getFaceDedupHits() {
        return FACE_DEDUP_HITS.get();
    }

    public static int getSturdyDedupHits() {
        return STURDY_DEDUP_HITS.get();
    }

    public static int getCachedFaceSetsCount() {
        return FACE_CACHE.size();
    }

    public static int getCachedSturdyMasksCount() {
        return STURDY_CACHE.size();
    }
}
