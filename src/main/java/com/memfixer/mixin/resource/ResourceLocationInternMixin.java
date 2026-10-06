package com.memfixer.mixin.resource;

import com.memfixer.modules.resource.ResourceInterner;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Thin mixin delegating resource string interning to the modular ResourceInterner.
 */
@Mixin(ResourceLocation.class)
public abstract class ResourceLocationInternMixin {

    @ModifyVariable(
            method = "<init>(Ljava/lang/String;Ljava/lang/String;)V",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true
    )
    private static String internNamespace(String namespace) {
        return ResourceInterner.intern(namespace);
    }

    @ModifyVariable(
            method = "<init>(Ljava/lang/String;Ljava/lang/String;)V",
            at = @At("HEAD"),
            ordinal = 1,
            argsOnly = true
    )
    private static String internPath(String path) {
        return ResourceInterner.intern(path);
    }
}
