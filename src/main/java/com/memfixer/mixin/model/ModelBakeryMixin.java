package com.memfixer.mixin.model;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.memfixer.config.MemFixerConfig;
import com.memfixer.modules.model.ModelCleaner;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Mixin transforming ModelBakery.bakeModels into an in-place streaming pipeline.
 * Iterates through topLevelModels and immediately removes each model from retention maps
 * upon baking, eliminating dual-cache memory spikes and preventing GC thrashing.
 * Uses @WrapOperation for seamless chaining with other model loading mods.
 */
@Mixin(ModelBakery.class)
public abstract class ModelBakeryMixin {

    @Inject(
            method = "bakeModels(Lnet/minecraft/client/resources/model/ModelBakery$TextureGetter;)V",
            at = @At("HEAD")
    )
    private void onBakeModelsPre(ModelBakery.TextureGetter textureGetter, CallbackInfo ci) {
        if (MemFixerConfig.RECLAIM_POST_LAUNCH || MemFixerConfig.DEDUPLICATE_QUADS) {
            ModelCleaner.cleanPreBake((ModelBakery) (Object) this);
        }
    }

    @WrapOperation(
            method = "bakeModels(Lnet/minecraft/client/resources/model/ModelBakery$TextureGetter;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Map;forEach(Ljava/util/function/BiConsumer;)V"
            )
    )
    private void onWrapStreamingBakeForEach(Map<ModelResourceLocation, UnbakedModel> map, BiConsumer<ModelResourceLocation, UnbakedModel> action, Operation<Void> original) {
        if (!MemFixerConfig.RECLAIM_POST_LAUNCH) {
            original.call(map, action);
            return;
        }

        Iterator<Map.Entry<ModelResourceLocation, UnbakedModel>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<ModelResourceLocation, UnbakedModel> entry = iterator.next();
            ModelResourceLocation loc = entry.getKey();
            UnbakedModel unbaked = entry.getValue();

            action.accept(loc, unbaked);

            iterator.remove();
        }
    }

    @Inject(
            method = "bakeModels(Lnet/minecraft/client/resources/model/ModelBakery$TextureGetter;)V",
            at = @At("RETURN")
    )
    private void onBakeModelsPost(ModelBakery.TextureGetter textureGetter, CallbackInfo ci) {
        if (MemFixerConfig.RECLAIM_POST_LAUNCH) {
            ModelCleaner.cleanPostBake((ModelBakery) (Object) this);
        }
    }
}
