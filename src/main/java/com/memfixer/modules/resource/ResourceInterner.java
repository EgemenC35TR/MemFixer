package com.memfixer.modules.resource;

import com.memfixer.config.MemFixerConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Modular interning delegator using JVM native StringTable.
 * Leverages native C++ SymbolTable to eliminate Java heap pool retention overhead.
 */
public final class ResourceInterner {
    private static final Logger LOGGER = LogManager.getLogger("MemFixer/ResourceInterner");

    private ResourceInterner() {}

    public static String intern(String str) {
        if (!MemFixerConfig.INTERN_RESOURCES || str == null || str.isEmpty()) {
            return str;
        }
        // Fast-path: "minecraft" is 99% of namespaces and already a compile-time constant
        if ("minecraft".equals(str)) {
            return "minecraft";
        }
        if (str.length() > 64) {
            return str;
        }
        return str.intern();
    }

    public static void compactPools() {
        if (MemFixerConfig.ENABLE_LOGGING) {
            LOGGER.info("[MemFixer/ResourceInterner] JVM native StringTable delegation active.");
        }
    }
}
