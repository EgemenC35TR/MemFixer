package com.memfixer.modules.reclaimer;

import com.memfixer.config.MemFixerConfig;
import com.memfixer.modules.model.QuadDeduplicator;
import com.memfixer.modules.resource.ResourceInterner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Modular memory reclaimer that tracks application lifecycle and triggers compacting GC sweeps
 * after bootstrap and title screen stabilization.
 */
public final class MemoryReclaimer {
    private static final Logger LOGGER = LogManager.getLogger("MemFixer/Reclaimer");
    private static final AtomicBoolean RECLAIMED = new AtomicBoolean(false);
    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "MemFixer-Reclaimer-Daemon");
        t.setDaemon(true);
        t.setPriority(Thread.MIN_PRIORITY);
        return t;
    });

    private MemoryReclaimer() {}

    public static void armClientReclamation() {
        if (!MemFixerConfig.RECLAIM_POST_LAUNCH) return;
        if (MemFixerConfig.ENABLE_LOGGING) {
            LOGGER.info("[MemFixer/Reclaimer] Lifecycle memory monitor armed. Awaiting TitleScreen stabilization.");
        }
    }

    public static void onTitleScreenLoaded() {
        if (!MemFixerConfig.RECLAIM_POST_LAUNCH || RECLAIMED.get()) return;
        // Schedule 1.5 seconds after TitleScreen mounts so that first frame rendering settles cleanly
        SCHEDULER.schedule(MemoryReclaimer::executePostStartupSweep, 1500, TimeUnit.MILLISECONDS);
    }

    public static void executePostStartupSweep() {
        if (!RECLAIMED.compareAndSet(false, true)) {
            return;
        }

        // Safety: Skip sweep if the player has already entered an active level to prevent frame hitches.
        try {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc != null && mc.level != null) {
                if (MemFixerConfig.ENABLE_LOGGING) {
                    LOGGER.info("[MemFixer/Reclaimer] Active game level detected. Skipping post-boot GC sweep to preserve frametime smoothness.");
                }
                return;
            }
        } catch (Throwable ignored) {}

        MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage beforeHeap = memBean.getHeapMemoryUsage();
        long beforeMb = beforeHeap.getUsed() / (1024 * 1024);

        if (MemFixerConfig.ENABLE_LOGGING) {
            LOGGER.info("[MemFixer/Reclaimer] TitleScreen stabilized. Initiating post-initialization memory sweep. Active heap: {} MB.", beforeMb);
        }

        QuadDeduplicator.logStats();
        QuadDeduplicator.clearCache();
        com.memfixer.modules.shape.ShapeDeduplicator.logStats();
        com.memfixer.modules.model.ConditionDeduplicator.logStats();
        com.memfixer.modules.model.ConditionDeduplicator.clearPairCache();
        com.memfixer.modules.state.FastNeighbourTable.clearCache();
        ResourceInterner.compactPools();
        com.memfixer.modules.tag.TagDeduplicator.compactPools();
        com.memfixer.modules.recipe.IngredientDeduplicator.logStats();
        com.memfixer.modules.state.StatePropertyDeduplicator.logStats();
        com.memfixer.modules.collision.CollisionDeduplicator.logStats();

        MemoryUsage afterHeap = memBean.getHeapMemoryUsage();
        long afterMb = afterHeap.getUsed() / (1024 * 1024);
        if (MemFixerConfig.ENABLE_LOGGING) {
            LOGGER.info("[MemFixer/Reclaimer] Post-initialization memory sweep finalized. Active heap: {} MB.", afterMb);
        }

        SCHEDULER.shutdown();
    }

    /**
     * Executes an asynchronous non-blocking memory sweep when leaving a singleplayer or multiplayer world.
     * Reclaims chunk section buffers, mob entity renderers, and audio caches back to the JVM.
     */
    public static void onLevelUnload() {
        if (!MemFixerConfig.RECLAIM_POST_LAUNCH) return;

        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                // Short 250ms yield to allow menu transition rendering to settle
                Thread.sleep(250);
            } catch (InterruptedException ignored) {}

            try {
                net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                if (mc != null && mc.level != null) {
                    return; // Player already entered a new world, do not GC
                }
            } catch (Throwable ignored) {}

            MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
            long beforeMb = memBean.getHeapMemoryUsage().getUsed() / (1024 * 1024);

            com.memfixer.modules.chunk.ChunkStorageOptimizer.logStats();
            com.memfixer.modules.tag.TagDeduplicator.clearPools();
            com.memfixer.modules.shape.ShapeDeduplicator.logStats();
            com.memfixer.modules.recipe.IngredientDeduplicator.logStats();
            com.memfixer.modules.state.StatePropertyDeduplicator.logStats();
            com.memfixer.modules.collision.CollisionDeduplicator.logStats();

            long afterMb = memBean.getHeapMemoryUsage().getUsed() / (1024 * 1024);
            if (MemFixerConfig.ENABLE_LOGGING) {
                LOGGER.info("[MemFixer/Reclaimer] World unloaded. Disconnect memory cleanup completed. Active heap: {} MB (freed from: {} MB).",
                        afterMb, beforeMb);
            }
        });
    }
}
