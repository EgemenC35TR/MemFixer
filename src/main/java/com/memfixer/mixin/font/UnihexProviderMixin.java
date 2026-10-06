package com.memfixer.mixin.font;

import com.memfixer.modules.font.LazyUnihexHandler;
import net.minecraft.client.gui.font.providers.UnihexProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.InputStream;
import java.util.List;

@Mixin(targets = "net.minecraft.client.gui.font.providers.UnihexProvider$Definition")
public abstract class UnihexProviderMixin {

    @Shadow
    private List<?> sizeOverrides;

    @Inject(method = "loadData", at = @At("HEAD"), cancellable = true)
    private void onBeforeLoadData(InputStream stream, CallbackInfoReturnable<UnihexProvider> cir) {
        if (LazyUnihexHandler.shouldBypassEagerLoad()) {
            UnihexProvider provider = LazyUnihexHandler.buildLazyProvider(stream, this.sizeOverrides);
            if (provider != null) {
                cir.setReturnValue(provider);
            }
        }
    }
}
