package com.memfixer.modules;

import com.memfixer.config.MemFixerConfig;
import com.memfixer.modules.recipe.IngredientDeduplicator;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.crafting.Ingredient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Subsystem 13: IngredientDeduplicator Tests")
class IngredientDeduplicatorTest {

    @Test
    @DisplayName("Should return null for null input")
    void testNullSafety() {
        MemFixerConfig.DEDUPLICATE_INGREDIENTS = true;
        assertNull(IngredientDeduplicator.canonicalize(null));
    }

    @Test
    @DisplayName("Should handle NonNullList canonicalization safely on empty or null list")
    void testListCanonicalization() {
        MemFixerConfig.DEDUPLICATE_INGREDIENTS = true;

        assertDoesNotThrow(() -> IngredientDeduplicator.canonicalizeList(null));
        NonNullList<Ingredient> list = NonNullList.create();
        assertDoesNotThrow(() -> IngredientDeduplicator.canonicalizeList(list));
        assertEquals(0, list.size());
    }

    @Test
    @DisplayName("Should track telemetry counters safely")
    void testTelemetry() {
        assertDoesNotThrow(IngredientDeduplicator::logStats);
        assertTrue(IngredientDeduplicator.getTotalProcessed() >= 0);
        assertTrue(IngredientDeduplicator.getDedupHits() >= 0);
        assertTrue(IngredientDeduplicator.getRecipesOptimized() >= 0);
    }
}
