package com.memfixer.mixin.font;

import net.minecraft.client.gui.font.CodepointMap;
import net.minecraft.client.gui.font.providers.UnihexProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(UnihexProvider.class)
public interface UnihexProviderAccessor {
    @SuppressWarnings("rawtypes")
    @Invoker("<init>")
    static UnihexProvider create(CodepointMap glyphs) {
        throw new UnsupportedOperationException();
    }
}
