package com.memfixer.modules;

import com.memfixer.config.MemFixerConfig;
import com.memfixer.modules.chunk.ChunkStorageOptimizer;
import com.memfixer.modules.chunk.DummyThreadingDetector;
import net.minecraft.util.ThreadingDetector;
import net.minecraft.util.ZeroBitStorage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Subsystem 11: ChunkStorageOptimizer Tests")
class ChunkStorageOptimizerTest {

    @Test
    @DisplayName("Should return canonical ZeroBitStorage singletons for 4096 and 64")
    void testCanonicalZeroBitStorage() {
        MemFixerConfig.OPTIMIZE_CHUNK_STORAGE = true;

        ZeroBitStorage storage4096_1 = ChunkStorageOptimizer.getZeroBitStorage(4096);
        ZeroBitStorage storage4096_2 = ChunkStorageOptimizer.getZeroBitStorage(4096);
        assertSame(ChunkStorageOptimizer.STATIC_4096, storage4096_1);
        assertSame(storage4096_1, storage4096_2);

        ZeroBitStorage storage64_1 = ChunkStorageOptimizer.getZeroBitStorage(64);
        ZeroBitStorage storage64_2 = ChunkStorageOptimizer.getZeroBitStorage(64);
        assertSame(ChunkStorageOptimizer.STATIC_64, storage64_1);
        assertSame(storage64_1, storage64_2);
    }

    @Test
    @DisplayName("Should return DummyThreadingDetector singleton when optimization is enabled")
    void testDummyThreadingDetectorSingleton() {
        MemFixerConfig.OPTIMIZE_CHUNK_STORAGE = true;

        ThreadingDetector detector1 = ChunkStorageOptimizer.getThreadingDetector("paletted_container_1");
        ThreadingDetector detector2 = ChunkStorageOptimizer.getThreadingDetector("paletted_container_2");

        assertSame(DummyThreadingDetector.INSTANCE, detector1);
        assertSame(detector1, detector2);

        // Verify no deadlock or exception on checkAndLock/checkAndUnlock
        assertDoesNotThrow(() -> {
            detector1.checkAndLock();
            detector1.checkAndUnlock();
        });
    }

    @Test
    @DisplayName("EmptyStructureMap should be resilient and no-op without throwing exceptions")
    @SuppressWarnings("unchecked")
    void testEmptyStructureMapResilience() {
        var map = ChunkStorageOptimizer.EMPTY_STRUCTURE_MAP;
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
        assertNull(map.get("test"));

        // Put and clear should be safe no-ops
        assertDoesNotThrow(() -> {
            map.put("key", "val");
            map.clear();
            map.remove("key");
        });
        assertTrue(map.isEmpty());
    }
}
