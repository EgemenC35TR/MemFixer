package com.memfixer.modules.model;

import com.memfixer.config.MemFixerConfig;
import com.memfixer.mixin.model.ModelBakeryAccessor;
import net.minecraft.client.resources.model.ModelBakery;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;

/**
 * Modular cleaner for clearing pre-baking and post-baking unbaked model caches.
 */
public final class ModelCleaner {
    private static final Logger LOGGER = LogManager.getLogger("MemFixer/ModelCleaner");

    private ModelCleaner() {}

    public static void cleanPreBake(ModelBakery bakery) {
        QuadDeduplicator.clearCache();
        if (MemFixerConfig.ENABLE_LOGGING) {
            LOGGER.info("[MemFixer/ModelCleaner] Model baking started. Geometry deduplication table armed.");
        }
    }

    public static void cleanPostBake(ModelBakery bakery) {
        if (!MemFixerConfig.RECLAIM_POST_LAUNCH) {
            return;
        }

        if (bakery instanceof ModelBakeryAccessor accessor) {
            int resourceCount = accessor.getModelResources() != null ? accessor.getModelResources().size() : 0;
            int unbaked = accessor.getUnbakedCache() != null ? accessor.getUnbakedCache().size() : 0;
            int topLevel = accessor.getTopLevelModels() != null ? accessor.getTopLevelModels().size() : 0;
            int bakedCache = accessor.getBakedCache() != null ? accessor.getBakedCache().size() : 0;

            if (resourceCount == 0 && unbaked == 0 && topLevel == 0 && bakedCache == 0) {
                return;
            }

            if (accessor.getModelResources() != null && resourceCount > 0) {
                accessor.setModelResources(Collections.emptyMap());
            }
            if (accessor.getUnbakedCache() != null) accessor.getUnbakedCache().clear();
            if (accessor.getTopLevelModels() != null) accessor.getTopLevelModels().clear();
            if (accessor.getBakedCache() != null) accessor.getBakedCache().clear();

            QuadDeduplicator.clearCache();

            if (MemFixerConfig.ENABLE_LOGGING) {
                LOGGER.info("[MemFixer/ModelCleaner] Post-bake memory reclamation complete: {} AST trees, {} unbaked models, {} top-level entries, {} lookup keys released.",
                        resourceCount, unbaked, topLevel, bakedCache);
            }
        }
    }
}
