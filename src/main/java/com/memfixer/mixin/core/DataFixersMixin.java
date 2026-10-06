package com.memfixer.mixin.core;

import com.memfixer.modules.dfu.LazyDfuHandler;
import com.mojang.datafixers.DSL;
import net.minecraft.util.datafix.DataFixers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Thin mixin intercepting DataFixers bootstrap optimization.
 */
@Mixin(DataFixers.class)
public abstract class DataFixersMixin {

    @Inject(method = "optimize", at = @At("HEAD"), cancellable = true)
    private static void onOptimize(Set<DSL.TypeReference> references, CallbackInfoReturnable<CompletableFuture<?>> cir) {
        if (LazyDfuHandler.shouldBypassOptimization()) {
            cir.setReturnValue(CompletableFuture.completedFuture(null));
        }
    }
}
