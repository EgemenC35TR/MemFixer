package com.memfixer.mixin.recipe;

import com.memfixer.config.MemFixerConfig;
import com.memfixer.modules.recipe.IngredientDeduplicator;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin intercepting ShapelessRecipe initialization to deduplicate identical Ingredient instances
 * across shapeless recipes in-place.
 */
@Mixin(ShapelessRecipe.class)
public abstract class ShapelessRecipeMixin {

    @Shadow
    NonNullList<Ingredient> ingredients;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInitPost(String group, CraftingBookCategory category, ItemStack result, NonNullList<Ingredient> ingredients, CallbackInfo ci) {
        if (MemFixerConfig.DEDUPLICATE_INGREDIENTS && this.ingredients != null) {
            IngredientDeduplicator.canonicalizeList(this.ingredients);
            IngredientDeduplicator.recordRecipeOptimized();
        }
    }
}
