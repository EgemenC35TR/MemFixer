package com.memfixer.modules;

import com.memfixer.config.MemFixerConfig;
import com.memfixer.modules.model.QuadDeduplicator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Subsystem 1: QuadDeduplicator Tests")
class QuadDeduplicatorTest {

    @BeforeEach
    void setup() {
        MemFixerConfig.DEDUPLICATE_QUADS = true;
        QuadDeduplicator.clearCache();
    }

    @Test
    @DisplayName("Should deduplicate identical BakedQuad instances into canonical reference")
    void testQuadDeduplication() {
        int[] v1 = new int[32];
        v1[0] = 100;
        v1[15] = 200;

        int[] v2 = new int[32];
        v2[0] = 100;
        v2[15] = 200;

        BakedQuad q1 = new BakedQuad(v1, -1, Direction.UP, null, true, true);
        BakedQuad q2 = new BakedQuad(v2, -1, Direction.UP, null, true, true);

        assertNotSame(q1, q2);

        BakedQuad dedup1 = QuadDeduplicator.deduplicate(q1);
        BakedQuad dedup2 = QuadDeduplicator.deduplicate(q2);

        assertSame(q1, dedup1);
        assertSame(dedup1, dedup2);
    }

    @Test
    @DisplayName("Should handle null and disabled config safely")
    void testSafety() {
        assertNull(QuadDeduplicator.deduplicate(null));

        MemFixerConfig.DEDUPLICATE_QUADS = false;
        int[] v = new int[32];
        BakedQuad q = new BakedQuad(v, -1, Direction.DOWN, null, false, false);
        assertSame(q, QuadDeduplicator.deduplicate(q));
    }

    @Test
    @DisplayName("Should deduplicate quads with IEEE 754 negative zero float representations")
    void testFloatNegativeZeroEquivalence() {
        MemFixerConfig.DEDUPLICATE_QUADS = true;
        int[] v1 = new int[32];
        int[] v2 = new int[32];
        v1[0] = 100;
        v2[0] = 100;
        v1[5] = 0; // +0.0f
        v2[5] = 0x80000000; // -0.0f

        BakedQuad q1 = new BakedQuad(v1, -1, Direction.UP, null, true, true);
        BakedQuad q2 = new BakedQuad(v2, -1, Direction.UP, null, true, true);

        BakedQuad dedup1 = QuadDeduplicator.deduplicate(q1);
        BakedQuad dedup2 = QuadDeduplicator.deduplicate(q2);

        assertSame(dedup1, dedup2);
    }
}
