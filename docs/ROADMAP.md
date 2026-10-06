# MemFixer Development Roadmap & Execution Plan

High-performance memory reclamation engine for Minecraft 1.21+ NeoForge (Java 21).  
**Author**: Egemen  
**Core Philosophy**: Deterministic, zero-compromise memory compaction and GC churn reduction with strict non-interference:
- **Zero Risk**: Never modify mutable collections or lifecycle-bound registry structures.
- **Zero Gameplay Interference**: No entity culling, no audio tampering, no visual degradation.
- **Flawless Mod Interoperability**: Seamless coexistence with Sodium, Lithium, FerriteCore, ModernFix, ImmediatelyFast, and large modpacks.

---

## Phase Status Overview

| Phase | Milestone Name | Scope & Focus | Status |
| :--- | :--- | :--- | :--- |
| **Phase 1** | **Core Memory Engine & Boot Optimization** | 15 core subsystems: Model streaming, DFU/Unihex deferral, O(1) FastNeighbourTable, Geometry & Shape deduplication, Native string interning, Chunk storage singletons, Tag/Holder deduplication, Recipe deduplication, StateDefinition property deduplication, Collision bounding box caching, Cloth Config GUI | **100% Completed** |
| **Phase 2** | **Diagnostics, Verification & Modpack Hardening** | Read-only `/memfixer status` command, large modpack edge-case verification, zero-allocation diagnostic telemetry | **Current Focus** |
| **Phase 3** | **Long-Term Maintenance & Platform Parity** | NeoForge 1.21.x lifecycle updates, continuous tracking with modern optimization stack releases | **Planned** |

---

## Phase 1: Core Memory Engine & Boot Optimization (Completed)

### Completed Deliverables

- [x] **Project Toolchain & Base Engine**:
  - Eclipse Adoptium JDK 21 integration.
  - NeoForge `21.1.219` with ModDevGradle `2.0.75`.
- [x] **Modular Configuration & In-Game GUI**:
  - `MemFixerConfig`: Persistent zero-overhead properties config (`config/memfixer.properties`).
  - `MemFixerClothConfigScreen`: In-game options screen under NeoForge `IConfigScreenFactory`.
  - Full localization support: English (`en_us.json`) and Turkish (`tr_tr.json`) with zero hardcoded UI strings.
  - 15 independent feature toggles providing full modularity.
  - Fact-based runtime logging with exact measured object counts, cache hits, and runtime-measured heap deltas.
- [x] **DataFixerUpper (DFU) Bypass**:
  - `LazyDfuHandler` + `DataFixersMixin`: Bypasses 229 eager rule compilations during boot, eliminating startup lag and transient heap churn.
- [x] **Unifont Glyph Deferral (True On-Demand)**:
  - `LazyUnihexHandler` + `LazyUnihexMap` + `UnihexProviderMixin`: Replaces eager allocation of 65,000+ CJK unifont glyphs with an on-demand index table (under 3 MB off-heap), ensuring full visual fidelity.
- [x] **Static Texture Buffer Reaper**:
  - `TextureAtlasReaperMixin` + `TextureReaperHandler`: Frees block/item and armor trim CPU-side `NativeImage` buffers immediately upon GPU VRAM upload.
- [x] **Streaming Model Baking Pipeline**:
  - `ModelBakeryMixin` `@WrapOperation`: Converts eager model collection into an in-place streaming iterator (`iterator.remove()`), eliminating the dual-retention memory spike non-destructively.
  - `ModelCleaner.cleanPreBake`: Purges unbaked JSON AST trees before model baking starts.
- [x] **Zero-Allocation Geometry Deduplication**:
  - `QuadDeduplicator`: Open-addressing canonical pool deduplicating redundant baked quads across block and item models.
- [x] **BlockState Neighbour Table Compaction**:
  - `FastNeighbourTable` + `StateHolderMixin`: Compacts 26,000+ BlockState transition lookup tables into O(1) open-addressing shared-key arrays, gracefully yielding when FerriteCore FastMap is detected.
- [x] **VoxelShape & Sturdy Face Deduplication**:
  - `ShapeDeduplicator` + `BlockStateCacheMixin`: Deduplicates 86,832 `VoxelShape` face slices and merges 26,505 `boolean[18] faceSturdy` arrays into 57 shared canonical masks.
- [x] **MultiPart Condition & Pair Deduplication**:
  - `ConditionDeduplicator` + `KeyValueConditionMixin` + `MultiPartBakedModelBuilderMixin`: Deduplicates 355,602 condition predicate requests down to 70 canonical singletons (eliminating 177,000 lambdas) and deduplicates 90,113 `Pair` instances.
- [x] **Title Screen Panorama Optimization & Frame Pacing**:
  - `CubeMapMixin`: Reduces 4-pass overdraw to 1-pass (24 draw calls/frame down to 6, a 75% GPU/heap churn reduction).
  - `PanoramaRendererMixin`: Replaces discrete tick residual with real-time frame delta from `DeltaTracker`, eliminating micro-stutters during rotation.
- [x] **Native String Interning**:
  - `ResourceInterner`: Migrated from heap map to JVM HotSpot native `String.intern()`, delegating ResourceLocation paths to the JVM string table.
- [x] **Post-Launch Memory Reaper**:
  - `MemoryReclaimer` + `TitleScreenMixin`: Executes lifecycle-aware compaction sweep once TitleScreen stabilizes, releasing unbaked AST trees, unbaked models, and temporary bootstrap caches.
- [x] **Chunk Section Storage & ThreadingDetector Elimination**:
  - `ChunkStorageOptimizer` + `PalettedContainerMixin` + `PalettedContainerConfigurationMixin`:
    - Replaces empty and uniform chunk sub-sections (16x16x16 air, uniform underground rock, and 4x4x4 uniform biomes) with immutable zero-allocation singletons (`STATIC_4096` and `STATIC_64`).
    - Eliminates per-container `ThreadingDetector` allocation churn (5 synchronization objects per container: `ThreadingDetector`, `Semaphore`, `Semaphore$NonfairSync`, `ReentrantLock`, `ReentrantLock$NonfairSync`), removing over 105,000 objects in a standard 10-chunk render distance (~6-8 MB direct heap saved).
    - Injects lock-free fast-path into `acquire()` and `release()` in production, removing thread contention during block query/set operations.
- [x] **Registry TagKey & HolderSet Deduplication**:
  - `TagDeduplicator` + `HolderReferenceMixin` + `HolderSetNamedMixin`:
    - Intercepts `Set.copyOf` in `Holder.Reference.bindTags` and `List.copyOf` in `HolderSet.Named.bind` via MixinExtras `@WrapOperation`.
    - Canonicalizes identical tag sets and holder lists across registry entries into shared unmodifiable references, saving memory that scales directly with modpack registry sizes.
- [x] **Recipe & Ingredient Deduplication**:
  - `IngredientDeduplicator` + `ShapedRecipePatternMixin` + `ShapelessRecipeMixin` + `SingleItemRecipeMixin` + `RecipeManagerMixin`:
    - Leverages NeoForge 1.21.1's native value-based `Ingredient.equals(Object)` and `hashCode()` contract to pool identical `Ingredient` instances.
    - Dedupes shaped, shapeless, and furnace/stonecutting recipes at construction time, with a defensive sweep on datapack reload and client network sync.
    - Shares lazy `ItemStack[]` and `IntList` cache arrays across identical ingredients, reclaiming tens of thousands of duplicate arrays in JEI/REI/EMI and recipe matching.
- [x] **StateDefinition Property Map Deduplication**:
  - `StatePropertyDeduplicator` + `StateDefinitionMixin`:
    - Canonicalizes Guava `ImmutableSortedMap<String, Property<?>>` across all `StateDefinition` (Block, Fluid, etc.) instances during registration.
    - Eliminates redundant immutable map instances for blocks sharing identical property topologies (stairs, slabs, walls, fences, doors, leaves, logs).
    - Backed by thread-safe `ConcurrentHashMap` with clean post-launch memory reclamation.
- [x] **VoxelShape Collision Bounding Box Singletons**:
  - `CollisionDeduplicator` + `VoxelShapeMixin`:
    - Caches unmodifiable `List<AABB>` and outer bounds directly on `VoxelShape` on first call via thread-safe benign race publication.
    - Eliminates continuous heap allocation churn from `toAabbs()` and `bounds()` during raycasting, arrow collision, hopper ticks, and rendering.
    - Canonicalizes `AABB` instances via `ConcurrentHashMap` pool and provides zero-allocation singletons for full cubes (`Shapes.block()`) and empty shapes.

### Verified Mod Compatibility
- Successfully tested and verified in active runtime alongside:
  - **Sodium 0.8.13** & **Lithium 0.15.4**
  - **FerriteCore 7.0.3** (seamless table interop and auto-yield)
  - **ModernFix 5.27.24**
  - **ImmediatelyFast 1.6.14** & **BadOptimizations 2.4.1**
  - **Cloth Config v15**

---

## Phase 2: Diagnostics, Verification & Modpack Hardening (Current Focus)

### Target Goal: Provide non-intrusive memory metrics and harden edge cases for heavy modpacks.

- [x] **Passive In-Game Memory Diagnostics (`/memfixer status`)**:
  - Lightweight, non-allocating command accessible to players and server administrators (`/memfixer status`, `/memfixer info`, `/memfixer help`).
  - Read-only introspection: Displays live JVM heap metrics (Used/Max/Committed), interned geometry counts, deduplicated shape counts, and active memory savings without touching game state.
  - Zero side-effects: Operates purely as a passive observer. Tested in live integrated server and singleplayer.

- [x] **Automated JUnit 5 Test Suite**:
  - Full automated regression test coverage across all core optimization algorithms in `src/test/java/com/memfixer/modules/`.
  - 40 unit tests covering: `CollisionDeduplicator`, `StatePropertyDeduplicator`, `FastNeighbourTable`, `ChunkStorageOptimizer`, `QuadDeduplicator`, `ConditionDeduplicator`, `ResourceInterner`, `TagDeduplicator`, `IngredientDeduplicator`, `ShapeDeduplicator`, `HandlerModules` (DFU, Texture, Font), and `MemFixerConfig`.
  - Verified 100% pass rate in standard Gradle build pipeline (`.\gradlew test` and `.\gradlew build`).

- [x] **Algorithmic Ratio Optimization & Deep Compaction**:
  - `FastNeighbourTable`: Deterministic cell sorting collapses cross-block redundant key arrays down to standard shared topologies (88% - 98% array sharing).
  - `TagDeduplicator`: Order-independent `Holder` registry name sorting ensures matching contents merge into shared list singletons.
  - `QuadDeduplicator`: IEEE 754 negative zero (`-0.0f`) bitwise normalization eliminates false geometry misses.
  - `CollisionDeduplicator`: Real-time fast-path cache hit tracking achieves 99.6% zero-allocation hits during in-game movement.

2. **Heavy Modpack Validation & Edge-Case Hardening**:
   - Extensive validation across major modpacks (e.g., AllTheMods 10, 200+ mod environments).
   - Ensure custom blockstates, non-standard multipart models, and custom font providers from complex mods (Create, Mekanism, Ars Nouveau) interact flawlessly with canonical deduplication pools.

---

## Phase 3: Long-Term Maintenance & Platform Parity (Planned)

### Target Goal: Maintain flawless stability across Minecraft minor releases.

1. **Continuous Stack Alignment**:
   - Keep mixin injection points resilient against upstream NeoForge, Sodium, and ModernFix updates.
   - Ongoing benchmark testing to ensure zero regression across newer JDK releases.
