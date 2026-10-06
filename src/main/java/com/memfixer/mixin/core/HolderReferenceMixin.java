package com.memfixer.mixin.core;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.memfixer.modules.tag.TagDeduplicator;
import net.minecraft.core.Holder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collection;
import java.util.Set;

/**
 * Mixin intercepting Holder.Reference.bindTags to deduplicate identical TagKey sets
 * across tens of thousands of registry entries.
 */
@Mixin(Holder.Reference.class)
public abstract class HolderReferenceMixin {

    @WrapOperation(
            method = "bindTags",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Set;copyOf(Ljava/util/Collection;)Ljava/util/Set;"
            )
    )
    private <E> Set<E> onWrapBindTags(Collection<E> collection, Operation<Set<E>> original) {
        return TagDeduplicator.canonicalizeTagSet(collection, original);
    }
}
