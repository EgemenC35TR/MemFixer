package com.memfixer.mixin.chunk;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.memfixer.config.MemFixerConfig;
import com.memfixer.modules.chunk.ChunkStorageOptimizer;
import net.minecraft.util.ZeroBitStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Mixin intercepting PalettedContainer$Configuration.createData to redirect ZeroBitStorage allocations
 * to canonical immutable singletons.
 * Uses @WrapOperation for seamless chaining with Lithium and Radium.
 */
@Mixin(targets = "net.minecraft.world.level.chunk.PalettedContainer$Configuration")
public abstract class PalettedContainerConfigurationMixin {

    @WrapOperation(
            method = "createData",
            at = @At(value = "NEW", target = "(I)Lnet/minecraft/util/ZeroBitStorage;")
    )
    private ZeroBitStorage wrapCreateDataZeroBitStorage(int size, Operation<ZeroBitStorage> original) {
        if (!MemFixerConfig.OPTIMIZE_CHUNK_STORAGE) {
            return original.call(size);
        }
        return ChunkStorageOptimizer.getZeroBitStorage(size);
    }
}
