package com.memfixer.modules;

import com.memfixer.modules.model.ConditionDeduplicator;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Subsystem 8: ConditionDeduplicator Tests")
class ConditionDeduplicatorTest {

    private final BooleanProperty testProp = BooleanProperty.create("test_waterlogged");

    @Test
    @DisplayName("Should return canonical Predicate instance for identical property-value pair")
    void testPredicateDeduplication() {
        Predicate<BlockState> pred1 = ConditionDeduplicator.getPropertyPredicate(testProp, true);
        Predicate<BlockState> pred2 = ConditionDeduplicator.getPropertyPredicate(testProp, true);

        assertSame(pred1, pred2);
    }

    @Test
    @DisplayName("Should return distinct Predicate instances for different values")
    void testDifferentValues() {
        Predicate<BlockState> predTrue = ConditionDeduplicator.getPropertyPredicate(testProp, true);
        Predicate<BlockState> predFalse = ConditionDeduplicator.getPropertyPredicate(testProp, false);

        assertNotSame(predTrue, predFalse);
    }

    @Test
    @DisplayName("Should return canonical Pair instance for identical predicate and model")
    void testPairDeduplication() {
        Predicate<BlockState> pred = ConditionDeduplicator.getPropertyPredicate(testProp, true);

        Pair<Predicate<BlockState>, net.minecraft.client.resources.model.BakedModel> pair1 =
                ConditionDeduplicator.getCanonicalPair(pred, null);
        Pair<Predicate<BlockState>, net.minecraft.client.resources.model.BakedModel> pair2 =
                ConditionDeduplicator.getCanonicalPair(pred, null);

        assertSame(pair1, pair2);
    }
}
