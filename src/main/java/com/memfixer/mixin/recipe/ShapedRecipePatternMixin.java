package com.memfixer.mixin.recipe;

import com.memfixer.config.MemFixerConfig;
import com.memfixer.modules.recipe.IngredientDeduplicator;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * Mixin intercepting ShapedRecipePattern initialization to deduplicate identical Ingredient instances
 * across shaped recipes in-place.
 */
@Mixin(ShapedRecipePattern.class)
public abstract class ShapedRecipePatternMixin {

    @Shadow
    private NonNullList<Ingredient> ingredients;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInitPost(int width, int height, NonNullList<Ingredient> ingredients, Optional<?> data, CallbackInfo ci) {
        if (MemFixerConfig.DEDUPLICATE_INGREDIENTS && this.ingredients != null) {
            IngredientDeduplicator.canonicalizeList(this.ingredients);
            IngredientDeduplicator.recordRecipeOptimized();
        }
    }
}
