package com.memfixer.modules.model;

import com.memfixer.config.MemFixerConfig;
import net.minecraft.client.renderer.block.model.BakedQuad;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Modular zero-allocation open-addressing geometry deduplicator.
 * Replaces lossy direct-mapped caches and heavy wrapper-based HashSets with a dynamically-resizing
 * open-addressing linear probing table. Guarantees 100% collision-free deduplication during model baking
 * without allocating a single wrapper object, and flushes to 0 bytes post-bake.
 */
public final class QuadDeduplicator {
    private static final Logger LOGGER = LogManager.getLogger("MemFixer/QuadDeduplicator");
    private static final int INITIAL_CAPACITY = 8192;
    private static final float LOAD_FACTOR = 0.70f;

    private static final Object LOCK = new Object();
    private static BakedQuad[] table;
    private static int[] hashes;
    private static int mask;
    private static int size;
    private static int threshold;

    private static final AtomicLong TOTAL_REQUESTS = new AtomicLong();
    private static final AtomicLong DEDUPLICATED_COUNT = new AtomicLong();

    private QuadDeduplicator() {}

    private static void ensureCapacityLocked() {
        if (table == null) {
            table = new BakedQuad[INITIAL_CAPACITY];
            hashes = new int[INITIAL_CAPACITY];
            mask = INITIAL_CAPACITY - 1;
            size = 0;
            threshold = (int) (INITIAL_CAPACITY * LOAD_FACTOR);
        }
    }

    public static BakedQuad deduplicate(BakedQuad quad) {
        if (!MemFixerConfig.DEDUPLICATE_QUADS || quad == null) {
            return quad;
        }

        TOTAL_REQUESTS.incrementAndGet();

        int[] vertices = quad.getVertices();
        int hash = murmur3(vertices);
        hash = 31 * hash + quad.getTintIndex();
        hash = 31 * hash + (quad.getDirection() != null ? quad.getDirection().ordinal() : -1);
        hash = 31 * hash + (quad.getSprite() != null ? quad.getSprite().hashCode() : 0);
        hash = 31 * hash + (quad.isShade() ? 1231 : 1237);
        hash = 31 * hash + (quad.hasAmbientOcclusion() ? 1249 : 1259);
        if (hash == 0) hash = 1;

        synchronized (LOCK) {
            ensureCapacityLocked();

            int currMask = mask;
            int idx = hash & currMask;

            while (true) {
                BakedQuad existing = table[idx];
                if (existing == null) {
                    table[idx] = quad;
                    hashes[idx] = hash;
                    size++;
                    if (size >= threshold) {
                        rehashLocked();
                    }
                    return quad;
                }

                if (hashes[idx] == hash && quadsEqual(existing, quad)) {
                    DEDUPLICATED_COUNT.incrementAndGet();
                    return existing;
                }

                idx = (idx + 1) & currMask;
            }
        }
    }

    private static void rehashLocked() {
        int newCap = table.length * 2;
        BakedQuad[] newTable = new BakedQuad[newCap];
        int[] newHashes = new int[newCap];
        int newMask = newCap - 1;

        for (int i = 0; i < table.length; i++) {
            BakedQuad q = table[i];
            if (q != null) {
                int h = hashes[i];
                int idx = h & newMask;
                while (newTable[idx] != null) {
                    idx = (idx + 1) & newMask;
                }
                newTable[idx] = q;
                newHashes[idx] = h;
            }
        }

        table = newTable;
        hashes = newHashes;
        mask = newMask;
        threshold = (int) (newCap * LOAD_FACTOR);
    }

    private static boolean verticesEqual(int[] a, int[] b) {
        if (a == b) return true;
        if (a == null || b == null || a.length != b.length) return false;
        for (int i = 0; i < a.length; i++) {
            int v1 = a[i];
            int v2 = b[i];
            if (v1 != v2) {
                if ((v1 == 0x80000000 && v2 == 0) || (v2 == 0x80000000 && v1 == 0)) {
                    continue;
                }
                return false;
            }
        }
        return true;
    }

    private static boolean quadsEqual(BakedQuad a, BakedQuad b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        if (a.getClass() != b.getClass()) return false;
        if (a.getTintIndex() != b.getTintIndex() || a.isShade() != b.isShade()) return false;
        if (a.hasAmbientOcclusion() != b.hasAmbientOcclusion()) return false;
        if (a.getDirection() != b.getDirection()) return false;
        if (a.getSprite() != b.getSprite()) return false;
        return verticesEqual(a.getVertices(), b.getVertices());
    }

    private static int murmur3(int[] data) {
        if (data == null) return 0;
        int h = 0x9747b28c;
        for (int k : data) {
            if (k == 0x80000000) k = 0;
            k *= 0xcc9e2d51;
            k = Integer.rotateLeft(k, 15);
            k *= 0x1b873593;

            h ^= k;
            h = Integer.rotateLeft(h, 13);
            h = h * 5 + 0xe6546b64;
        }
        h ^= data.length * 4;
        h ^= h >>> 16;
        h *= 0x85ebca6b;
        h ^= h >>> 13;
        h *= 0xc2b2ae35;
        h ^= h >>> 16;
        return h;
    }

    public static void clearCache() {
        synchronized (LOCK) {
            table = null;
            hashes = null;
            size = 0;
            mask = 0;
            threshold = 0;
        }
        if (com.memfixer.config.MemFixerConfig.ENABLE_LOGGING) {
            LOGGER.info("[MemFixer/QuadDeduplicator] Geometry cache table flushed. Quad and sprite references released.");
        }
    }

    public static void logStats() {
        if (!com.memfixer.config.MemFixerConfig.ENABLE_LOGGING) return;
        long total = TOTAL_REQUESTS.get();
        long deduped = DEDUPLICATED_COUNT.get();
        LOGGER.info("[MemFixer/QuadDeduplicator] Open-addressing geometry pool: {} requests, {} duplicate quads deduplicated.",
                total, deduped);
    }

    public static long getDeduplicatedCount() {
        return DEDUPLICATED_COUNT.get();
    }

    public static long getTotalRequests() {
        return TOTAL_REQUESTS.get();
    }
}
