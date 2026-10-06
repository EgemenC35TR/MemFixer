# Project Rules and Operational Directives

## 1. Language Directives
- **Project Artifacts**: All project code, comments, documentation, commit messages, and internal architecture notes MUST be written in English.
- **User Communication**: Communication with the user MUST be conducted in Turkish, except where technical terms explicitly dictate otherwise.

## 2. Engineering & Methodological Rigor
- **No Guesswork / Assumptions**: Every architectural choice, patch, and memory-saving optimization must be backed by verified profiling data (JOL, Async-profiler, JFR, Heap Dumps) and bytecode analysis. Speculative changes without verification are forbidden.
- **Planned Execution**: All modifications and features must follow explicit, modular, documented implementation plans.
- **Tone & Formatting**: Strictly NO emojis in any communication, code, or documentation.

## 3. Project Target & Scope
- **Core Objective**: Optimize Minecraft memory consumption to the absolute physical minimum (target ceiling: 186 MB process memory) without compromising FPS or visual quality.
- **Target Platform**: NeoForge starting from Minecraft 1.21+ (Java 21), designed for forward-compatibility with future releases.
- **Delivery Format**: Standard mod JAR drop-in into the `mods/` directory. No mandatory custom JVM installation for end-users.
- **Compatibility Target**: Zero visual degradation; 100% compatibility with Sodium/Embeddium, modern rendering pipelines, and extensive modpacks.
- **Performance Requirement**: Uncompromised high frame rates (1000+ FPS capable on adequate hardware), zero micro-stutters, no blocking disk paging during gameplay.

## 4. Rule Adherence
- Rules defined herein are strictly binding unless explicit, documented exceptions are authorized by the user.
