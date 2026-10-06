package com.memfixer.modules.chunk;

import net.minecraft.util.ThreadingDetector;

/**
 * High-performance no-op ThreadingDetector singleton.
 * Eliminates per-container Semaphore and ReentrantLock allocation churn (~5 objects per container)
 * across tens of thousands of PalettedContainers in loaded chunks.
 */
public final class DummyThreadingDetector extends ThreadingDetector {
    public static final DummyThreadingDetector INSTANCE = new DummyThreadingDetector();

    private DummyThreadingDetector() {
        super("MemFixerDummy");
    }

    @Override
    public void checkAndLock() {
        // No-op: Lock-free operation in production environment
    }

    @Override
    public void checkAndUnlock() {
        // No-op: Lock-free operation in production environment
    }
}
