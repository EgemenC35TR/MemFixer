package com.memfixer.modules.texture;

import com.memfixer.config.MemFixerConfig;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlas;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Modular handler responsible for releasing CPU-side pixel buffers
 * once texture atlas pages have been transferred to GPU VRAM.
 */
public final class TextureReaperHandler {
    private static final Logger LOGGER = LogManager.getLogger("MemFixer/TextureReaper");

    private TextureReaperHandler() {}

    private static final java.util.concurrent.atomic.AtomicInteger TOTAL_STATIC_REAPED = new java.util.concurrent.atomic.AtomicInteger(0);

    public static int getTotalReapedCount() {
        return TOTAL_STATIC_REAPED.get();
    }

    public static void reapStaticSprites(TextureAtlas atlas, List<SpriteContents> sprites) {
        if (!com.memfixer.config.MemFixerConfig.REAP_TEXTURES || sprites == null || sprites.isEmpty()) {
            return;
        }

        String path = atlas.location().getPath();
        // Target heavy 3D terrain and trim atlases (>95% memory). Preserve GUI, map decorations, and HUD atlases for mod compatibility.
        if (!path.contains("blocks") && !path.contains("armor_trims")) {
            return;
        }

        int closedCount = 0;
        int animatedCount = 0;

        for (SpriteContents sprite : sprites) {
            if (sprite != null) {
                try {
                    // If the sprite has no animation ticker, its CPU pixel buffers are never referenced again.
                    if (sprite.createTicker() == null) {
                        sprite.close();
                        closedCount++;
                        TOTAL_STATIC_REAPED.incrementAndGet();
                    } else {
                        animatedCount++;
                    }
                } catch (Throwable t) {
                    LOGGER.debug("[MemFixer/TextureReaper] Skipped sprite in atlas {}: {}", atlas.location(), t.getMessage());
                }
            }
        }

        if (MemFixerConfig.ENABLE_LOGGING) {
            LOGGER.info("[MemFixer/TextureReaper] Atlas '{}' processed: {} static sprite buffers released to GPU, {} animated tickers retained.",
                    atlas.location(), closedCount, animatedCount);
        }
    }
}
