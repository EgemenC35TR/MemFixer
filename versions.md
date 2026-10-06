# MemFixer Version Matrix & Multi-Version Roadmap

Comprehensive testing, porting, and verification tracking matrix across all 18 Minecraft versions (1.21.x series and 26.x calendar Game Drops).

---

## 1. Complete Version Tracking Matrix (18 Versions)

| # | Minecraft Version | Release / Drop Name | Release Date | Java Version | NeoForge Target | MemFixer Status | Key Technical Notes |
| :-: | :--- | :--- | :--- | :-: | :---: | :---: | :--- |
| **1** | `1.21` | [Tricky Trials](https://minecraft.wiki/w/Tricky_Trials) | June 13, 2024 | Java 21 | `21.0.x` | `[-] Omitted (1.21.1 Parity)` | Bytecode parity with 1.21.1; covered by 1.21.1 build range. |
| **2** | `1.21.1` | [Tricky Trials (Hotfix)](https://minecraft.wiki/w/Java_Edition_1.21.1) | August 8, 2024 | Java 21 | `21.1.x` | `[x] Active Baseline (v0.1.0)` | Current primary baseline. 15 subsystems and 40 automated tests verified. |
| **3** | `1.21.2` | [Bundles of Bravery](https://minecraft.wiki/w/Bundles_of_Bravery) | October 22, 2024 | Java 21 | `21.2.x` | `[-] Omitted (Superseded)` | Initial Client Items refactor; superseded by 1.21.4. |
| **4** | `1.21.3` | [Bundles of Bravery (Hotfix)](https://minecraft.wiki/w/Java_Edition_1.21.3) | October 23, 2024 | Java 21 | `21.3.x` | `[-] Omitted (Superseded)` | Emergency crash fix for 1.21.2; superseded by 1.21.4. |
| **5** | `1.21.4` | [The Garden Awakens](https://minecraft.wiki/w/The_Garden_Awakens) | December 3, 2024 | Java 21 | `21.4.x` | `[ ] Active Target 1` | Pale Garden, Creaking mob, and stable Client Items refactor. |
| **6** | `1.21.5` | [Spring to Life](https://minecraft.wiki/w/Spring_to_Life) | March 25, 2025 | Java 21 | `21.5.x` | `[ ] Active Target 2` | Q1 2025 drop: Quality of life and environmental block enhancements. |
| **7** | `1.21.6` | [Chase the Skies](https://minecraft.wiki/w/Chase_the_Skies) | June 17, 2025 | Java 21 | `21.6.x` | `[-] Omitted (Superseded)` | Atmospheric drop; superseded by 1.21.8 stability release. |
| **8** | `1.21.7` | [Chase the Skies (Hotfix 1)](https://minecraft.wiki/w/Java_Edition_1.21.7) | June 30, 2025 | Java 21 | `21.7.x` | `[-] Omitted (Superseded)` | Network protocol patch; superseded by 1.21.8. |
| **9** | `1.21.8` | [Chase the Skies (Hotfix 2)](https://minecraft.wiki/w/Java_Edition_1.21.8) | July 17, 2025 | Java 21 | `21.8.x` | `[ ] Active Target 3` | Stable release of the Chase the Skies series; collision updates. |
| **10** | `1.21.9` | [The Copper Age](https://minecraft.wiki/w/The_Copper_Age) | September 30, 2025 | Java 21 | `21.9.x` | `[-] Omitted (Superseded)` | Initial Copper drop; superseded by 1.21.11 finale. |
| **11** | `1.21.10` | [The Copper Age (Hotfix)](https://minecraft.wiki/w/Java_Edition_1.21.10) | October 7, 2025 | Java 21 | `21.10.x` | `[-] Omitted (Superseded)` | Copper hotfix; superseded by 1.21.11 finale. |
| **12** | `1.21.11` | [Mounts of Mayhem](https://minecraft.wiki/w/Mounts_of_Mayhem) | December 9, 2025 | Java 21 | `21.11.x` | `[ ] Active Target 4` | Final 1.21 update. Mount mechanics and physics overhaul. |
| **13** | `26.1` | [Tiny Takeover](https://minecraft.wiki/w/Tiny_Takeover) | March 24, 2026 | **Java 25** | `26.1.x` | `[-] Omitted (Superseded)` | Initial Java 25 release; superseded by 26.1.2 hotfix. |
| **14** | `26.1.1` | [Tiny Takeover (Hotfix 1)](https://minecraft.wiki/w/Java_Edition_26.1.1) | April 1, 2026 | Java 25 | `26.1.1.x` | `[-] Omitted (Superseded)` | Intermediate patch; superseded by 26.1.2. |
| **15** | `26.1.2` | [Tiny Takeover (Hotfix 2)](https://minecraft.wiki/w/Java_Edition_26.1.2) | April 9, 2026 | Java 25 | `26.1.2.x` | `[ ] Active Target 5` | Stable production release of Tiny Takeover on Java 25. |
| **16** | `26.2` | [Chaos Cubed](https://minecraft.wiki/w/Chaos_Cubed) | June 16, 2026 | Java 25 | `26.2.x` | `[ ] Active Target 6` | Second 2026 Game Drop: Dungeon structures and spatial mechanics. |
| **17** | `26.3` | [Wilderness Bound](https://minecraft.wiki/w/Wilderness_Bound) | September 15, 2026 | Java 25 | `26.3.x` | `[ ] Active Target 7` | Current production release. Mottled Forest biome, poplar wood. |
| **18** | `26.4` | [Fourth Drop 2026](https://minecraft.wiki/w/Fourth_Drop_2026) | Upcoming Q4 2026 | Java 25 | `26.4.x` | `[-] Tracking Development` | Unreleased snapshot track. |

---

## 2. Curated Active Porting Track (8 Milestone Releases)

Development and porting efforts are strictly focused on the following 8 milestone releases:

1. **`1.21.1` (Baseline):** Complete and verified in production with 40 automated tests.
2. **`1.21.4` (Target 1):** The Garden Awakens (Creaking, Pale Garden, and initial stable Client Items).
3. **`1.21.5` (Target 2):** Spring to Life (Environmental updates and block refinements).
4. **`1.21.8` (Target 3):** Chase the Skies (Refined flight physics and entity collision sync).
5. **`1.21.11` (Target 4):** Mounts of Mayhem (1.21 series final milestone).
6. **`26.1.2` (Target 5):** Tiny Takeover (Stable Java 25 baseline release).
7. **`26.2` (Target 6):** Chaos Cubed (Spatial structures and new mechanics).
8. **`26.3` (Target 7):** Wilderness Bound (Latest active production release).

### Architectural Implementation Groups

```
[1.21.1]                      -> Group A (Classic 1.21 Baseline - Java 21) [COMPLETED]
[1.21.4, 1.21.5, 1.21.8, 1.21.11] -> Group B (Data-Driven Client Items - Java 21) [NEXT]
[26.1.2, 26.2, 26.3]          -> Group C (Modern Game Drops - Java 25) [FUTURE]
```

### Group A: Classic 1.21 Baseline (1.21, 1.21.1)
* **Java:** 21
* **Status:** `1.21.1` is 100% complete and verified with 40 unit tests. `1.21` requires backward compatibility verification.
* **Target Modules:** Unmodified. Existing code runs directly.

### Group B: Data-Driven Model System (1.21.2 -> 1.21.11)
* **Java:** 21
* **Critical Changes:** `ModelBakery`, `ItemModelGenerator`, and unbaked cache maps refactored into data-driven item definitions.
* **Target Refactoring Modules:**
  * `ModelBakeryMixin`
  * `ModelCleaner`
  * `ItemModelGeneratorMixin`
  * `MultiPartBakedModelBuilderMixin`
* **Stable Modules (Zero Modification Required):** `FastNeighbourTable`, `ChunkStorageOptimizer`, `CollisionDeduplicator`, `TextureReaperHandler`, `ResourceInterner`, `IngredientDeduplicator`.

### Group C: Modern Game Drops & Java 25 Era (26.1 -> 26.4)
* **Java:** **Java 25** (JVM classfile major version 69)
* **Build System:** Gradle 9.1+ with modern ModDevGradle / NeoGradle plugin.
* **Critical Changes:** Java 25 compiler toolchain, bytecode target updates, and package relocations.

---

## 3. Verification & Quality Assurance Procedure

For every individual target version, the following verification checklist must pass unconditionally:

1. [ ] **Compilation Verification:** `.\gradlew.bat compileJava` succeeds with zero errors.
2. [ ] **Automated Test Suite:** `.\gradlew.bat test` passes all 40 unit tests cleanly.
3. [ ] **Client Initialization:** Client boots cleanly, TitleScreen stabilizes, and `MemoryReclaimer` activates without exception.
4. [ ] **Runtime Telemetry:** `/memfixer status` and `/memfixer info` display accurate metrics without heap allocation churn.
5. [ ] **World Lifecycle:** Entering and exiting singleplayer and multiplayer worlds introduces no leaks, stalls, or crashes.

---

## 4. Migration & Stale Reference Audit Directive

Whenever porting or updating MemFixer to a new Minecraft version, the following audit checklist MUST be systematically verified across the repository to ensure no obsolete version constraints, stale descriptions, or hardcoded strings remain.

### Audit Step 1: Mod Manifest & Dependency Bounds (`src/main/resources/META-INF/neoforge.mods.toml`)
- [ ] **Mod Version:** `version="<mod_version>+mc<target_mc_version>"` matches the intended release.
- [ ] **Description Field:** Confirm target Minecraft version text reflects the exact target range (never use open-ended plus signs such as `1.21.1+`).
- [ ] **Minecraft Version Range:** `versionRange` MUST strictly define both lower and upper bounds (e.g., `[1.21.4, 1.21.5)` or `[26.3.0, 26.4.0)`). Open-ended ranges (`[X,)`) are strictly forbidden because breaking internal API refactors cause client startup crashes.
- [ ] **NeoForge Version Range:** `versionRange` MUST strictly define the exact compatible NeoForge version series (e.g., `[21.4.0, 21.5.0)` or `[26.3.0, 26.4.0)`).
- [ ] **Cloth Config Compatibility:** Verify `cloth_config` dependency range matches the target platform's Cloth Config major version.

### Audit Step 2: Build Toolchain & Dependencies (`build.gradle`)
- [ ] **Project Version:** Update `version = '<mod_version>+mc<target_mc_version>'`.
- [ ] **Java Toolchain:** Verify `java.toolchain.languageVersion = JavaLanguageVersion.of(X)` matches target requirement (Java 21 for Group A/B, Java 25 for Group C).
- [ ] **NeoForge Dependency:** Update `neoForge.version` to the verified target release.
- [ ] **Gradle Wrapper:** Confirm `gradle-wrapper.properties` targets Gradle 9.1+ if compiling for Java 25.
- [ ] **Plugin Version:** Ensure `net.neoforged.moddev` or `net.neoforged.gradle` plugin coordinates match the target NeoForge ecosystem.

### Audit Step 3: Mixin Configuration (`src/main/resources/memfixer.mixins.json`)
- [ ] **Compatibility Level:** Update `"compatibilityLevel"` to match target JVM (`JAVA_21` or `JAVA_25`).
- [ ] **Mixin Target Audit:** Verify target classes and methods exist in the target Minecraft JAR. For Group B (1.21.2+), inspect refactored `ModelBakery` and `ItemModel` classes. Remove or update mixins whose target signatures have changed.

### Audit Step 4: Runtime Diagnostics & Fallback Strings (`src/main/java/com/memfixer/command/MemFixerCommand.java`)
- [ ] **Dynamic Resolution:** Ensure `getModVersion()` and `getPlatformInfo()` continue to resolve dynamically through `ModList.get().getModContainerById(...)`.
- [ ] **Fallback Strings:** Verify fallback strings provide accurate defaults for the target branch if `ModList` is unavailable.

### Audit Step 5: Documentation & Artifact Metadata
- [ ] **README.md:** Update header summary, platform line, build command JDK prerequisite, and expected output JAR file name in `build/libs/`.
- [ ] **docs/ARCHITECTURE.md:** Update target platform specification and any version-specific bytecode profiling references.
- [ ] **docs/ROADMAP.md:** Update target engine versions and ecosystem compatibility table.
- [ ] **CHANGELOG.md:** Add entry under `[Unreleased]` or target release section adhering to Keep a Changelog standards.
- [ ] **versions.md:** Update the target version row status from `[ ] Pending Verification` to `[x] Active / Verified`.

### Audit Step 6: Packaged Artifact Bytecode & Manifest Inspection
Before publishing, run:
```bash
.\gradlew.bat clean build
```
Inspect the output JAR archive directly to confirm packaged resources contain no stale references:
```powershell
tar -xOf build/libs/memfixer-neoforge-<version>.jar META-INF/neoforge.mods.toml
```
Ensure the packaged `versionRange` attributes match the exact bounds defined in Audit Step 1.

