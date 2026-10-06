package com.memfixer.mixin.model;

import com.memfixer.config.MemFixerConfig;
import com.memfixer.modules.model.ModelCleaner;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin purging ModelBakery unbaked caches once resource reloading completes.
 * Preserves the modelBakery reference to prevent NullPointerException in NeoForge's getModelBakery().
 */
@Mixin(ModelManager.class)
public abstract class ModelManagerMixin {

    @Shadow
    private ModelBakery modelBakery;

    @Inject(
            method = "apply(Lnet/minecraft/client/resources/model/ModelManager$ReloadState;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At("RETURN")
    )
    private void onApplyPost(@Coerce Object reloadState, ProfilerFiller profiler, CallbackInfo ci) {
        if (MemFixerConfig.RECLAIM_POST_LAUNCH && this.modelBakery != null) {
            ModelCleaner.cleanPostBake(this.modelBakery);
        }
    }
}
