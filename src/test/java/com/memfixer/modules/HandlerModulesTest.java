package com.memfixer.modules;

import com.memfixer.config.MemFixerConfig;
import com.memfixer.modules.dfu.LazyDfuHandler;
import com.memfixer.modules.font.LazyUnihexHandler;
import com.memfixer.modules.texture.TextureReaperHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Subsystems 3, 5, 6: Handler Modules Tests")
class HandlerModulesTest {

    @Test
    @DisplayName("LazyDfuHandler should reflect MemFixerConfig.LAZY_DFU state")
    void testLazyDfuState() {
        MemFixerConfig.LAZY_DFU = true;
        assertTrue(LazyDfuHandler.shouldBypassOptimization());

        MemFixerConfig.LAZY_DFU = false;
        assertFalse(LazyDfuHandler.shouldBypassOptimization());

        MemFixerConfig.LAZY_DFU = true; // reset
    }

    @Test
    @DisplayName("TextureReaperHandler should safely ignore null or empty sprite collections")
    void testTextureReaperSafety() {
        MemFixerConfig.REAP_TEXTURES = true;

        assertDoesNotThrow(() -> TextureReaperHandler.reapStaticSprites(null, null));
        assertDoesNotThrow(() -> TextureReaperHandler.reapStaticSprites(null, List.of()));
    }

    @Test
    @DisplayName("LazyUnihexHandler should reflect MemFixerConfig.OPTIMIZE_UNIFONT toggle")
    void testLazyUnihexToggle() {
        MemFixerConfig.OPTIMIZE_UNIFONT = false;
        assertFalse(LazyUnihexHandler.shouldBypassEagerLoad());

        MemFixerConfig.OPTIMIZE_UNIFONT = true;
        // In headless test without CJK options, returns true
        assertTrue(LazyUnihexHandler.shouldBypassEagerLoad());
    }
}
