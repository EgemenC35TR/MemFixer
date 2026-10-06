package com.memfixer.mixin.core;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.memfixer.modules.shape.ShapeDeduplicator;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin intercepting BlockState Cache initialization to deduplicate VoxelShape occlusion arrays
 * and faceSturdy boolean arrays, eliminating ~197,000 duplicate SliceShapes and SubShapes.
 * Uses @WrapOperation to coexist seamlessly with FerriteCore blockstate cache mixins.
 */
@Mixin(targets = "net.minecraft.world.level.block.state.BlockBehaviour$BlockStateBase$Cache")
@SuppressWarnings("null")
public abstract class BlockStateCacheMixin {

    @Shadow
    @Mutable
    VoxelShape[] occlusionShapes;

    @Shadow
    @Mutable
    boolean[] faceSturdy;

    @WrapOperation(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/shapes/Shapes;getFaceShape(Lnet/minecraft/world/phys/shapes/VoxelShape;Lnet/minecraft/core/Direction;)Lnet/minecraft/world/phys/shapes/VoxelShape;"
            )
    )
    private VoxelShape onWrapGetFaceShape(VoxelShape shape, Direction direction, Operation<VoxelShape> original) {
        if (!com.memfixer.config.MemFixerConfig.DEDUPLICATE_SHAPES) {
            return original.call(shape, direction);
        }
        return ShapeDeduplicator.getFaceShape(shape, direction);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onCacheInitPost(BlockState state, CallbackInfo ci) {
        if (!com.memfixer.config.MemFixerConfig.DEDUPLICATE_SHAPES) {
            return;
        }
        if (this.occlusionShapes != null) {
            this.occlusionShapes = ShapeDeduplicator.getOrComputeFaceShapes(
                    state.getOcclusionShape(
                            net.minecraft.world.level.EmptyBlockGetter.INSTANCE,
                            net.minecraft.core.BlockPos.ZERO
                    )
            );
        }
        if (this.faceSturdy != null) {
            this.faceSturdy = ShapeDeduplicator.deduplicateFaceSturdy(this.faceSturdy);
        }
    }
}
