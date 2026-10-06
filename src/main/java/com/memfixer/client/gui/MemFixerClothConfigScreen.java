package com.memfixer.client.gui;

import com.memfixer.config.MemFixerConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Cloth Config integration screen providing GUI controls for MemFixer modules.
 */
public final class MemFixerClothConfigScreen {

    private MemFixerClothConfigScreen() {}

    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("title.memfixer.config"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        // Category: Core Optimizations
        ConfigCategory coreCategory = builder.getOrCreateCategory(Component.translatable("category.memfixer.core"));

        coreCategory.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("option.memfixer.lazy_dfu"),
                        MemFixerConfig.LAZY_DFU)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("tooltip.memfixer.lazy_dfu"))
                .setSaveConsumer(val -> MemFixerConfig.LAZY_DFU = val)
                .build());

        coreCategory.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("option.memfixer.optimize_unifont"),
                        MemFixerConfig.OPTIMIZE_UNIFONT)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("tooltip.memfixer.optimize_unifont"))
                .setSaveConsumer(val -> MemFixerConfig.OPTIMIZE_UNIFONT = val)
                .build());

        coreCategory.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("option.memfixer.reclaim_post_launch"),
                        MemFixerConfig.RECLAIM_POST_LAUNCH)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("tooltip.memfixer.reclaim_post_launch"))
                .setSaveConsumer(val -> MemFixerConfig.RECLAIM_POST_LAUNCH = val)
                .build());

        coreCategory.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("option.memfixer.reap_textures"),
                        MemFixerConfig.REAP_TEXTURES)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("tooltip.memfixer.reap_textures"))
                .setSaveConsumer(val -> MemFixerConfig.REAP_TEXTURES = val)
                .build());

        coreCategory.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("option.memfixer.compact_blockstates"),
                        MemFixerConfig.COMPACT_BLOCKSTATES)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("tooltip.memfixer.compact_blockstates"))
                .setSaveConsumer(val -> MemFixerConfig.COMPACT_BLOCKSTATES = val)
                .build());

        coreCategory.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("option.memfixer.deduplicate_shapes"),
                        MemFixerConfig.DEDUPLICATE_SHAPES)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("tooltip.memfixer.deduplicate_shapes"))
                .setSaveConsumer(val -> MemFixerConfig.DEDUPLICATE_SHAPES = val)
                .build());

        coreCategory.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("option.memfixer.optimize_panorama"),
                        MemFixerConfig.OPTIMIZE_PANORAMA)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("tooltip.memfixer.optimize_panorama"))
                .setSaveConsumer(val -> MemFixerConfig.OPTIMIZE_PANORAMA = val)
                .build());

        coreCategory.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("option.memfixer.deduplicate_tags"),
                        MemFixerConfig.DEDUPLICATE_TAGS)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("tooltip.memfixer.deduplicate_tags"))
                .setSaveConsumer(val -> MemFixerConfig.DEDUPLICATE_TAGS = val)
                .build());

        coreCategory.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("option.memfixer.deduplicate_ingredients"),
                        MemFixerConfig.DEDUPLICATE_INGREDIENTS)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("tooltip.memfixer.deduplicate_ingredients"))
                .setSaveConsumer(val -> MemFixerConfig.DEDUPLICATE_INGREDIENTS = val)
                .build());

        coreCategory.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("option.memfixer.deduplicate_state_definitions"),
                        MemFixerConfig.DEDUPLICATE_STATE_DEFINITIONS)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("tooltip.memfixer.deduplicate_state_definitions"))
                .setSaveConsumer(val -> MemFixerConfig.DEDUPLICATE_STATE_DEFINITIONS = val)
                .build());

        coreCategory.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("option.memfixer.deduplicate_aabb_cache"),
                        MemFixerConfig.DEDUPLICATE_AABB_CACHE)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("tooltip.memfixer.deduplicate_aabb_cache"))
                .setSaveConsumer(val -> MemFixerConfig.DEDUPLICATE_AABB_CACHE = val)
                .build());

        // Category: Geometry & String Caches
        ConfigCategory modelCategory = builder.getOrCreateCategory(Component.translatable("category.memfixer.models"));

        modelCategory.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("option.memfixer.deduplicate_quads"),
                        MemFixerConfig.DEDUPLICATE_QUADS)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("tooltip.memfixer.deduplicate_quads"))
                .setSaveConsumer(val -> MemFixerConfig.DEDUPLICATE_QUADS = val)
                .build());

        modelCategory.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("option.memfixer.deduplicate_conditions"),
                        MemFixerConfig.DEDUPLICATE_CONDITIONS)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("tooltip.memfixer.deduplicate_conditions"))
                .setSaveConsumer(val -> MemFixerConfig.DEDUPLICATE_CONDITIONS = val)
                .build());

        modelCategory.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("option.memfixer.intern_resources"),
                        MemFixerConfig.INTERN_RESOURCES)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("tooltip.memfixer.intern_resources"))
                .setSaveConsumer(val -> MemFixerConfig.INTERN_RESOURCES = val)
                .build());

        // Category: World & Chunk Storage
        ConfigCategory worldCategory = builder.getOrCreateCategory(Component.translatable("category.memfixer.world"));

        worldCategory.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("option.memfixer.optimize_chunk_storage"),
                        MemFixerConfig.OPTIMIZE_CHUNK_STORAGE)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("tooltip.memfixer.optimize_chunk_storage"))
                .setSaveConsumer(val -> MemFixerConfig.OPTIMIZE_CHUNK_STORAGE = val)
                .build());

        // Category: Diagnostics & Logging
        ConfigCategory diagCategory = builder.getOrCreateCategory(Component.translatable("category.memfixer.diagnostics"));

        diagCategory.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("option.memfixer.enable_logging"),
                        MemFixerConfig.ENABLE_LOGGING)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("tooltip.memfixer.enable_logging"))
                .setSaveConsumer(val -> MemFixerConfig.ENABLE_LOGGING = val)
                .build());

        builder.setSavingRunnable(MemFixerConfig::save);

        return builder.build();
    }
}
