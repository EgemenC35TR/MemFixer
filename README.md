# MemFixer

High-performance, ultra-low memory optimization engine for Minecraft 1.21.1 on NeoForge (Java 21).  
**Author**: Egemen

MemFixer reduces JVM heap footprint, eliminates duplicate baked quads, compacts block state transition tables, bypasses multi-object synchronization lock churn, and releases CPU-side memory buffers without sacrificing visual fidelity or render distance.

---

## Proven Live Telemetry & Real-World Benchmarks

Measured directly in active client runtime alongside standard optimization mods (Sodium, Lithium, ModernFix, ImmediatelyFast, BadOptimizations):

| Optimization Subsystem | Measured Live Metric | Hit Rate / Compaction |
| :--- | :--- | :--- |
| **Bounding Box Collision Caching** | 520 hits across 522 queries | **99.6%** (Zero-Allocation) |
| **Recipe Ingredient Canonicalization** | 17,204 duplicate ingredients eliminated | **96.8%** hit rate |
| **Multipart Condition Pairs** | 110,902 model pairs deduplicated, 761 predicates cached | **96.4%** savings |
| **Chunk Storage & Threading Detectors** | 185,023 ZeroBit singletons, 224,210 ThreadingDetectors bypassed | **1,121,050 lock objects eliminated** |
| **BlockState Transition Graphs** | 26,350 states compacted into 3,163 shared O(1) arrays | **88.0%** array sharing |
| **StateDefinition Property Maps** | 584 duplicate maps eliminated across 693 definitions | **84.3%** hit rate |
| **Model BakedQuad Geometry** | 122,349 duplicate quads eliminated from 175,074 instances | **69.8%** (Physical geometry ceiling) |
| **CPU Native Image Buffer Reaper** | 2,144 static sprite buffers freed from RAM post-GPU upload | **100%** static buffers reaped |
| **Lazy Unicode Glyphs (CJK)** | 66,863 unused Asian glyphs deferred to off-heap buffer | **~85%** font heap reduction (~1 MB) |
| **Active JVM Heap Footprint** | Measured at singleplayer steady-state in render distance 12 | **~385 MB** (9% of 4096 MB) |

---

## 15 Core Optimization Subsystems

| # | Subsystem | Engineering Mechanism |
| :---: | :--- | :--- |
| **1** | **Quad Deduplicator** | Dynamically-resizing linear-probing open-addressing table pooling identical `BakedQuad` instances during model baking with IEEE 754 negative-zero (`-0.0f`) bitwise normalization. |
| **2** | **FastNeighbourTable** | Replaces heavy Guava tables across 26,000+ BlockState transition graphs with compact, shared-key index arrays sorted deterministically by property and value. |
| **3** | **BlockState Cache Optimizer** | Compresses individual boolean collision and render flags into 64-bit BitSets, reducing per-state cache memory by 87.5%. |
| **4** | **Chunk Storage Optimizer** | Replaces uniform and empty 16x16x16 chunk sub-sections with shared zero-allocation singletons (`STATIC_4096`, `STATIC_64`) and eliminates per-container `ThreadingDetector` allocation churn. |
| **5** | **Lazy Unihex Glyphs** | Bypasses eager loading of 65,000+ unused CJK Unicode glyphs in non-Asian locales, loading glyphs on demand into an off-heap direct buffer. |
| **6** | **Model AST Cleaner** | Purges temporary blockstate AST trees, unbaked definitions, and key maps upon model bake finalization. |
| **7** | **Condition Deduplicator** | Canonicalizes multipart model state predicates and model pairs, deduplicating over 110,000 model pairs into shared singletons. |
| **8** | **Tag Deduplicator** | Canonicalizes duplicate `Set<TagKey>` and `List<Holder>` collections across registry entries with deterministic Holder sorting by registry name. |
| **9** | **Resource Interner** | Connects duplicate `ResourceLocation` namespaces and paths directly to the JVM HotSpot native `String.intern()` table. |
| **10** | **Lazy DFU** | Defers eager DataFixerUpper schema rule compilation on startup until a world save requiring upgrade is loaded. |
| **11** | **Texture Reaper** | Releases CPU-side `NativeImage` pixel buffers to the operating system immediately following successful GPU VRAM upload, while preserving animated sprite tickers. |
| **12** | **Shape Deduplicator** | Deduplicates VoxelShape face slices and merges sturdy face boolean arrays into shared canonical masks. |
| **13** | **Ingredient Deduplicator** | Canonicalizes identical `Ingredient` instances across shaped, shapeless, and furnace recipes into shared singletons. |
| **14** | **StateProperty Deduplicator** | Canonicalizes duplicate immutable `Property<?>` maps across `StateDefinition` instances into shared canonical singletons. |
| **15** | **Collision Deduplicator** | Caches unmodifiable `AABB` lists and outer bounds on `VoxelShape` instances and interns bounding box singletons to eliminate raycast and collision allocation churn. |

---

## Mod Compatibility & Ecosystem Coexistence

MemFixer is engineered from the ground up to coexist seamlessly with the modern optimization stack:

* **Sodium / Embeddium**: Fully compatible. Vertex data deduplication and chunk compaction run seamlessly alongside modern GPU meshing.
* **Lithium**: Fully compatible. World logic and chunk section access optimizations chain cleanly with MixinExtras `@WrapOperation`.
* **FerriteCore**: Fully compatible. When FerriteCore is installed, MemFixer safely yields BlockState transition table compaction to FerriteCore to avoid table conflicts, while continuing to apply all other 14 optimizations. When FerriteCore is not installed, MemFixer engages its own built-in canonical O(1) `FastNeighbourTable`.
* **ModernFix**: Fully compatible. Dynamic rule loading, datapack caching, and bootstrap stages operate without interference.
* **ImmediatelyFast & BadOptimizations**: Fully compatible across font, HUD, and entity rendering pipelines.
* **Cloth Config v15**: Fully integrated for in-game configuration screens.

---

## In-Game Diagnostic Commands

MemFixer provides lightweight, passive, read-only diagnostic commands accessible to players and server administrators:

* `/memfixer status`: Displays live JVM heap metrics (Used/Max/Committed) and real-time deduplication telemetry across all active subsystems without allocating garbage or touching world state.
* `/memfixer info`: Displays mod metadata, platform details, active subsystem status, and configuration file paths.
* `/memfixer help`: Displays command usage syntax.

---

## Configuration & In-Game GUI

MemFixer provides two configuration mechanisms:
1. **Config File (`config/memfixer.properties`)**: Automatically generated on first launch. Works identically on clients and dedicated servers.
2. **In-Game GUI Screen**: Accessible via **Mods -> MemFixer -> Config** using Cloth Config. Fully localized in English (`en_us`) and Turkish (`tr_tr`).

Every subsystem — including diagnostic console logging (`enable_logging`) — can be toggled individually.

---

## Quality Assurance & Automated Testing

MemFixer maintains a comprehensive automated JUnit 5 test suite with 40 unit tests across 11 test classes covering:
* Null and boundary safety
* Concurrency and multi-thread data race prevention
* Order-independent canonicalization
* Configuration disable fallbacks

Run the test suite with:
```bash
.\gradlew.bat test
```

---

## Documentation

* [Architecture & Engineering Specification](docs/ARCHITECTURE.md)
* [Development Roadmap & Subsystem Status](docs/ROADMAP.md)
* [Version Matrix & Multi-Version Roadmap](versions.md)
* [Git Workflow & Commit Guidelines](docs/GIT_GUIDE.md)
* [Version Changelog](CHANGELOG.md)

---

## Building from Source

Requires JDK 21.

```bash
.\gradlew.bat build
```

The output mod JAR (`memfixer-neoforge-0.1.0+mc1.21.1.jar`) will be located in `build/libs/`.
