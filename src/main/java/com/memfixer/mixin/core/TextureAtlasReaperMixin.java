package com.memfixer.mixin.core;

import com.memfixer.modules.texture.TextureReaperHandler;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Mixin intercepting TextureAtlas upload to release CPU-side pixel allocations post-upload.
 */
@Mixin(TextureAtlas.class)
public abstract class TextureAtlasReaperMixin {

    @Shadow
    private List<SpriteContents> sprites;

    @Inject(
            method = "upload(Lnet/minecraft/client/renderer/texture/SpriteLoader$Preparations;)V",
            at = @At("RETURN")
    )
    private void onUploadPost(SpriteLoader.Preparations preparations, CallbackInfo ci) {
        TextureReaperHandler.reapStaticSprites((TextureAtlas) (Object) this, this.sprites);
    }
}
