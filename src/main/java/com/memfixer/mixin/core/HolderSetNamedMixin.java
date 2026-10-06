package com.memfixer.mixin.core;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.memfixer.modules.tag.TagDeduplicator;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collection;
import java.util.List;

/**
 * Mixin intercepting HolderSet.Named.bind to deduplicate identical Holder lists
 * across modded registry tags.
 */
@Mixin(HolderSet.Named.class)
public abstract class HolderSetNamedMixin {

    @WrapOperation(
            method = "bind",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;copyOf(Ljava/util/Collection;)Ljava/util/List;"
            )
    )
    private <T> List<Holder<T>> onWrapBind(Collection<Holder<T>> collection, Operation<List<Holder<T>>> original) {
        return TagDeduplicator.canonicalizeHolderList(collection, original);
    }
}
