package com.memfixer.mixin.resource;

import com.memfixer.modules.resource.ResourceInterner;
import net.minecraft.client.resources.model.ModelResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Mixin canonicalizing ModelResourceLocation variant strings to minimize heap duplication.
 */
@Mixin(ModelResourceLocation.class)
public abstract class ModelResourceLocationInternMixin {

    @ModifyVariable(
            method = "<init>(Lnet/minecraft/resources/ResourceLocation;Ljava/lang/String;)V",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private static String internVariant(String variant) {
        return ResourceInterner.intern(variant);
    }
}
