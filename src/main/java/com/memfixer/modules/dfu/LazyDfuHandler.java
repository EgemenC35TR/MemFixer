package com.memfixer.modules.dfu;

import com.memfixer.config.MemFixerConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Modular handler responsible for deferring DataFixerUpper optimization work during bootstrap.
 * Bypasses redundant schema precomputations on startup.
 */
public final class LazyDfuHandler {
    private static final Logger LOGGER = LogManager.getLogger("MemFixer/DFU");

    private LazyDfuHandler() {}

    public static boolean shouldBypassOptimization() {
        if (MemFixerConfig.LAZY_DFU) {
            if (MemFixerConfig.ENABLE_LOGGING) {
                LOGGER.info("[MemFixer/DFU] DataFixer initialization deferred; precomputed rule schema evaluation bypassed.");
            }
            return true;
        }
        return false;
    }
}
