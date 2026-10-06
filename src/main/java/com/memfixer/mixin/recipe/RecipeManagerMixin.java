package com.memfixer.mixin.recipe;

import com.memfixer.config.MemFixerConfig;
import com.memfixer.modules.recipe.IngredientDeduplicator;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * Mixin intercepting RecipeManager loading and network replacement to sweep and canonicalize
 * all recipe ingredients and log deduplication metrics.
 */
@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {

    @Shadow
    private Map<ResourceLocation, RecipeHolder<?>> byName;

    @Inject(
            method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At("RETURN")
    )
    private void onApplyPost(Map<?, ?> recipes, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci) {
        if (!MemFixerConfig.DEDUPLICATE_INGREDIENTS) return;
        memfixer$sweepRecipes();
        IngredientDeduplicator.logStats();
    }

    @Inject(method = "replaceRecipes", at = @At("RETURN"))
    private void onReplaceRecipesPost(Iterable<?> recipes, CallbackInfo ci) {
        if (!MemFixerConfig.DEDUPLICATE_INGREDIENTS) return;
        memfixer$sweepRecipes();
        IngredientDeduplicator.logStats();
    }

    @Unique
    private void memfixer$sweepRecipes() {
        if (this.byName == null) return;
        for (RecipeHolder<?> holder : this.byName.values()) {
            if (holder == null || holder.value() == null) continue;
            try {
                NonNullList<Ingredient> ingredients = holder.value().getIngredients();
                if (ingredients != null && !ingredients.isEmpty()) {
                    IngredientDeduplicator.canonicalizeList(ingredients);
                }
            } catch (Throwable ignored) {
                // Defensive: in case third-party modded recipe returns immutable NonNullList
            }
        }
    }
}
