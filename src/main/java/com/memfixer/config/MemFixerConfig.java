package com.memfixer.config;

import net.neoforged.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Global configuration parameters and modular feature flags.
 * Each optimization module can be independently toggled via Cloth Config or memfixer.properties.
 */
public final class MemFixerConfig {
    private static final Logger LOGGER = LogManager.getLogger("MemFixer/Config");
    private static final String FILE_NAME = "memfixer.properties";

    /** Enables BakedQuad deduplication across block and item models. */
    public static volatile boolean DEDUPLICATE_QUADS = true;

    /** Enables String and ResourceLocation namespace/path interning. */
    public static volatile boolean INTERN_RESOURCES = true;

    /** Enables post-launch garbage compaction and transient structure scrubbing. */
    public static volatile boolean RECLAIM_POST_LAUNCH = true;

    /** Enables lazy / lightweight Unicode glyph loading to prevent 65k+ glyph heap allocation. */
    public static volatile boolean OPTIMIZE_UNIFONT = true;

    /** Enables lazy evaluation of DataFixerUpper rules. */
    public static volatile boolean LAZY_DFU = true;

    /** Enables releasing CPU-side static sprite pixel buffers after GPU upload. */
    public static volatile boolean REAP_TEXTURES = true;

    /** Enables compacting 26,000+ BlockState neighbour transition tables with FastNeighbourTable. */
    public static volatile boolean COMPACT_BLOCKSTATES = true;

    /** Enables optimizing TitleScreen panorama to single pass and real-time frame pacing. */
    public static volatile boolean OPTIMIZE_PANORAMA = true;

    /** Enables deduplicating VoxelShape face slices and sturdy masks in BlockState cache. */
    public static volatile boolean DEDUPLICATE_SHAPES = true;

    /** Enables deduplicating multipart model condition predicates and model pairs. */
    public static volatile boolean DEDUPLICATE_CONDITIONS = true;

    /** Enables informational console and game logging. */
    public static volatile boolean ENABLE_LOGGING = true;

    /** Enables chunk storage compaction: zero-bit storage singletons, dummy threading detector, and lock-free container access. */
    public static volatile boolean OPTIMIZE_CHUNK_STORAGE = true;

    /** Enables canonical deduplication of identical TagKey sets and Holder lists across registry entries. */
    public static volatile boolean DEDUPLICATE_TAGS = true;

    /** Enables canonical deduplication of identical recipe Ingredient instances and item arrays. */
    public static volatile boolean DEDUPLICATE_INGREDIENTS = true;

    /** Enables canonical deduplication of identical ImmutableSortedMap property maps in StateDefinitions. */
    public static volatile boolean DEDUPLICATE_STATE_DEFINITIONS = true;

    /** Enables canonical deduplication and caching of VoxelShape bounding box lists and outer bounds. */
    public static volatile boolean DEDUPLICATE_AABB_CACHE = true;

    static {
        load();
    }

    private MemFixerConfig() {}

    public static synchronized void load() {
        Path configPath;
        try {
            configPath = FMLPaths.CONFIGDIR.get().resolve(FILE_NAME);
        } catch (Throwable ignored) {
            configPath = Path.of("config", FILE_NAME);
        }

        if (!Files.exists(configPath)) {
            save();
            return;
        }

        Properties properties = new Properties();
        try (InputStream in = Files.newInputStream(configPath)) {
            properties.load(in);
            DEDUPLICATE_QUADS = Boolean.parseBoolean(properties.getProperty("deduplicate_quads", "true"));
            INTERN_RESOURCES = Boolean.parseBoolean(properties.getProperty("intern_resources", "true"));
            RECLAIM_POST_LAUNCH = Boolean.parseBoolean(properties.getProperty("reclaim_post_launch", "true"));
            OPTIMIZE_UNIFONT = Boolean.parseBoolean(properties.getProperty("optimize_unifont", "true"));
            LAZY_DFU = Boolean.parseBoolean(properties.getProperty("lazy_dfu", "true"));
            REAP_TEXTURES = Boolean.parseBoolean(properties.getProperty("reap_textures", "true"));
            COMPACT_BLOCKSTATES = Boolean.parseBoolean(properties.getProperty("compact_blockstates", "true"));
            OPTIMIZE_PANORAMA = Boolean.parseBoolean(properties.getProperty("optimize_panorama", "true"));
            DEDUPLICATE_SHAPES = Boolean.parseBoolean(properties.getProperty("deduplicate_shapes", "true"));
            DEDUPLICATE_CONDITIONS = Boolean.parseBoolean(properties.getProperty("deduplicate_conditions", "true"));
            ENABLE_LOGGING = Boolean.parseBoolean(properties.getProperty("enable_logging", "true"));
            OPTIMIZE_CHUNK_STORAGE = Boolean.parseBoolean(properties.getProperty("optimize_chunk_storage", "true"));
            DEDUPLICATE_TAGS = Boolean.parseBoolean(properties.getProperty("deduplicate_tags", "true"));
            DEDUPLICATE_INGREDIENTS = Boolean.parseBoolean(properties.getProperty("deduplicate_ingredients", "true"));
            DEDUPLICATE_STATE_DEFINITIONS = Boolean.parseBoolean(properties.getProperty("deduplicate_state_definitions", "true"));
            DEDUPLICATE_AABB_CACHE = Boolean.parseBoolean(properties.getProperty("deduplicate_aabb_cache", "true"));
            if (ENABLE_LOGGING) {
                LOGGER.info("[MemFixer/Config] Loaded configuration parameters from {}", FILE_NAME);
            }
        } catch (Exception e) {
            LOGGER.error("[MemFixer/Config] Failed to load {}: {}", FILE_NAME, e.getMessage());
        }
    }

    public static synchronized void save() {
        Path configPath;
        try {
            configPath = FMLPaths.CONFIGDIR.get().resolve(FILE_NAME);
        } catch (Throwable ignored) {
            configPath = Path.of("config", FILE_NAME);
        }

        Properties properties = new Properties();
        properties.setProperty("deduplicate_quads", String.valueOf(DEDUPLICATE_QUADS));
        properties.setProperty("intern_resources", String.valueOf(INTERN_RESOURCES));
        properties.setProperty("reclaim_post_launch", String.valueOf(RECLAIM_POST_LAUNCH));
        properties.setProperty("optimize_unifont", String.valueOf(OPTIMIZE_UNIFONT));
        properties.setProperty("lazy_dfu", String.valueOf(LAZY_DFU));
        properties.setProperty("reap_textures", String.valueOf(REAP_TEXTURES));
        properties.setProperty("compact_blockstates", String.valueOf(COMPACT_BLOCKSTATES));
        properties.setProperty("optimize_panorama", String.valueOf(OPTIMIZE_PANORAMA));
        properties.setProperty("deduplicate_shapes", String.valueOf(DEDUPLICATE_SHAPES));
        properties.setProperty("deduplicate_conditions", String.valueOf(DEDUPLICATE_CONDITIONS));
        properties.setProperty("enable_logging", String.valueOf(ENABLE_LOGGING));
        properties.setProperty("optimize_chunk_storage", String.valueOf(OPTIMIZE_CHUNK_STORAGE));
        properties.setProperty("deduplicate_tags", String.valueOf(DEDUPLICATE_TAGS));
        properties.setProperty("deduplicate_ingredients", String.valueOf(DEDUPLICATE_INGREDIENTS));
        properties.setProperty("deduplicate_state_definitions", String.valueOf(DEDUPLICATE_STATE_DEFINITIONS));
        properties.setProperty("deduplicate_aabb_cache", String.valueOf(DEDUPLICATE_AABB_CACHE));

        try {
            if (configPath.getParent() != null) {
                Files.createDirectories(configPath.getParent());
            }
            try (OutputStream out = Files.newOutputStream(configPath)) {
                properties.store(out, "MemFixer Configuration - Ultra-low footprint memory engine");
            }
            if (ENABLE_LOGGING) {
                LOGGER.info("[MemFixer/Config] Saved configuration parameters to {}", FILE_NAME);
            }
        } catch (Exception e) {
            LOGGER.error("[MemFixer/Config] Failed to save {}: {}", FILE_NAME, e.getMessage());
        }
    }
}
