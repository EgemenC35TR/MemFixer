package com.memfixer.mixin.model;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

/**
 * Accessor for ModelBakery internal transient caches.
 */
@Mixin(ModelBakery.class)
public interface ModelBakeryAccessor {
    @Accessor("modelResources")
    Map<ResourceLocation, BlockModel> getModelResources();

    @Accessor("modelResources")
    @org.spongepowered.asm.mixin.Mutable
    void setModelResources(Map<ResourceLocation, BlockModel> modelResources);

    @Accessor("unbakedCache")
    Map<ResourceLocation, UnbakedModel> getUnbakedCache();

    @Accessor("topLevelModels")
    Map<?, UnbakedModel> getTopLevelModels();

    @Accessor("bakedCache")
    Map<?, ?> getBakedCache();
}
