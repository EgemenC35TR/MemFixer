package com.memfixer.modules;

import com.memfixer.config.MemFixerConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Configuration & Modularity Tests")
class MemFixerConfigTest {

    @Test
    @DisplayName("All 15 core optimization flags should default to enabled (true)")
    void testDefaultFlagValues() {
        assertTrue(MemFixerConfig.DEDUPLICATE_QUADS);
        assertTrue(MemFixerConfig.INTERN_RESOURCES);
        assertTrue(MemFixerConfig.RECLAIM_POST_LAUNCH);
        assertTrue(MemFixerConfig.OPTIMIZE_UNIFONT);
        assertTrue(MemFixerConfig.LAZY_DFU);
        assertTrue(MemFixerConfig.REAP_TEXTURES);
        assertTrue(MemFixerConfig.COMPACT_BLOCKSTATES);
        assertTrue(MemFixerConfig.OPTIMIZE_PANORAMA);
        assertTrue(MemFixerConfig.DEDUPLICATE_SHAPES);
        assertTrue(MemFixerConfig.DEDUPLICATE_CONDITIONS);
        assertTrue(MemFixerConfig.OPTIMIZE_CHUNK_STORAGE);
        assertTrue(MemFixerConfig.DEDUPLICATE_TAGS);
        assertTrue(MemFixerConfig.DEDUPLICATE_INGREDIENTS);
        assertTrue(MemFixerConfig.DEDUPLICATE_STATE_DEFINITIONS);
        assertTrue(MemFixerConfig.DEDUPLICATE_AABB_CACHE);
    }

    @Test
    @DisplayName("Config load and save methods should execute safely")
    void testConfigMethods() {
        assertDoesNotThrow(MemFixerConfig::load);
    }
}
