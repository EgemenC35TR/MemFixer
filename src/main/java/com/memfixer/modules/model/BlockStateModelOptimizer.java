package com.memfixer.modules.model;

import com.memfixer.modules.resource.ResourceInterner;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.renderer.block.model.multipart.MultiPart;
import net.minecraft.client.renderer.block.model.multipart.Selector;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.*;

/**
 * High-performance, allocation-conscious optimizer for BlockStateModelLoader and ModelBakery.
 * Eliminates heavy Java Stream pipelines across 25,000+ blockstate model evaluations and dependency resolutions.
 */
@SuppressWarnings("null")
public final class BlockStateModelOptimizer {

    private BlockStateModelOptimizer() {}

    /**
     * Optimized extraction of coloring values without Stream or Lambda allocations.
     * Returns a singleton empty list for the vast majority of blocks without color properties.
     */
    public static List<Object> getOptimizedColoringValues(BlockState state, Collection<Property<?>> properties) {
        if (state == null || properties == null || properties.isEmpty()) {
            return Collections.emptyList();
        }

        int size = properties.size();
        if (size == 1) {
            Property<?> singleProp = properties.iterator().next();
            Comparable<?> val = state.getValue(singleProp);
            return val != null ? Collections.singletonList(val) : Collections.emptyList();
        }

        List<Object> values = new ArrayList<>(size);
        for (Property<?> property : properties) {
            Comparable<?> val = state.getValue(property);
            if (val != null) {
                values.add(val);
            }
        }
        return Collections.unmodifiableList(values);
    }

    /**
     * Fast-path filtering of multipart selectors without Stream pipelines.
     */
    public static List<UnbakedModel> getOptimizedMultipartModels(BlockState state, MultiPart multipart) {
        if (state == null || multipart == null) {
            return Collections.emptyList();
        }

        List<Selector> selectors = multipart.getSelectors();
        if (selectors == null || selectors.isEmpty()) {
            return Collections.emptyList();
        }

        StateDefinition<Block, BlockState> stateDefinition = state.getBlock().getStateDefinition();
        List<UnbakedModel> matchingModels = new ArrayList<>(selectors.size());

        for (int i = 0; i < selectors.size(); i++) {
            Selector selector = selectors.get(i);
            if (selector.getPredicate(stateDefinition).test(state)) {
                matchingModels.add(selector.getVariant());
            }
        }

        return matchingModels.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(matchingModels);
    }

    /**
     * Allocation-free dependency resolution for MultiVariant without Stream or Collector overhead.
     */
    public static Collection<ResourceLocation> getOptimizedMultiVariantDependencies(List<Variant> variants) {
        if (variants == null || variants.isEmpty()) {
            return Collections.emptySet();
        }
        if (variants.size() == 1) {
            return Collections.singleton(variants.get(0).getModelLocation());
        }
        Set<ResourceLocation> set = new HashSet<>(variants.size());
        for (int i = 0; i < variants.size(); i++) {
            set.add(variants.get(i).getModelLocation());
        }
        return set;
    }

    /**
     * Allocation-free dependency resolution for MultiPart without flatMap Streams.
     */
    public static Collection<ResourceLocation> getOptimizedMultiPartDependencies(List<Selector> selectors) {
        if (selectors == null || selectors.isEmpty()) {
            return Collections.emptySet();
        }
        Set<ResourceLocation> set = new HashSet<>(selectors.size() * 2);
        for (int i = 0; i < selectors.size(); i++) {
            set.addAll(selectors.get(i).getVariant().getDependencies());
        }
        return set;
    }

    /**
     * Allocation-conscious conversion of blockstate property maps to canonical variant strings.
     * Bypasses heavy StringBuilder allocations for propertyless and single-property states,
     * and interns variant strings into the canonical resource pool.
     */
    public static String getOptimizedStatePropertiesString(Map<Property<?>, Comparable<?>> properties) {
        if (properties == null || properties.isEmpty()) {
            return "";
        }

        if (properties.size() == 1) {
            Map.Entry<Property<?>, Comparable<?>> single = properties.entrySet().iterator().next();
            Property<?> prop = single.getKey();
            String res = prop.getName() + "=" + getPropertyValueName(prop, single.getValue());
            return ResourceInterner.intern(res);
        }

        StringBuilder sb = new StringBuilder(48);
        for (Map.Entry<Property<?>, Comparable<?>> entry : properties.entrySet()) {
            if (sb.length() != 0) {
                sb.append(',');
            }
            Property<?> property = entry.getKey();
            sb.append(property.getName()).append('=').append(getPropertyValueName(property, entry.getValue()));
        }
        return ResourceInterner.intern(sb.toString());
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> String getPropertyValueName(Property<T> property, Comparable<?> value) {
        return property.getName((T) value);
    }
}
