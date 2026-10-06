package com.memfixer.modules;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.memfixer.config.MemFixerConfig;
import com.memfixer.modules.tag.TagDeduplicator;
import net.minecraft.core.Holder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Subsystem 12: TagDeduplicator Tests")
class TagDeduplicatorTest {

    @Test
    @DisplayName("Should return canonical Set for identical tag collections")
    @SuppressWarnings("unchecked")
    void testTagSetCanonicalization() {
        MemFixerConfig.DEDUPLICATE_TAGS = true;

        Operation<Set<String>> op = args -> Set.copyOf((Collection<String>) args[0]);

        Set<String> set1 = TagDeduplicator.canonicalizeTagSet(List.of("minecraft:mineable/pickaxe", "minecraft:needs_iron_tool"), op);
        Set<String> set2 = TagDeduplicator.canonicalizeTagSet(List.of("minecraft:mineable/pickaxe", "minecraft:needs_iron_tool"), op);

        assertSame(set1, set2);
        assertTrue(TagDeduplicator.getTagSetHits() > 0);
    }

    @Test
    @DisplayName("Should return empty set for empty input")
    void testEmptyTagSet() {
        MemFixerConfig.DEDUPLICATE_TAGS = true;
        Operation<Set<String>> op = args -> Set.of();

        Set<String> result = TagDeduplicator.canonicalizeTagSet(List.of(), op);
        assertTrue(result.isEmpty());
        assertSame(Set.of(), result);
    }

    @Test
    @DisplayName("Should canonicalize Holder lists order-independently")
    @SuppressWarnings("unchecked")
    void testHolderListCanonicalizationOrderIndependent() {
        MemFixerConfig.DEDUPLICATE_TAGS = true;

        Holder<String> holderA = Holder.direct("minecraft:apple");
        Holder<String> holderB = Holder.direct("minecraft:bread");

        Operation<List<Holder<String>>> op = args -> List.copyOf((Collection<Holder<String>>) args[0]);

        List<Holder<String>> list1 = TagDeduplicator.canonicalizeHolderList(List.of(holderA, holderB), op);
        List<Holder<String>> list2 = TagDeduplicator.canonicalizeHolderList(List.of(holderB, holderA), op);

        assertSame(list1, list2);
        assertTrue(TagDeduplicator.getHolderListHits() > 0);
    }

    @Test
    @DisplayName("Should return empty list for empty holder collection")
    void testEmptyHolderList() {
        MemFixerConfig.DEDUPLICATE_TAGS = true;
        Operation<List<Holder<String>>> op = args -> List.of();

        List<Holder<String>> result = TagDeduplicator.canonicalizeHolderList(List.of(), op);
        assertTrue(result.isEmpty());
        assertSame(List.of(), result);
    }
}
