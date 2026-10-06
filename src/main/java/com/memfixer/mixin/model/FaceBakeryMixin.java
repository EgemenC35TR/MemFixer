package com.memfixer.mixin.model;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.memfixer.modules.model.QuadDeduplicator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin intercepting FaceBakery.bakeQuad to deduplicate vertex arrays and BakedQuad instances,
 * and caching Direction.values() in recalculateWinding to eliminate hundreds of thousands of
 * temporary enum clone array allocations.
 * Uses @WrapOperation for non-conflicting mod interoperability.
 */
@Mixin(FaceBakery.class)
public abstract class FaceBakeryMixin {

    @Unique
    private static final Direction[] MEMFIXER$CACHED_DIRECTIONS = Direction.values();

    @WrapOperation(
            method = "recalculateWinding",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/Direction;values()[Lnet/minecraft/core/Direction;"
            )
    )
    private Direction[] onWrapDirectionValues(Operation<Direction[]> original) {
        if (!com.memfixer.config.MemFixerConfig.DEDUPLICATE_QUADS) {
            return original.call();
        }
        return MEMFIXER$CACHED_DIRECTIONS;
    }

    @Inject(
            method = "bakeQuad",
            at = @At("RETURN"),
            cancellable = true
    )
    private void onBakeQuadPost(CallbackInfoReturnable<BakedQuad> cir) {
        if (com.memfixer.config.MemFixerConfig.DEDUPLICATE_QUADS) {
            cir.setReturnValue(QuadDeduplicator.deduplicate(cir.getReturnValue()));
        }
    }
}
