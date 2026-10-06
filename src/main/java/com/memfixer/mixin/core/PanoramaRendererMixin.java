package com.memfixer.mixin.core;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PanoramaRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Eliminates 20 Hz saw-tooth tick-boundary jerking in TitleScreen panorama rotation
 * by replacing deltaTickResidual with smooth real-time frame delta from DeltaTracker.
 */
@Mixin(PanoramaRenderer.class)
public abstract class PanoramaRendererMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @ModifyVariable(
            method = "render",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 1
    )
    private float useRealtimeDelta(float originalDelta) {
        if (com.memfixer.config.MemFixerConfig.OPTIMIZE_PANORAMA && this.minecraft != null && this.minecraft.getTimer() != null) {
            float delta = this.minecraft.getTimer().getRealtimeDeltaTicks();
            return (delta > 0.0f && delta < 5.0f) ? delta : originalDelta;
        }
        return originalDelta;
    }
}
