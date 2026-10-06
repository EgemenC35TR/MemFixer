package com.memfixer.mixin.model;

import com.memfixer.modules.model.BlockStateModelOptimizer;
import net.minecraft.client.renderer.block.model.MultiVariant;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.List;

/**
 * Thin mixin eliminating stream overhead in MultiVariant dependency resolution.
 */
@Mixin(MultiVariant.class)
public abstract class MultiVariantMixin {

    @Shadow
    public abstract List<Variant> getVariants();

    @Inject(method = "getDependencies", at = @At("HEAD"), cancellable = true)
    private void onGetDependencies(CallbackInfoReturnable<Collection<ResourceLocation>> cir) {
        if (!com.memfixer.config.MemFixerConfig.DEDUPLICATE_CONDITIONS) {
            return;
        }
        cir.setReturnValue(BlockStateModelOptimizer.getOptimizedMultiVariantDependencies(this.getVariants()));
    }
}
