package com.memfixer.mixin.chunk;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.memfixer.config.MemFixerConfig;
import com.memfixer.modules.chunk.ChunkStorageOptimizer;
import net.minecraft.util.ThreadingDetector;
import net.minecraft.util.ZeroBitStorage;
import net.minecraft.world.level.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin intercepting PalettedContainer to eliminate per-container synchronization object allocation churn
 * and share canonical ZeroBitStorage instances.
 * Uses @WrapOperation for 100% interoperability with Lithium, Radium, and other chunk optimization mods.
 */
@Mixin(PalettedContainer.class)
public abstract class PalettedContainerMixin {

    @Inject(method = "acquire", at = @At("HEAD"), cancellable = true)
    private void onAcquire(CallbackInfo ci) {
        if (MemFixerConfig.OPTIMIZE_CHUNK_STORAGE) {
            ci.cancel();
        }
    }

    @Inject(method = "release", at = @At("HEAD"), cancellable = true)
    private void onRelease(CallbackInfo ci) {
        if (MemFixerConfig.OPTIMIZE_CHUNK_STORAGE) {
            ci.cancel();
        }
    }

    @WrapOperation(
            method = "<init>(Lnet/minecraft/core/IdMap;Lnet/minecraft/world/level/chunk/PalettedContainer$Strategy;Lnet/minecraft/world/level/chunk/PalettedContainer$Configuration;Lnet/minecraft/util/BitStorage;Ljava/util/List;)V",
            at = @At(value = "NEW", target = "(Ljava/lang/String;)Lnet/minecraft/util/ThreadingDetector;")
    )
    private static ThreadingDetector wrapThreadingDetector1(String name, Operation<ThreadingDetector> original) {
        if (!MemFixerConfig.OPTIMIZE_CHUNK_STORAGE) {
            return original.call(name);
        }
        return ChunkStorageOptimizer.getThreadingDetector(name);
    }

    @WrapOperation(
            method = "<init>(Lnet/minecraft/core/IdMap;Lnet/minecraft/world/level/chunk/PalettedContainer$Strategy;Lnet/minecraft/world/level/chunk/PalettedContainer$Data;)V",
            at = @At(value = "NEW", target = "(Ljava/lang/String;)Lnet/minecraft/util/ThreadingDetector;")
    )
    private static ThreadingDetector wrapThreadingDetector2(String name, Operation<ThreadingDetector> original) {
        if (!MemFixerConfig.OPTIMIZE_CHUNK_STORAGE) {
            return original.call(name);
        }
        return ChunkStorageOptimizer.getThreadingDetector(name);
    }

    @WrapOperation(
            method = "<init>(Lnet/minecraft/core/IdMap;Ljava/lang/Object;Lnet/minecraft/world/level/chunk/PalettedContainer$Strategy;)V",
            at = @At(value = "NEW", target = "(Ljava/lang/String;)Lnet/minecraft/util/ThreadingDetector;")
    )
    private static ThreadingDetector wrapThreadingDetector3(String name, Operation<ThreadingDetector> original) {
        if (!MemFixerConfig.OPTIMIZE_CHUNK_STORAGE) {
            return original.call(name);
        }
        return ChunkStorageOptimizer.getThreadingDetector(name);
    }

    @WrapOperation(
            method = "unpack",
            at = @At(value = "NEW", target = "(I)Lnet/minecraft/util/ZeroBitStorage;")
    )
    private static ZeroBitStorage wrapUnpackZeroBitStorage(int size, Operation<ZeroBitStorage> original) {
        if (!MemFixerConfig.OPTIMIZE_CHUNK_STORAGE) {
            return original.call(size);
        }
        return ChunkStorageOptimizer.getZeroBitStorage(size);
    }
}
