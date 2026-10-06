package com.memfixer.command;

import com.memfixer.client.ClientTelemetry;
import com.memfixer.config.MemFixerConfig;
import com.memfixer.modules.chunk.ChunkStorageOptimizer;
import com.memfixer.modules.collision.CollisionDeduplicator;
import com.memfixer.modules.recipe.IngredientDeduplicator;
import com.memfixer.modules.shape.ShapeDeduplicator;
import com.memfixer.modules.state.FastNeighbourTable;
import com.memfixer.modules.state.StatePropertyDeduplicator;
import com.memfixer.modules.tag.TagDeduplicator;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.fml.loading.FMLEnvironment;

/**
 * Passive, read-only in-game diagnostic command for MemFixer.
 * Displays live JVM memory metrics and module deduplication savings
 * without altering game state or allocating garbage.
 */
@SuppressWarnings("null")
public final class MemFixerCommand {

    private static final int TOTAL_MODULES = 15;

    private MemFixerCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("memfixer")
                        .requires(source -> true) // Allow all players & console to view status
                        .then(Commands.literal("status").executes(context -> sendStatus(context.getSource())))
                        .then(Commands.literal("info").executes(context -> sendInfo(context.getSource())))
                        .then(Commands.literal("help").executes(context -> sendHelp(context.getSource())))
                        .executes(context -> sendStatus(context.getSource()))
        );
    }

    private static int sendHelp(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("§6[MemFixer]§r Commands:")
                .append(Component.literal("\n §e/memfixer status§r - Live memory metrics & deduplication telemetry")
                        .withStyle(ChatFormatting.GRAY))
                .append(Component.literal("\n §e/memfixer info§r - Mod version, platform metadata & config location")
                        .withStyle(ChatFormatting.GRAY))
                .append(Component.literal("\n §e/memfixer help§r - Displays available commands")
                        .withStyle(ChatFormatting.GRAY)), false);
        return 1;
    }

    private static String getModVersion() {
        try {
            return net.neoforged.fml.ModList.get()
                    .getModContainerById("memfixer")
                    .map(container -> "v" + container.getModInfo().getVersion().toString())
                    .orElse("v0.1.0+mc1.21.1");
        } catch (Throwable t) {
            return "v0.1.0+mc1.21.1";
        }
    }

    private static String getPlatformInfo() {
        try {
            String javaVer = System.getProperty("java.specification.version", "21");
            return net.neoforged.fml.ModList.get()
                    .getModContainerById("neoforge")
                    .map(container -> "NeoForge " + container.getModInfo().getVersion().toString() + " (Java " + javaVer + ")")
                    .orElse("NeoForge (Java " + javaVer + ")");
        } catch (Throwable t) {
            return "NeoForge (Java 21)";
        }
    }

    private static int sendInfo(CommandSourceStack source) {
        MutableComponent info = Component.literal("§6══════════ MemFixer Mod Info ══════════\n")
                .append(Component.literal(" §7• §bVersion: §f" + getModVersion() + "\n"))
                .append(Component.literal(" §7• §bAuthor: §fEgemen\n"))
                .append(Component.literal(" §7• §bPlatform: §f" + getPlatformInfo() + "\n"))
                .append(Component.literal(" §7• §bActive Subsystems: §a" + countActiveModules() + " / " + TOTAL_MODULES + " §7enabled\n"))
                .append(Component.literal(" §7• §bConfig: §econfig/memfixer.properties\n"))
                .append(Component.literal(" §7• §bIn-Game GUI: §fMods -> MemFixer -> Config (Cloth Config)\n"))
                .append(Component.literal("§6══════════════════════════════════════════"));
        source.sendSuccess(() -> info, false);
        return 1;
    }

    private static int sendStatus(CommandSourceStack source) {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        long usedMemory = totalMemory - freeMemory;

        long usedMb = usedMemory / (1024 * 1024);
        long maxMb = maxMemory / (1024 * 1024);
        long committedMb = totalMemory / (1024 * 1024);
        int percentUsed = maxMemory > 0 ? (int) ((usedMemory * 100) / maxMemory) : 0;

        MutableComponent report = Component.empty();

        // Header
        report.append(Component.literal("§6══════════ §eMemFixer Memory Status §6══════════\n"));

        // JVM Heap line
        ChatFormatting heapColor = percentUsed > 80 ? ChatFormatting.RED : (percentUsed > 60 ? ChatFormatting.YELLOW : ChatFormatting.GREEN);
        report.append(Component.literal(" §7JVM Heap: §f" + usedMb + " MB §7/ §f" + maxMb + " MB §7(")
                .append(Component.literal(percentUsed + "%").withStyle(heapColor))
                .append(Component.literal("§7) | Committed: §f" + committedMb + " MB\n")));

        // Quad Deduplicator (Client-only)
        if (FMLEnvironment.dist.isClient()) {
            ClientTelemetry.appendModelQuads(report, MemFixerCommand::formatNumber);
        }

        // Shape Deduplicator
        int faceSets = ShapeDeduplicator.getCachedFaceSetsCount();
        int sturdyMasks = ShapeDeduplicator.getCachedSturdyMasksCount();
        int faceHits = ShapeDeduplicator.getFaceDedupHits();
        report.append(Component.literal(" §7• §bVoxelShapes: §f" + formatNumber(faceSets) + " §7canonical face sets, §f"
                + formatNumber(sturdyMasks) + " §7sturdy masks §8(" + formatNumber(faceHits) + " hits)\n"));

        // Multipart Conditions (Client-only)
        if (FMLEnvironment.dist.isClient()) {
            ClientTelemetry.appendMultipartConditions(report, MemFixerCommand::formatNumber);
        }

        // BlockState Neighbour Tables
        if (FastNeighbourTable.isFerriteCoreActive()) {
            report.append(Component.literal(" §7• §bState Tables: §aYielded to FerriteCore FastMap §8(interop safe)\n"));
        } else {
            int compactedStates = FastNeighbourTable.getCompactedCount();
            int sharedKeys = FastNeighbourTable.getSharedKeysCount();
            report.append(Component.literal(" §7• §bState Tables: §f" + formatNumber(compactedStates) + " §7states compacted (§f"
                    + formatNumber(sharedKeys) + " §7O(1) shared key arrays)\n"));
        }

        // Chunk Storage
        long zeroBitSaved = ChunkStorageOptimizer.getZeroBitSaved();
        long detectorsSaved = ChunkStorageOptimizer.getDetectorsSaved();
        report.append(Component.literal(" §7• §bChunk Storage: §f" + formatNumber(zeroBitSaved) + " §7ZeroBit singletons, §f"
                + formatNumber(detectorsSaved) + " §7ThreadingDetectors eliminated\n"));

        // Fonts & Textures (Client-only)
        if (FMLEnvironment.dist.isClient()) {
            ClientTelemetry.appendFontAndTextures(report, MemFixerCommand::formatNumber);
        } else {
            report.append(Component.literal(" §7• §bClient Subsystems: §8(Quads, Textures, Fonts active on connected clients)\n"));
        }

        // Registry Tags & HolderSets
        int tagSets = TagDeduplicator.getCanonicalTagSetsCount();
        int tagHits = TagDeduplicator.getTagSetHits();
        int holderLists = TagDeduplicator.getCanonicalHolderListsCount();
        int holderHits = TagDeduplicator.getHolderListHits();
        report.append(Component.literal(" §7• §bRegistry Tags: §f" + formatNumber(tagSets) + " §7tag sets (§f"
                + formatNumber(tagHits) + " §7hits), §f" + formatNumber(holderLists) + " §7holder lists (§f"
                + formatNumber(holderHits) + " §7hits)\n"));

        // Recipe Ingredients
        int canonicalIngredients = IngredientDeduplicator.getCanonicalCount();
        int ingredientHits = IngredientDeduplicator.getDedupHits();
        int recipesOptimized = IngredientDeduplicator.getRecipesOptimized();
        report.append(Component.literal(" §7• §bRecipes: §f" + formatNumber(canonicalIngredients) + " §7canonical ingredients (§f"
                + formatNumber(ingredientHits) + " §7hits across §f" + formatNumber(recipesOptimized) + " §7recipes)\n"));

        // StateDefinition Property Maps
        int canonicalMaps = StatePropertyDeduplicator.getCanonicalMapsCount();
        int stateDefHits = StatePropertyDeduplicator.getDedupHits();
        int stateDefRequests = StatePropertyDeduplicator.getTotalRequests();
        report.append(Component.literal(" §7• §bProperty Maps: §f" + formatNumber(canonicalMaps) + " §7canonical maps (§f"
                + formatNumber(stateDefHits) + " §7hits across §f" + formatNumber(stateDefRequests) + " §7definitions)\n"));

        // Collision Bounding Boxes
        int canonicalAabbs = CollisionDeduplicator.getCanonicalAabbsCount();
        int aabbHits = CollisionDeduplicator.getCacheHits();
        int aabbRequests = CollisionDeduplicator.getTotalRequests();
        report.append(Component.literal(" §7• §bBounding Boxes: §f" + formatNumber(canonicalAabbs) + " §7canonical AABBs (§f"
                + formatNumber(aabbHits) + " §7hits across §f" + formatNumber(aabbRequests) + " §7queries)\n"));

        // Active modules summary
        int activeModules = countActiveModules();
        report.append(Component.literal(" §7• §bActive Modules: §a" + activeModules + " / " + TOTAL_MODULES + " §7enabled\n"));
        report.append(Component.literal("§6══════════════════════════════════════════"));

        source.sendSuccess(() -> report, false);
        return 1;
    }

    private static int countActiveModules() {
        int count = 0;
        if (MemFixerConfig.LAZY_DFU) count++;
        if (MemFixerConfig.OPTIMIZE_UNIFONT) count++;
        if (MemFixerConfig.REAP_TEXTURES) count++;
        if (MemFixerConfig.DEDUPLICATE_QUADS) count++;
        if (MemFixerConfig.COMPACT_BLOCKSTATES) count++;
        if (MemFixerConfig.OPTIMIZE_PANORAMA) count++;
        if (MemFixerConfig.DEDUPLICATE_SHAPES) count++;
        if (MemFixerConfig.DEDUPLICATE_CONDITIONS) count++;
        if (MemFixerConfig.INTERN_RESOURCES) count++;
        if (MemFixerConfig.RECLAIM_POST_LAUNCH) count++;
        if (MemFixerConfig.OPTIMIZE_CHUNK_STORAGE) count++;
        if (MemFixerConfig.DEDUPLICATE_TAGS) count++;
        if (MemFixerConfig.DEDUPLICATE_INGREDIENTS) count++;
        if (MemFixerConfig.DEDUPLICATE_STATE_DEFINITIONS) count++;
        if (MemFixerConfig.DEDUPLICATE_AABB_CACHE) count++;
        return count;
    }

    private static String formatNumber(long number) {
        return String.format("%,d", number);
    }
}
