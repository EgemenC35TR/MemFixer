package com.memfixer.mixin.core;

import com.memfixer.modules.shape.ShapeDeduplicator;
import com.memfixer.modules.state.FastNeighbourTable;
import net.minecraft.server.Bootstrap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Bootstrap lifecycle hook for memory logging and constraints.
 */
@Mixin(Bootstrap.class)
public abstract class BootstrapMemoryMixin {
    private static final Logger LOGGER = LogManager.getLogger("MemFixer/Bootstrap");

    @Inject(method = "bootStrap", at = @At("HEAD"))
    private static void onBootstrapStart(CallbackInfo ci) {
        if (com.memfixer.config.MemFixerConfig.ENABLE_LOGGING) {
            LOGGER.info("[MemFixer] Bootstrap phase initiated with memory constraint monitoring.");
        }
    }

    @Inject(method = "bootStrap", at = @At("RETURN"))
    private static void onBootstrapEnd(CallbackInfo ci) {
        if (com.memfixer.config.MemFixerConfig.ENABLE_LOGGING) {
            LOGGER.info("[MemFixer] Bootstrap phase finalized. Registry structures normalized.");
            LOGGER.info("[MemFixer] FastNeighbourTable: Compacted {} StateHolder neighbour tables ({} empty singletons).",
                    FastNeighbourTable.getCompactedCount(), FastNeighbourTable.getEmptyCount());
        }
        ShapeDeduplicator.logStats();
    }
}
