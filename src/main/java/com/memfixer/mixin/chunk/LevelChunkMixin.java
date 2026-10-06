package com.memfixer.mixin.chunk;

import com.memfixer.config.MemFixerConfig;
import com.memfixer.modules.chunk.ChunkStorageOptimizer;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin intercepting LevelChunk initialization to strip unused structure maps
 * from client-side chunks, saving heap allocation churn across loaded chunks.
 */
@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin {

    @Inject(
            method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/chunk/UpgradeData;Lnet/minecraft/world/ticks/LevelChunkTicks;Lnet/minecraft/world/ticks/LevelChunkTicks;J[Lnet/minecraft/world/level/chunk/LevelChunkSection;Lnet/minecraft/world/level/chunk/LevelChunk$PostLoadProcessor;Lnet/minecraft/world/level/levelgen/blending/BlendingData;)V",
            at = @At("RETURN")
    )
    private void onLevelChunkInit(CallbackInfo ci) {
        if (!MemFixerConfig.OPTIMIZE_CHUNK_STORAGE) {
            return;
        }
        LevelChunk chunk = (LevelChunk) (Object) this;
        if (chunk.getLevel() != null && chunk.getLevel().isClientSide()) {
            ChunkStorageOptimizer.stripClientStructureMaps(chunk);
        }
    }
}
