package com.memfixer.mixin.core;

import net.minecraft.client.renderer.CubeMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Eliminates legacy 4-pass jitter overdraw in TitleScreen CubeMap rendering.
 * Reduces 24 draw calls and mesh allocations per frame to 6, dropping GPU fill-rate
 * and heap allocation churn by 75% while producing a crisper panorama.
 */
@Mixin(CubeMap.class)
public abstract class CubeMapMixin {

    @ModifyConstant(
            method = "render",
            constant = @Constant(intValue = 4, ordinal = 0)
    )
    private int setSinglePassPanorama(int original) {
        return com.memfixer.config.MemFixerConfig.OPTIMIZE_PANORAMA ? 1 : original;
    }
}
