package com.memfixer.mixin.core;

import com.google.common.collect.ImmutableSortedMap;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.memfixer.modules.state.StatePropertyDeduplicator;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

/**
 * Mixin intercepting StateDefinition initialization to deduplicate identical
 * ImmutableSortedMap property maps across block and fluid definitions.
 */
@Mixin(StateDefinition.class)
public abstract class StateDefinitionMixin {

    @WrapOperation(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/google/common/collect/ImmutableSortedMap;copyOf(Ljava/util/Map;)Lcom/google/common/collect/ImmutableSortedMap;"
            )
    )
    private ImmutableSortedMap<String, Property<?>> onWrapPropertiesByName(
            Map<String, Property<?>> map,
            Operation<ImmutableSortedMap<String, Property<?>>> original
    ) {
        return StatePropertyDeduplicator.canonicalize(map, original);
    }
}
