package com.memfixer.mixin.model;

import com.memfixer.modules.model.BlockStateModelOptimizer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.List;

/**
 * Thin mixin delegating ModelGroupKey coloring value extraction to BlockStateModelOptimizer.
 * Eliminates heavy stream pipelines across 25,000+ block states during ModelBakery initialization.
 */
@Mixin(targets = "net.minecraft.client.resources.model.BlockStateModelLoader$ModelGroupKey")
public abstract class ModelGroupKeyMixin {

    @Inject(method = "getColoringValues", at = @At("HEAD"), cancellable = true)
    private static void onGetColoringValues(BlockState state, Collection<Property<?>> properties, CallbackInfoReturnable<List<Object>> cir) {
        if (!com.memfixer.config.MemFixerConfig.DEDUPLICATE_CONDITIONS) {
            return;
        }
        cir.setReturnValue(BlockStateModelOptimizer.getOptimizedColoringValues(state, properties));
    }
}
