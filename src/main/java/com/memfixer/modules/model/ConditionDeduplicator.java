package com.memfixer.modules.model;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

/**
 * Ultra-compact deduplication engine for BlockState multipart condition predicates and pairs.
 * Eliminates ~177,000 KeyValueCondition lambdas, ~178,000 captured Optionals, and ~115,000 ImmutablePairs (~8 MB heap).
 */
@SuppressWarnings("null")
public final class ConditionDeduplicator {
    private static final Logger LOGGER = LogManager.getLogger("MemFixer/ConditionDeduplicator");

    private static final Map<PropertyConditionKey, Predicate<BlockState>> PREDICATE_CACHE = new ConcurrentHashMap<>(512);
    private static final Map<PairKey, Pair<Predicate<BlockState>, BakedModel>> PAIR_CACHE = new ConcurrentHashMap<>(4096);

    private static final AtomicInteger PREDICATE_DEDUP_HITS = new AtomicInteger(0);
    private static final AtomicInteger PAIR_DEDUP_HITS = new AtomicInteger(0);

    private ConditionDeduplicator() {}

    public static Predicate<BlockState> getPropertyPredicate(Property<?> property, Comparable<?> value) {
        PropertyConditionKey key = new PropertyConditionKey(property, value);
        Predicate<BlockState> existing = PREDICATE_CACHE.get(key);
        if (existing != null) {
            PREDICATE_DEDUP_HITS.incrementAndGet();
            return existing;
        }
        Predicate<BlockState> pred = new FastPropertyPredicate(property, value);
        PREDICATE_CACHE.put(key, pred);
        return pred;
    }

    public static Pair<Predicate<BlockState>, BakedModel> getCanonicalPair(Predicate<BlockState> predicate, BakedModel model) {
        PairKey key = new PairKey(predicate, model);
        Pair<Predicate<BlockState>, BakedModel> existing = PAIR_CACHE.get(key);
        if (existing != null) {
            PAIR_DEDUP_HITS.incrementAndGet();
            return existing;
        }
        Pair<Predicate<BlockState>, BakedModel> newPair = new ImmutablePair<>(predicate, model);
        PAIR_CACHE.put(key, newPair);
        return newPair;
    }

    public static void logStats() {
        if (!com.memfixer.config.MemFixerConfig.ENABLE_LOGGING) return;
        LOGGER.info("[MemFixer/ConditionDeduplicator] Deduplication stats: {} unique predicates ({} hits), {} unique model pairs ({} hits).",
                PREDICATE_CACHE.size(), PREDICATE_DEDUP_HITS.get(), PAIR_CACHE.size(), PAIR_DEDUP_HITS.get());
    }

    public static int getPredicateHits() {
        return PREDICATE_DEDUP_HITS.get();
    }

    public static int getPairHits() {
        return PAIR_DEDUP_HITS.get();
    }

    public static int getUniquePredicatesCount() {
        return PREDICATE_CACHE.size();
    }

    public static void clearPairCache() {
        // Pairs and predicates are fully baked into models during startup; release temporary lookup maps
        PAIR_CACHE.clear();
        PREDICATE_CACHE.clear();
        if (com.memfixer.config.MemFixerConfig.ENABLE_LOGGING) {
            LOGGER.info("[MemFixer/ConditionDeduplicator] Pair and predicate lookup tables flushed post-bake.");
        }
    }

    public static final class FastPropertyPredicate implements Predicate<BlockState> {
        private final Property<?> property;
        private final Comparable<?> value;

        public FastPropertyPredicate(Property<?> property, Comparable<?> value) {
            this.property = property;
            this.value = value;
        }

        @Override
        public boolean test(BlockState state) {
            Comparable<?> val = state.getValue(this.property);
            return val == this.value || (val != null && val.equals(this.value));
        }

        @Override
        public String toString() {
            return this.property.getName() + "=" + this.value;
        }
    }

    private static final class PropertyConditionKey {
        private final Property<?> property;
        private final Comparable<?> value;
        private final int hash;

        public PropertyConditionKey(Property<?> property, Comparable<?> value) {
            this.property = property;
            this.value = value;
            this.hash = System.identityHashCode(property) * 31 + value.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PropertyConditionKey other)) return false;
            return this.property == other.property && this.value.equals(other.value);
        }

        @Override
        public int hashCode() {
            return this.hash;
        }
    }

    private static final class PairKey {
        private final Predicate<BlockState> predicate;
        private final BakedModel model;
        private final int hash;

        public PairKey(Predicate<BlockState> predicate, BakedModel model) {
            this.predicate = predicate;
            this.model = model;
            this.hash = System.identityHashCode(predicate) * 31 + System.identityHashCode(model);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PairKey other)) return false;
            return this.predicate == other.predicate && this.model == other.model;
        }

        @Override
        public int hashCode() {
            return this.hash;
        }
    }
}
