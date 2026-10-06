package com.memfixer.modules;

import com.memfixer.config.MemFixerConfig;
import com.memfixer.modules.resource.ResourceInterner;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Subsystem 9: ResourceInterner Tests")
class ResourceInternerTest {

    @Test
    @DisplayName("Should return constant reference for 'minecraft' namespace")
    void testMinecraftNamespaceFastPath() {
        MemFixerConfig.INTERN_RESOURCES = true;

        String dynamicMinecraft = new String("minecraft");
        String result = ResourceInterner.intern(dynamicMinecraft);

        assertSame("minecraft", result);
    }

    @Test
    @DisplayName("Should intern dynamic duplicate strings to the same instance")
    void testDynamicStringInterning() {
        MemFixerConfig.INTERN_RESOURCES = true;

        String path1 = new String("block/stone_slab");
        String path2 = new String("block/stone_slab");

        assertNotSame(path1, path2);

        String interned1 = ResourceInterner.intern(path1);
        String interned2 = ResourceInterner.intern(path2);

        assertSame(interned1, interned2);
    }

    @Test
    @DisplayName("Should handle null, empty, and strings exceeding max length safely")
    void testBoundaryConditions() {
        MemFixerConfig.INTERN_RESOURCES = true;

        assertNull(ResourceInterner.intern(null));
        assertEquals("", ResourceInterner.intern(""));

        String longStr = "a".repeat(100);
        assertSame(longStr, ResourceInterner.intern(longStr));
    }
}
