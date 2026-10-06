package com.memfixer.modules.chunk;

import com.memfixer.config.MemFixerConfig;
import net.minecraft.util.ThreadingDetector;
import net.minecraft.util.ZeroBitStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Memory reclamation engine for chunk paletted containers.
 * Replaces redundant ZeroBitStorage instances with immutable canonical singletons (4096 for block states, 64 for biomes)
 * and bypasses multi-object ThreadingDetector allocation churn on every chunk section.
 */
@SuppressWarnings("null")
public final class ChunkStorageOptimizer {
    private static final Logger LOGGER = LogManager.getLogger("MemFixer/Chunk");

    public static final ZeroBitStorage STATIC_4096 = new ZeroBitStorage(4096);
    public static final ZeroBitStorage STATIC_64 = new ZeroBitStorage(64);

    @SuppressWarnings("rawtypes")
    public static final Map EMPTY_STRUCTURE_MAP = new EmptyStructureMap();

    private static final AtomicLong ZERO_BIT_SAVED = new AtomicLong(0);
    private static final AtomicLong DETECTORS_SAVED = new AtomicLong(0);
    private static final AtomicLong CLIENT_CHUNKS_STRIPPED = new AtomicLong(0);

    private ChunkStorageOptimizer() {}

    /**
     * Strips unused structure starts and references from client-side chunks,
     * replacing per-chunk HashMaps with shared zero-allocation singleton maps.
     */
    @SuppressWarnings("unchecked")
    public static void stripClientStructureMaps(net.minecraft.world.level.chunk.LevelChunk chunk) {
        if (!MemFixerConfig.OPTIMIZE_CHUNK_STORAGE || chunk == null) return;
        try {
            if (chunk instanceof com.memfixer.mixin.chunk.ChunkAccessAccessor accessor) {
                accessor.setStructureStarts(EMPTY_STRUCTURE_MAP);
                accessor.setStructuresRefences(EMPTY_STRUCTURE_MAP);
                CLIENT_CHUNKS_STRIPPED.incrementAndGet();
            }
        } catch (Throwable t) {
            LOGGER.debug("[MemFixer/Chunk] Failed to strip structure maps: {}", t.getMessage());
        }
    }

    /**
     * Resolves canonical ZeroBitStorage singletons for empty and uniform chunk containers.
     *
     * @param size container capacity (4096 for 16x16x16 blockstates, 64 for 4x4x4 biomes)
     * @return shared immutable ZeroBitStorage instance
     */
    public static ZeroBitStorage getZeroBitStorage(int size) {
        if (!MemFixerConfig.OPTIMIZE_CHUNK_STORAGE) {
            return new ZeroBitStorage(size);
        }

        if (size == 4096) {
            ZERO_BIT_SAVED.incrementAndGet();
            return STATIC_4096;
        } else if (size == 64) {
            ZERO_BIT_SAVED.incrementAndGet();
            return STATIC_64;
        }

        return new ZeroBitStorage(size);
    }

    /**
     * Resolves the shared no-op ThreadingDetector singleton, eliminating per-container
     * Semaphore and ReentrantLock allocation churn.
     *
     * @param name detector name
     * @return shared DummyThreadingDetector singleton
     */
    public static ThreadingDetector getThreadingDetector(String name) {
        if (!MemFixerConfig.OPTIMIZE_CHUNK_STORAGE) {
            return new ThreadingDetector(name);
        }

        DETECTORS_SAVED.incrementAndGet();
        return DummyThreadingDetector.INSTANCE;
    }

    /**
     * Logs exact, fact-based runtime metrics for chunk storage compaction.
     */
    public static void logStats() {
        if (!MemFixerConfig.ENABLE_LOGGING) return;

        long detectors = DETECTORS_SAVED.get();
        long zeroBit = ZERO_BIT_SAVED.get();
        long stripped = CLIENT_CHUNKS_STRIPPED.get();

        if (detectors > 0 || zeroBit > 0 || stripped > 0) {
            long syncPrimitivesEliminated = detectors * 5; // ThreadingDetector + Semaphore + NonfairSync + ReentrantLock + NonfairSync
            LOGGER.info("[MemFixer/Chunk] Chunk storage compaction active: {} ThreadingDetectors bypassed ({} synchronization primitives eliminated), {} ZeroBitStorage allocations saved, {} client chunks stripped of structure maps.",
                    detectors, syncPrimitivesEliminated, zeroBit, stripped);
        }
    }

    public static long getZeroBitSaved() {
        return ZERO_BIT_SAVED.get();
    }

    public static long getDetectorsSaved() {
        return DETECTORS_SAVED.get();
    }

    /**
     * Zero-allocation, exception-safe empty map singleton for client chunk structure references.
     * Does not throw UnsupportedOperationException on clear() or put() to preserve mod compatibility.
     */
    public static final class EmptyStructureMap<K, V> extends java.util.AbstractMap<K, V> {
        @Override
        public int size() { return 0; }

        @Override
        public boolean isEmpty() { return true; }

        @Override
        public boolean containsKey(Object key) { return false; }

        @Override
        public boolean containsValue(Object value) { return false; }

        @Override
        public V get(Object key) { return null; }

        @Override
        public V put(K key, V value) { return null; }

        @Override
        public V remove(Object key) { return null; }

        @Override
        public void clear() { /* no-op */ }

        @Override
        public java.util.Set<java.util.Map.Entry<K, V>> entrySet() { return java.util.Collections.emptySet(); }
    }
}
