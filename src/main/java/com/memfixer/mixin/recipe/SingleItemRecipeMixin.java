package com.memfixer.mixin.recipe;

import com.memfixer.config.MemFixerConfig;
import com.memfixer.modules.recipe.IngredientDeduplicator;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin intercepting SingleItemRecipe initialization (smelting, blasting, smoking, campfire, stonecutting)
 * to deduplicate the primary ingredient into shared canonical instances.
 */
@Mixin(SingleItemRecipe.class)
public abstract class SingleItemRecipeMixin {

    @Shadow
    @Mutable
    protected Ingredient ingredient;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInitPost(RecipeType<?> type, RecipeSerializer<?> serializer, String group, Ingredient ingredient, ItemStack result, CallbackInfo ci) {
        if (MemFixerConfig.DEDUPLICATE_INGREDIENTS && this.ingredient != null) {
            this.ingredient = IngredientDeduplicator.canonicalize(this.ingredient);
            IngredientDeduplicator.recordRecipeOptimized();
        }
    }
}
