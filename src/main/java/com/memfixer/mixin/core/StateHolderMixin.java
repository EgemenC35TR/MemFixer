package com.memfixer.mixin.core;

import com.google.common.collect.Table;
import com.memfixer.modules.state.FastNeighbourTable;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * Mixin intercepting StateHolder.populateNeighbours to replace bulky Guava ArrayTable
 * and HashBasedTable instances with ultra-compact FastNeighbourTable arrays across 25,000+ BlockStates.
 */
@Mixin(StateHolder.class)
public abstract class StateHolderMixin<O, S> {

    @Shadow
    @Mutable
    private Table<Property<?>, Comparable<?>, S> neighbours;

    @org.spongepowered.asm.mixin.Unique
    private static final java.util.concurrent.atomic.AtomicBoolean MEMFIXER$FERRITE_LOGGED = new java.util.concurrent.atomic.AtomicBoolean(false);

    @Inject(
            method = "populateNeighbours",
            at = @At("RETURN")
    )
    private void onPopulateNeighboursPost(Map<Map<Property<?>, Comparable<?>>, S> states, CallbackInfo ci) {
        if (!com.memfixer.config.MemFixerConfig.COMPACT_BLOCKSTATES) {
            return;
        }
        if (this.neighbours == null) {
            FastNeighbourTable.recordEmpty();
            this.neighbours = FastNeighbourTable.empty();
        } else if (this.neighbours.getClass().getName().contains("ferritecore") || this.neighbours instanceof FastNeighbourTable) {
            if (this.neighbours.getClass().getName().contains("ferritecore")) {
                FastNeighbourTable.markFerriteCoreActive();
                if (MEMFIXER$FERRITE_LOGGED.compareAndSet(false, true) && com.memfixer.config.MemFixerConfig.ENABLE_LOGGING) {
                    org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger("MemFixer/StateHolder");
                    logger.info("[MemFixer/StateHolder] FerriteCore FastMap active on BlockStates. Yielding neighbour table compaction to FerriteCore.");
                    logger.info("[MemFixer/StateHolder] Notice: MemFixer includes built-in O(1) canonical FastNeighbourTable compaction when FerriteCore is not present.");
                }
            }
        } else if (this.neighbours.isEmpty()) {
            FastNeighbourTable.recordEmpty();
            this.neighbours = FastNeighbourTable.empty();
        } else {
            this.neighbours = FastNeighbourTable.create(this.neighbours);
        }
    }
}
