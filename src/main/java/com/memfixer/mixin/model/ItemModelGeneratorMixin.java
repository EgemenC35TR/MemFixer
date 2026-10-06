package com.memfixer.mixin.model;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.HashMap;

/**
 * Mixin optimizing ItemModelGenerator side element map allocation footprint.
 * Replaces default 16-element HashMap capacity with compact 2-element maps to prevent
 * GC thrashing and heap exhaustion during item voxel slicing under tight memory ceilings.
 * Uses @WrapOperation for 100% coexistence with FerriteCore, Sodium, and other modpack engines.
 */
@Mixin(ItemModelGenerator.class)
public abstract class ItemModelGeneratorMixin {

    @WrapOperation(
            method = "createSideElements",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/google/common/collect/Maps;newHashMap()Ljava/util/HashMap;",
                    remap = false
            )
    )
    private HashMap<?, ?> onWrapNewHashMap(Operation<HashMap<?, ?>> original) {
        if (!com.memfixer.config.MemFixerConfig.DEDUPLICATE_QUADS) {
            return original.call();
        }
        return new HashMap<>(2, 1.0f);
    }
}
