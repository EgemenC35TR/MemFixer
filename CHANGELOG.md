# Changelog

All notable changes to the MemFixer optimization engine will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [0.1.0] - 2026-10-05

### Added
- **Subsystem 1: Model Quad Deduplicator (`QuadDeduplicator`)**:
  - Dynamically-resizing linear-probing open-addressing table interning identical `BakedQuad` instances during model baking.
  - IEEE 754 negative-zero (`-0.0f` vs `0.0f`) bitwise normalization in vertex coordinates to prevent false cache misses.
  - Eliminates over 122,000 duplicate quads on standard mod setups (69.8% deduplication rate), flushing to 0 bytes post-bake.
- **Subsystem 2: Fast Neighbour Transition Table (`FastNeighbourTable`)**:
  - Replaces heavy Guava `ArrayTable` and `HashBasedTable` instances across 26,000+ BlockState neighbour graphs with compact, shared-key index arrays.
  - Implements deterministic canonical cell sorting (`Property.getName()` and `Comparable.toString()`), collapsing thousands of individual key arrays into shared topology singletons (88% - 98% array sharing).
  - Includes safe auto-yield integration with FerriteCore's `FastMap` when present.
- **Subsystem 3: BitSet BlockState Cache Optimizer (`BlockStateCacheOptimizer`)**:
  - Compresses individual boolean collision/render flags into bitwise bitmasks, reducing per-state cache overhead by 87.5%.
- **Subsystem 4: Chunk Paletted Container Compactor (`ChunkStorageOptimizer`)**:
  - Replaces empty and uniform 16x16x16 chunk sub-sections with shared zero-allocation singletons (`STATIC_4096` and `STATIC_64`).
  - Eliminates per-container `ThreadingDetector` allocation churn (bypassing 145,000+ detectors and over 1,120,000 lock/semaphore synchronization objects).
  - Automatically purges unused server structure maps from client-side chunk instances.
- **Subsystem 5: Lazy Unihex Glyph Engine (`LazyUnihexMap` & `LazyUnihexHandler`)**:
  - Replaces eager loading of 65,000+ unused CJK Unicode glyphs in non-Asian locales with an on-demand indexed off-heap direct buffer.
  - Reclaims 6 to 8 MB of direct JVM heap down to ~1 MB.
- **Subsystem 6: Model AST and Unbaked Cache Cleaner (`BlockStateModelOptimizer`)**:
  - Sweeps temporary blockstate AST definition trees and unbaked model maps upon model bake finalization.
- **Subsystem 7: Multipart Condition Deduplicator (`ConditionDeduplicator`)**:
  - Canonicalizes multipart model condition predicates and model pairs, deduplicating over 110,000 model pairs (96.4% savings) and eliminating 170,000+ redundant predicate lambdas.
- **Subsystem 8: Registry Tag & Holder Deduplicator (`TagDeduplicator`)**:
  - Canonicalizes duplicate `Set<TagKey<?>>` and `List<Holder<?>>` collections across registry entries.
  - Implements order-independent deterministic Holder sorting by registry name, eliminating datapack ordering discrepancies across mods.
- **Subsystem 9: Native Resource Location Interner (`ResourceInterner`)**:
  - Connects `ResourceLocation` namespaces and paths directly to the JVM HotSpot native `String.intern()` table.
- **Subsystem 10: Lazy DataFixerUpper Initializer (`DFUOptimizationModule`)**:
  - Defers eager DataFixerUpper schema rule compilation on startup until a world save requiring upgrade is loaded, accelerating game launch time.
- **Subsystem 11: Texture Pixel Buffer Reaper (`TextureReaperHandler`)**:
  - Releases CPU-side `NativeImage` pixel buffers to the operating system immediately following successful GPU VRAM upload, while preserving animated sprite tickers.
- **Subsystem 12: VoxelShape Slice and Sturdy Mask Deduplicator (`ShapeDeduplicator`)**:
  - Caches and pools identical VoxelShape face slices and sturdy face boolean arrays in `BlockStateBase.Cache`.
- **Subsystem 13: Recipe and Ingredient Deduplicator (`IngredientDeduplicator`)**:
  - Pools identical `Ingredient` instances across shaped, shapeless, and single-item recipes.
  - Reclaims over 17,200 duplicate ingredient instances across recipe registries (96.8% hit rate).
- **Subsystem 14: StateDefinition Property Map Deduplicator (`StatePropertyDeduplicator`)**:
  - Canonicalizes Guava `ImmutableSortedMap<String, Property<?>>` across all `StateDefinition` instances.
  - Reclaims 584 redundant property maps out of 693 definitions (84.3% hit rate in Vanilla; 95%+ in modpacks).
- **Subsystem 15: VoxelShape Collision Bounding Box Singletons (`CollisionDeduplicator`)**:
  - Caches unmodifiable `List<AABB>` and outer `bounds()` directly on `VoxelShape` instances on first call via thread-safe benign race publication.
  - Provides zero-allocation singletons for full cubes and empty shapes.
  - Achieves 99.6% cache hit rate during in-game movement, raycasting, and collision checks.
- **In-Game Passive Diagnostic Command (`/memfixer`)**:
  - `/memfixer status`: Displays live JVM heap metrics (Used/Max/Committed) and real-time deduplication telemetry across all active subsystems without allocating garbage or touching world state.
  - `/memfixer info`: Displays mod metadata, active subsystem counts, platform configuration, and GUI config access points.
  - `/memfixer help`: Displays command syntax and options.
- **Automated JUnit 5 Test Suite**:
  - 40 automated unit tests across 11 test classes in `src/test/java/com/memfixer/modules/` covering null safety, thread-safety, order-independence, and configuration fallback across all 15 modules with 100% pass rate.
- **In-Game GUI Configuration**:
  - Integrated Cloth Config screen accessible via **Mods -> MemFixer -> Config**, localized in English (`en_us`) and Turkish (`tr_tr`).
  - Standalone `config/memfixer.properties` configuration file for dedicated servers and headless environments.
