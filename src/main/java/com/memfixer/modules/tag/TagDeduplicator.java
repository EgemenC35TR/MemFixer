package com.memfixer.modules.tag;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.memfixer.config.MemFixerConfig;
import net.minecraft.core.Holder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * High-performance canonicalization engine for registry tag sets and holder lists.
 * Eliminates duplicate Set<TagKey<?>> and List<Holder<?>> collections across
 * registry entries in modded environments.
 */
public final class TagDeduplicator {
    private static final Logger LOGGER = LogManager.getLogger("MemFixer/TagDeduplicator");

    private static final Map<Set<?>, Set<?>> TAG_SET_POOL = new ConcurrentHashMap<>(1024);
    private static final Map<List<?>, List<?>> HOLDER_LIST_POOL = new ConcurrentHashMap<>(1024);

    private static final AtomicInteger TAG_SET_HITS = new AtomicInteger(0);
    private static final AtomicInteger HOLDER_LIST_HITS = new AtomicInteger(0);

    private static String getHolderKey(Holder<?> holder) {
        if (holder == null) return "";
        if (holder.unwrapKey().isPresent()) {
            return holder.unwrapKey().get().location().toString();
        }
        Object val = holder.value();
        return val != null ? val.toString() : "";
    }

    @SuppressWarnings("rawtypes")
    private static final java.util.Comparator<Holder> HOLDER_COMPARATOR = (h1, h2) -> {
        if (h1 == h2) return 0;
        if (h1 == null) return -1;
        if (h2 == null) return 1;
        return getHolderKey(h1).compareTo(getHolderKey(h2));
    };

    private TagDeduplicator() {}

    @SuppressWarnings("unchecked")
    public static <E> Set<E> canonicalizeTagSet(Collection<E> collection, Operation<Set<E>> original) {
        if (!MemFixerConfig.DEDUPLICATE_TAGS) {
            return original.call(collection);
        }
        if (collection == null || collection.isEmpty()) {
            return Set.of();
        }

        Set<E> copied = original.call(collection);
        Set<?> existing = TAG_SET_POOL.putIfAbsent(copied, copied);
        if (existing != null) {
            TAG_SET_HITS.incrementAndGet();
            return (Set<E>) existing;
        }
        return copied;
    }

    @SuppressWarnings("unchecked")
    public static <T> List<Holder<T>> canonicalizeHolderList(Collection<Holder<T>> list, Operation<List<Holder<T>>> original) {
        if (!MemFixerConfig.DEDUPLICATE_TAGS) {
            return original.call(list);
        }
        if (list == null || list.isEmpty()) {
            return List.of();
        }

        List<Holder<T>> copied = original.call(list);
        int size = copied.size();
        if (size > 1) {
            Holder<T>[] arr = copied.toArray(new Holder[size]);
            java.util.Arrays.sort(arr, HOLDER_COMPARATOR);
            copied = List.of(arr);
        }

        List<?> existing = HOLDER_LIST_POOL.putIfAbsent(copied, copied);
        if (existing != null) {
            HOLDER_LIST_HITS.incrementAndGet();
            return (List<Holder<T>>) existing;
        }
        return copied;
    }

    public static void compactPools() {
        logStats();
    }

    public static void logStats() {
        if (!MemFixerConfig.ENABLE_LOGGING) return;
        int setHits = TAG_SET_HITS.get();
        int listHits = HOLDER_LIST_HITS.get();
        if (setHits > 0 || listHits > 0) {
            LOGGER.info("[MemFixer/TagDeduplicator] Tag collection deduplication: {} canonical tag sets ({} hits), {} canonical holder lists ({} hits).",
                    TAG_SET_POOL.size(), setHits, HOLDER_LIST_POOL.size(), listHits);
        }
    }

    public static int getTagSetHits() {
        return TAG_SET_HITS.get();
    }

    public static int getHolderListHits() {
        return HOLDER_LIST_HITS.get();
    }

    public static int getCanonicalTagSetsCount() {
        return TAG_SET_POOL.size();
    }

    public static int getCanonicalHolderListsCount() {
        return HOLDER_LIST_POOL.size();
    }
}
