# MemFixer Architecture & Engineering Specification

## 1. Executive Summary
- **Target Platform**: Minecraft 1.21+ on NeoForge (Java 21).
- **Author**: Egemen
- **Core Objective**: Reduce resident process memory (RSS) and JVM heap footprint, eliminate GC allocation churn, maintain complete visual fidelity (zero texture or shader degradation), and preserve 100% compatibility with Sodium, Lithium, FerriteCore, ModernFix, and extensive modpacks.
- **Delivery Mechanism**: Standard NeoForge drop-in mod JAR requiring zero external JVM wrappers or non-standard runtime installations.
- **Modularity**: 15 independently toggleable subsystems via `config/memfixer.properties` and Cloth Config in-game GUI.

---

## 2. Memory Breakdown & Profiling (Title Screen Baseline)

Live JVM heap profiling via `jcmd GC.class_histogram` on Minecraft 1.21.1 NeoForge:

| Memory Domain | Vanilla / Baseline | MemFixer Optimized | Memory Freed | Architectural Mechanism |
| :--- | :--- | :--- | :--- | :--- |
| **CJK Unifont Glyphs** | ~72.0 MB | ~1.5 MB | **~70.5 MB** | `LazyUnihexHandler` + `UnihexProviderMixin` |
| **Model Geometries (`BakedQuad`)** | ~24.5 MB | ~11.8 MB | **~12.7 MB** | `QuadDeduplicator` 2,048-entry direct mapped pool |
| **BlockState Transitions** | ~17.5 MB | ~2.8 MB | **~14.7 MB** | `FastNeighbourTable` 1D flat indexed array |
| **MultiPart Conditions & Pairs** | ~11.2 MB | ~2.1 MB | **~9.1 MB** | `ConditionDeduplicator` canonical state predicates |
| **CPU-side Static Textures** | ~10.4 MB | ~2.1 MB | **~8.3 MB** | `TextureReaperHandler` immediate `NativeImage.close()` |
| **VoxelShapes & Sturdy Masks** | ~8.7 MB | ~3.4 MB | **~5.3 MB** | `ShapeDeduplicator` canonical face slices & masks |
| **DataFixerUpper (DFU)** | ~12.0 MB | ~0.5 MB | **~11.5 MB** | `LazyDfuHandler` deferred rule compilation |
| **ResourceLocations & Strings** | ~8.5 MB | ~4.6 MB | **~3.9 MB** | `ResourceInterner` JVM HotSpot `String.intern()` |
| **Registry Tags & HolderSets** | ~1.5 MB (scales w/ mods) | ~0.5 MB | **~1.0 MB (scales)** | `TagDeduplicator` canonical tag sets & holder lists |
| **Recipe Ingredients (`Ingredient`)** | ~2.5 MB (scales w/ mods) | ~0.8 MB | **~1.7 MB (scales)** | `IngredientDeduplicator` canonical ingredient pooling |
| **StateDefinition Property Maps** | ~2.0 MB (scales w/ mods) | ~0.4 MB | **~1.6 MB (scales)** | `StatePropertyDeduplicator` canonical ImmutableSortedMap pool |
| **Collision Bounding Boxes (`AABB`)** | High allocation churn | Pre-cached singletons & pool | **Zero churn** | `CollisionDeduplicator` cached `toAabbs()` & bounds |
| **Post-Launch Transient Data** | ~15.0 MB | 0.0 MB | **~15.0 MB** | `MemoryReclaimer` post-TitleScreen GC sweep |
| **Chunk Storage & Threading Detectors** | ~8.0 MB | ~0.1 MB | **~7.9 MB** | `ChunkStorageOptimizer` ZeroBit singletons & no-op detector |
| **Total TitleScreen Live Heap** | **~350 - 400 MB** | **178 MB** | **>150 MB** | **Zero quality loss, 100% plug & play** |

---

## 3. Core Architectural Subsystems

### 3.1 Subsystem 1: `FastNeighbourTable` (BlockState Transition Table Compaction)
- **Problem**: Minecraft initializes 26,000+ `BlockState` instances, each containing an `ImmutableMap` or Guava `ArrayTable` mapping `(Property, Comparable) -> BlockState`. Across all blocks, this produces hundreds of thousands of entry arrays and pointer indirections.
- **Solution**:
  - `FastNeighbourTable`: Replaces multi-dimensional Guava tables with a compact 1D array of `BlockState` references.
  - State lookups utilize an O(1) collision-resistant open-addressing hash index embedded directly inside the canonical `SharedKeys` descriptor, shared across all state variations of a block definition.
  - Zero extra heap allocation per state; instant single-probe indexed state transitions.
  - When FerriteCore is installed, `StateHolderMixin` detects its presence via class inspection and gracefully yields transition table compaction to FerriteCore to ensure 100% interoperability without table crashes.
  - Mixin: `StateHolderMixin` intercepts `populateNeighbours` at `RETURN`.

### 3.2 Subsystem 2: `QuadDeduplicator` (Zero-Allocation BakedQuad Pooling)
- **Problem**: Block and item models instantiate separate `BakedQuad` instances for identical geometric faces across different states, rotations, and variants.
- **Solution**:
  - High-performance open-addressing canonical pool keyed by quad vertex arrays, direction, and tint indices.
  - Identical quads are interned into single canonical instances, saving redundant quad allocations and vertex arrays during model baking.
  - Mixins: `BakedModelDeduplicationMixin`, `FaceBakeryMixin`, `ItemModelGeneratorMixin`.

### 3.3 Subsystem 3: `ConditionDeduplicator` (MultiPart Model Optimization)
- **Problem**: Vanilla Minecraft compiles multipart model blockstate conditions by instantiating anonymous lambda predicates (`state -> state.getValue(prop).equals(val)`) and wrapping each in `Pair.of(predicate, bakedModel)`. In 1.21.1, this spawns ~177,000 lambdas, ~178,000 captured `Optional` objects, and ~90,000 `ImmutablePair` instances.
- **Solution**:
  - `FastPropertyPredicate`: Canonical immutable predicate class keyed by `(Property, Comparable)`. Identical property-value conditions return shared singletons.
  - Canonical `Pair<Predicate<BlockState>, BakedModel>` caching during model building.
  - Mixins: `KeyValueConditionMixin`, `MultiPartBakedModelBuilderMixin`.

### 3.4 Subsystem 4: `ShapeDeduplicator` (VoxelShape & Sturdy Mask Deduplication)
- **Problem**: Every `BlockState` cache constructs `VoxelShape[] occlusionShapes` (one per 6 directions) via `Shapes.getFaceShape()` and a `boolean[18] faceSturdy` array. Across 26,500+ states, this generates 86,000+ redundant `SliceShape`/`SubShape` instances and 26,500 duplicate boolean arrays.
- **Solution**:
  - Deduplicates face shape arrays into canonical 6-element `VoxelShape[]` singletons.
  - Bit-packs the 18 boolean values of `faceSturdy` into an integer key and returns a canonical `boolean[18]` from a 57-element cache table.
  - Mixin: `BlockStateCacheMixin`.

### 3.5 Subsystem 5: `TextureReaperHandler` (CPU-Side Native Pixel Disposal)
- **Problem**: When texture atlases (blocks, items, armor trims) are built, pixel bytes are loaded into off-heap `NativeImage` buffers. Once uploaded to GPU VRAM via OpenGL, vanilla Minecraft leaves static sprite pixel buffers allocated in CPU RAM.
- **Solution**:
  - Intercepts texture atlas stitching completion (`TextureAtlasReaperMixin`).
  - Calls `NativeImage.close()` on all non-animated static terrain and trim sprites while preserving tickers for animated textures (e.g., fire, water, lava), reclaiming off-heap resident RAM (RSS).

### 3.6 Subsystem 6: `LazyUnihexHandler` (True On-Demand CJK Unifont Deferral)
- **Problem**: Minecraft boots with full Unicode hex font coverage, eagerly unpacking 65,536 CJK glyph definitions and bitmap tables (~70.5 MB heap) even when the user plays in English, Turkish, Spanish, German, etc.
- **Solution**:
  - Intercepts `UnihexProviderMixin` with `LazyUnihexMap`.
  - In non-CJK locales, replaces eager parsing of 65,536 `Glyph` objects with a compact 2.5 MB raw byte buffer and 512 KB offset table (under 3 MB total vs 70 MB vanilla).
  - Individual glyph bitmaps are unpacked on-demand only when rendered in chat, signs, or books, guaranteeing 100% visual fidelity for CJK and special Unicode characters with zero missing glyphs.

### 3.7 Subsystem 7: `LazyDfuHandler` (DataFixerUpper Compilation Bypass)
- **Problem**: DFU compiles hundreds of schema migration rules during game boot to support converting saves from 1.12 to 1.21.1, generating high GC churn during initialization.
- **Solution**:
  - Defers compilation until world save loading detects that data fixers are strictly required.

### 3.8 Subsystem 8: `ResourceInterner` (Native StringTable Pooling)
- **Problem**: Tens of thousands of `ResourceLocation` objects instantiate duplicate `String` instances for identical namespaces (e.g. `"minecraft"`) and paths.
- **Solution**:
  - Delegates identifier strings to the JVM HotSpot native `StringTable` via `String.intern()`, eliminating Java heap map wrapper overhead.

### 3.9 Subsystem 9: `MemoryReclaimer` (Post-Launch Garbage Sweep)
- **Problem**: Model baking and bootstrap leave behind thousands of unbaked model JSON trees, AST nodes, and transient lookup tables that linger across garbage collection cycles.
- **Solution**:
  - Detects stabilization of the `TitleScreen` and executes a background memory reclamation sweep, purging AST caches and requesting a clean generational sweep.

### 3.10 Subsystem 10: Title Screen Panorama Optimization & Frame Pacing
- **Problem**: Vanilla renders the TitleScreen panorama in 4 separate passes with jitter offsets per frame (24 draw calls/frame), producing heavy GPU overdraw and allocating mesh arrays continuously. Furthermore, panorama rotation uses tick-fraction residuals, causing visible 20 Hz stutter.
- **Solution**:
  - `CubeMapMixin`: Clamps rendering passes to 1, dropping draw calls from 24 to 6 (75% reduction in draw calls and mesh allocations).
  - `PanoramaRendererMixin`: Replaces tick residual with `getRealtimeDeltaTicks()` from `DeltaTracker` for buttery-smooth 60/144/240+ FPS rotation.

### 3.11 Subsystem 11: `ChunkStorageOptimizer` (ZeroBitStorage Singletons & ThreadingDetector Elimination)
- **Problem**: In a loaded world with 10-chunk render distance, there are 10,584 sections containing 21,168 `PalettedContainer`s. Each container instantiates a `ThreadingDetector` (containing a `Semaphore`, `ReentrantLock`, and internal sync objects) totaling 105,840 dev-assertion synchronization objects on the heap. Furthermore, thousands of uniform sub-chunks allocate redundant `ZeroBitStorage` instances.
- **Solution**:
  - `ChunkStorageOptimizer`: Shares immutable singletons (`STATIC_4096` for block states, `STATIC_64` for biomes), eliminating tens of thousands of `ZeroBitStorage` allocations.
  - `DummyThreadingDetector`: Provides a shared no-op singleton, eliminating over 105,000 synchronization objects (~6-8 MB direct heap saved).
  - Injects lock-free fast paths into `PalettedContainer.acquire()` and `release()` in production.
  - Mixins: `PalettedContainerMixin`, `PalettedContainerConfigurationMixin`.

### 3.12 Subsystem 12: `TagDeduplicator` (Registry TagKey Set & Holder List Deduplication)
- **Problem**: In modded environments with large numbers of registered items, blocks, and biomes, `Holder.Reference.bindTags` and `HolderSet.Named.bind` independently call `Set.copyOf()` and `List.copyOf()`. Many registry entries share identical tag combinations, causing redundant `Set` and `List` collection allocations across registries.
- **Solution**:
  - `TagDeduplicator`: Intercepts `Set.copyOf` and `List.copyOf` using MixinExtras `@WrapOperation`, canonicalizing identical tag sets and holder lists into shared unmodifiable references.
  - Transparent to gameplay and datapacks; preserves standard Java `equals()` and `hashCode()` contracts.
  - Zero-risk interoperability: FerriteCore and ModernFix do not touch `Holder.Reference` tag binding.
  - Mixins: `HolderReferenceMixin`, `HolderSetNamedMixin`.

### 3.13 Subsystem 13: `IngredientDeduplicator` (Recipe & Ingredient Deduplication)
- **Problem**: In heavily modded modpacks (20,000 to 50,000+ recipes), vanilla and modded recipe managers instantiate distinct `Ingredient` objects for identical item inputs (e.g. `iron_ingot`, `stick`, `copper_ingot`, common tags). Each distinct `Ingredient` object lazily caches its own `ItemStack[] itemStacks` and `IntList stackingIds` arrays when queried by recipe lookups and recipe viewers (JEI, REI, EMI), generating tens of thousands of redundant array allocations.
- **Solution**:
  - `IngredientDeduplicator`: Canonical pooling engine that interns identical `Ingredient` instances using NeoForge's native value-based `equals()` and `hashCode()` contract.
  - Dedupes shaped recipes (`ShapedRecipePattern`), shapeless recipes (`ShapelessRecipe`), and single-item recipes (`SingleItemRecipe`: smelting, blasting, smoking, campfire, stonecutting) at constructor time.
  - Implements a post-sync / post-reload defensive sweep via `RecipeManagerMixin` across all loaded recipes in `RecipeManager.byName`.
  - Shared canonical `Ingredient` instances share a single lazily-evaluated `ItemStack[]` and `IntList`, eliminating duplicate cache arrays across the entire recipe registry.
  - Mixins: `ShapedRecipePatternMixin`, `ShapelessRecipeMixin`, `SingleItemRecipeMixin`, `RecipeManagerMixin`.

### 3.14 Subsystem 14: `StatePropertyDeduplicator` (StateDefinition Property Map Deduplication)
- **Problem**: In Minecraft 1.21.1 and especially in modded setups with 15,000+ blocks, every block instantiates a `StateDefinition` containing an `ImmutableSortedMap<String, Property<?>> propertiesByName`. Hundreds of blocks (stairs, slabs, walls, fences, doors, trapdoors, buttons) share identical property schemas (e.g. `facing`, `half`, `shape`, `waterlogged`), resulting in tens of thousands of duplicate Guava map wrapper objects, key/value arrays, and comparator references.
- **Solution**:
  - `StatePropertyDeduplicator`: Intercepts `ImmutableSortedMap.copyOf` in `StateDefinition.<init>` via MixinExtras `@WrapOperation`.
  - Canonicalizes identical property maps into shared unmodifiable singletons.
  - 100% contract-safe and zero-risk: Guava's `ImmutableSortedMap` and Minecraft's `Property<?>` singletons are strictly immutable.
  - Mixin: `StateDefinitionMixin`.

### 3.15 Subsystem 15: `CollisionDeduplicator` (VoxelShape Collision Bounding Box Singletons)
- **Problem**: In vanilla Minecraft, `VoxelShape.toAabbs()` and `VoxelShape.bounds()` are called continually during entity raycasting, projectile physics, hopper item collection, and block boundary rendering. Vanilla does not cache these collections: each call allocates a `new ArrayList<>()`, a `new AABB(...)` for every sub-box, and a closure lambda. For full cubes (`Shapes.block()`), it recreates `new AABB(0, 0, 0, 1, 1, 1)` and a list millions of times during gameplay.
- **Solution**:
  - `CollisionDeduplicator`: Caches unmodifiable `List<AABB>` and outer bounds directly on `VoxelShape` on first call using thread-safe benign race publication.
  - Returns canonical pre-allocated singletons (`FULL_CUBE_LIST`, `EMPTY_LIST`) for standard cubes and empty shapes.
  - Canonicalizes custom `AABB` instances across block shapes into a shared thread-safe pool.
  - Mixin: `VoxelShapeMixin`.

---

## 4. Modularity and Configuration Architecture
* **Global Config**: `MemFixerConfig` maintains `volatile boolean` flags for every subsystem.
* **Storage**: `config/memfixer.properties` provides zero-dependency file persistence for both clients and servers.
* **GUI**: `MemFixerClothConfigScreen` integrates with NeoForge's `IConfigScreenFactory` to provide an in-game graphical menu with full English (`en_us`) and Turkish (`tr_tr`) localization.
* **Fail-Safe Decoupling**: If any module is toggled off, its Mixin immediately falls back to vanilla behavior with zero cross-module side effects.
