package com.memfixer.mixin.model;

import com.memfixer.modules.model.ConditionDeduplicator;
import net.minecraft.client.renderer.block.model.multipart.KeyValueCondition;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Mixin intercepting KeyValueCondition.getBlockStatePredicate to return deduplicated canonical
 * FastPropertyPredicate singletons, eliminating ~177,000 lambdas and ~178,000 captured Optionals.
 */
@Mixin(KeyValueCondition.class)
@SuppressWarnings("null")
public abstract class KeyValueConditionMixin {

    @Shadow
    @Final
    private String key;

    @Shadow
    @Final
    private String value;

    @Inject(
            method = "getBlockStatePredicate",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onGetBlockStatePredicate(StateDefinition<Block, BlockState> stateDefinition, Property<?> property, String valueString, CallbackInfoReturnable<Predicate<BlockState>> cir) {
        if (!com.memfixer.config.MemFixerConfig.DEDUPLICATE_CONDITIONS) {
            return;
        }
        Optional<?> optional = property.getValue(valueString);
        if (optional.isEmpty()) {
            throw new RuntimeException(String.format(
                    Locale.ROOT,
                    "Unknown value '%s' for property '%s' on '%s' in '%s'",
                    valueString, this.key, stateDefinition.getOwner(), this.value
            ));
        }
        Comparable<?> val = (Comparable<?>) optional.get();
        cir.setReturnValue(ConditionDeduplicator.getPropertyPredicate(property, val));
    }
}
