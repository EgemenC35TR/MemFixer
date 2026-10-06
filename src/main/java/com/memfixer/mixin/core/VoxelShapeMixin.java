package com.memfixer.mixin.core;

import com.memfixer.config.MemFixerConfig;
import com.memfixer.modules.collision.CollisionDeduplicator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(VoxelShape.class)
public abstract class VoxelShapeMixin {

    @Shadow
    public abstract boolean isEmpty();

    @Unique
    private volatile List<AABB> memfixer$cachedAabbs;

    @Unique
    private volatile AABB memfixer$cachedBounds;

    @Inject(method = "toAabbs", at = @At("HEAD"), cancellable = true)
    private void memfixer$getCachedAabbs(CallbackInfoReturnable<List<AABB>> cir) {
        if (!MemFixerConfig.DEDUPLICATE_AABB_CACHE) {
            return;
        }

        List<AABB> cached = this.memfixer$cachedAabbs;
        if (cached != null) {
            CollisionDeduplicator.recordHit();
            cir.setReturnValue(cached);
            return;
        }

        VoxelShape self = (VoxelShape)(Object)this;
        List<AABB> computed = CollisionDeduplicator.computeAabbs(self);
        this.memfixer$cachedAabbs = computed;
        cir.setReturnValue(computed);
    }

    @Inject(method = "bounds", at = @At("HEAD"), cancellable = true)
    private void memfixer$getCachedBounds(CallbackInfoReturnable<AABB> cir) {
        if (!MemFixerConfig.DEDUPLICATE_AABB_CACHE) {
            return;
        }

        AABB cached = this.memfixer$cachedBounds;
        if (cached != null) {
            CollisionDeduplicator.recordHit();
            cir.setReturnValue(cached);
            return;
        }

        if (this.isEmpty()) {
            return;
        }

        VoxelShape self = (VoxelShape)(Object)this;
        AABB computed = CollisionDeduplicator.computeBounds(self);
        this.memfixer$cachedBounds = computed;
        cir.setReturnValue(computed);
    }
}
