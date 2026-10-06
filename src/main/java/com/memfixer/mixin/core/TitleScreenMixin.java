package com.memfixer.mixin.core;

import com.memfixer.modules.reclaimer.MemoryReclaimer;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin triggering lifecycle-aware memory reclamation once TitleScreen is initialized and rendered.
 */
@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin {

    @Inject(method = "init", at = @At("RETURN"))
    private void onTitleScreenInit(CallbackInfo ci) {
        MemoryReclaimer.onTitleScreenLoaded();
    }
}
