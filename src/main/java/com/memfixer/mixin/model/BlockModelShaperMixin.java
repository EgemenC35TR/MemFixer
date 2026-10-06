package com.memfixer.mixin.model;

import com.memfixer.modules.model.BlockStateModelOptimizer;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

/**
 * Mixin delegating statePropertiesToString serialization to BlockStateModelOptimizer.
 * Bypasses redundant StringBuilder allocations across 25,000+ blockstate models.
 */
@Mixin(BlockModelShaper.class)
public abstract class BlockModelShaperMixin {

    @Inject(
            method = "statePropertiesToString",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void onStatePropertiesToString(Map<Property<?>, Comparable<?>> properties, CallbackInfoReturnable<String> cir) {
        if (!com.memfixer.config.MemFixerConfig.DEDUPLICATE_CONDITIONS) {
            return;
        }
        cir.setReturnValue(BlockStateModelOptimizer.getOptimizedStatePropertiesString(properties));
    }
}
