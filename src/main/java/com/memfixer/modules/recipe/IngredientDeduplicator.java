package com.memfixer.modules.recipe;

import com.memfixer.config.MemFixerConfig;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.crafting.Ingredient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * High-performance canonicalization engine for crafting recipe ingredients.
 * Pools identical Ingredient instances, eliminating tens of thousands of duplicate
 * Ingredient, ItemStack[], and IntList objects across modded recipe registries.
 */
public final class IngredientDeduplicator {
    private static final Logger LOGGER = LogManager.getLogger("MemFixer/IngredientDeduplicator");

    private static final Map<Ingredient, Ingredient> INGREDIENT_POOL = new ConcurrentHashMap<>(1024);
    private static final AtomicInteger TOTAL_PROCESSED = new AtomicInteger(0);
    private static final AtomicInteger DEDUP_HITS = new AtomicInteger(0);
    private static final AtomicInteger RECIPES_OPTIMIZED = new AtomicInteger(0);

    private IngredientDeduplicator() {}

    public static Ingredient canonicalize(Ingredient ingredient) {
        if (!MemFixerConfig.DEDUPLICATE_INGREDIENTS || ingredient == null) {
            return ingredient;
        }
        if (ingredient.isEmpty()) {
            return Ingredient.EMPTY;
        }

        TOTAL_PROCESSED.incrementAndGet();
        Ingredient existing = INGREDIENT_POOL.putIfAbsent(ingredient, ingredient);
        if (existing != null) {
            DEDUP_HITS.incrementAndGet();
            return existing;
        }
        return ingredient;
    }

    public static void canonicalizeList(NonNullList<Ingredient> ingredients) {
        if (!MemFixerConfig.DEDUPLICATE_INGREDIENTS || ingredients == null || ingredients.isEmpty()) {
            return;
        }

        for (int i = 0; i < ingredients.size(); i++) {
            Ingredient original = ingredients.get(i);
            if (original != null && !original.isEmpty()) {
                Ingredient canonical = canonicalize(original);
                if (canonical != original) {
                    ingredients.set(i, canonical);
                }
            }
        }
    }

    public static void recordRecipeOptimized() {
        RECIPES_OPTIMIZED.incrementAndGet();
    }

    public static void logStats() {
        if (!MemFixerConfig.ENABLE_LOGGING) return;
        int hits = DEDUP_HITS.get();
        if (hits > 0) {
            LOGGER.info("[MemFixer/IngredientDeduplicator] Recipe ingredients deduplicated: {} canonical ingredients ({} hits across {} recipes).",
                    INGREDIENT_POOL.size(), hits, RECIPES_OPTIMIZED.get());
        }
    }

    public static int getCanonicalCount() {
        return INGREDIENT_POOL.size();
    }

    public static int getDedupHits() {
        return DEDUP_HITS.get();
    }

    public static int getRecipesOptimized() {
        return RECIPES_OPTIMIZED.get();
    }

    public static int getTotalProcessed() {
        return TOTAL_PROCESSED.get();
    }
}
