package com.memfixer.mixin.model;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.memfixer.modules.model.ConditionDeduplicator;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.MultiPartBakedModel;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Predicate;

/**
 * Mixin intercepting MultiPartBakedModel$Builder.add to deduplicate (Predicate, BakedModel) pairs,
 * eliminating ~115,000 redundant ImmutablePair allocations.
 * Uses @WrapOperation to chain seamlessly with FerriteCore and other optimization mods.
 */
@Mixin(MultiPartBakedModel.Builder.class)
public abstract class MultiPartBakedModelBuilderMixin {

    @SuppressWarnings("unchecked")
    @WrapOperation(
            method = "add",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/apache/commons/lang3/tuple/Pair;of(Ljava/lang/Object;Ljava/lang/Object;)Lorg/apache/commons/lang3/tuple/Pair;"
            )
    )
    private Pair<Predicate<BlockState>, BakedModel> onWrapPairOf(Object left, Object right, Operation<Pair<Predicate<BlockState>, BakedModel>> original) {
        if (!com.memfixer.config.MemFixerConfig.DEDUPLICATE_CONDITIONS) {
            return original.call(left, right);
        }
        return ConditionDeduplicator.getCanonicalPair((Predicate<BlockState>) left, (BakedModel) right);
    }
}
