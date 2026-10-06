package com.memfixer.mixin.model;

import com.memfixer.modules.model.QuadDeduplicator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

/**
 * Thin mixin delegating quad deduplication to the modular QuadDeduplicator
 * and compacting face collections on model build to eliminate sparse array overhead.
 */
@Mixin(SimpleBakedModel.Builder.class)
public abstract class BakedModelDeduplicationMixin {

    @Shadow
    @Mutable
    private List<BakedQuad> unculledFaces;

    @Shadow
    @Mutable
    private Map<Direction, List<BakedQuad>> culledFaces;

    @ModifyVariable(
            method = "addCulledFace",
            at = @At("HEAD"),
            argsOnly = true
    )
    private BakedQuad deduplicateCulledQuad(BakedQuad quad) {
        return com.memfixer.config.MemFixerConfig.DEDUPLICATE_QUADS ? QuadDeduplicator.deduplicate(quad) : quad;
    }

    @ModifyVariable(
            method = "addUnculledFace",
            at = @At("HEAD"),
            argsOnly = true
    )
    private BakedQuad deduplicateUnculledQuad(BakedQuad quad) {
        return com.memfixer.config.MemFixerConfig.DEDUPLICATE_QUADS ? QuadDeduplicator.deduplicate(quad) : quad;
    }

    @Inject(method = "build", at = @At("HEAD"))
    private void onBuildCompact(CallbackInfoReturnable<BakedModel> cir) {
        if (!com.memfixer.config.MemFixerConfig.DEDUPLICATE_QUADS) {
            return;
        }
        if (this.unculledFaces != null) {
            if (this.unculledFaces.isEmpty()) {
                this.unculledFaces = Collections.emptyList();
            } else if (this.unculledFaces.size() == 1) {
                this.unculledFaces = Collections.singletonList(this.unculledFaces.get(0));
            } else if (this.unculledFaces instanceof ArrayList<BakedQuad> al) {
                al.trimToSize();
            }
        }

        if (this.culledFaces != null) {
            if (this.culledFaces.isEmpty()) {
                this.culledFaces = Collections.emptyMap();
            } else {
                for (Map.Entry<Direction, List<BakedQuad>> entry : this.culledFaces.entrySet()) {
                    List<BakedQuad> list = entry.getValue();
                    if (list == null || list.isEmpty()) {
                        entry.setValue(Collections.emptyList());
                    } else if (list.size() == 1) {
                        entry.setValue(Collections.singletonList(list.get(0)));
                    } else if (list instanceof ArrayList<BakedQuad> al) {
                        al.trimToSize();
                    }
                }
            }
        }
    }
}
