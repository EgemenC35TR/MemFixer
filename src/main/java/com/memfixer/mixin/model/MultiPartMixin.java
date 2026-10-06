package com.memfixer.mixin.model;

import com.memfixer.modules.model.BlockStateModelOptimizer;
import net.minecraft.client.renderer.block.model.multipart.MultiPart;
import net.minecraft.client.renderer.block.model.multipart.Selector;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.List;

/**
 * Thin mixin eliminating flatMap stream overhead in MultiPart dependency resolution.
 */
@Mixin(MultiPart.class)
public abstract class MultiPartMixin {

    @Shadow
    public abstract List<Selector> getSelectors();

    @Inject(method = "getDependencies", at = @At("HEAD"), cancellable = true)
    private void onGetDependencies(CallbackInfoReturnable<Collection<ResourceLocation>> cir) {
        if (!com.memfixer.config.MemFixerConfig.DEDUPLICATE_CONDITIONS) {
            return;
        }
        cir.setReturnValue(BlockStateModelOptimizer.getOptimizedMultiPartDependencies(this.getSelectors()));
    }
}
