package com.memfixer.modules;

import com.memfixer.modules.shape.ShapeDeduplicator;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Subsystem 7: ShapeDeduplicator Tests")
class ShapeDeduplicatorTest {

    @Test
    @DisplayName("Should return Shapes.block() for all faces of Shapes.block()")
    void testBlockFaces() {
        for (Direction direction : Direction.values()) {
            VoxelShape face = ShapeDeduplicator.getFaceShape(Shapes.block(), direction);
            assertSame(Shapes.block(), face);
        }
    }

    @Test
    @DisplayName("Should return Shapes.empty() for all faces of Shapes.empty()")
    void testEmptyFaces() {
        for (Direction direction : Direction.values()) {
            VoxelShape face = ShapeDeduplicator.getFaceShape(Shapes.empty(), direction);
            assertSame(Shapes.empty(), face);
        }
    }

    @Test
    @DisplayName("Should deduplicate identical boolean arrays for sturdy faces")
    void testSturdyArrayDeduplication() {
        boolean[] array1 = new boolean[18];
        array1[0] = true;
        array1[5] = true;

        boolean[] array2 = new boolean[18];
        array2[0] = true;
        array2[5] = true;

        assertNotSame(array1, array2);

        boolean[] canonical1 = ShapeDeduplicator.deduplicateFaceSturdy(array1);
        boolean[] canonical2 = ShapeDeduplicator.deduplicateFaceSturdy(array2);

        assertSame(canonical1, canonical2);
        assertTrue(canonical1[0]);
        assertTrue(canonical1[5]);
        assertFalse(canonical1[1]);
    }

    @Test
    @DisplayName("Should handle null and empty arrays safely")
    void testSafety() {
        assertNull(ShapeDeduplicator.deduplicateFaceSturdy(null));
        boolean[] shortArray = new boolean[5];
        assertSame(shortArray, ShapeDeduplicator.deduplicateFaceSturdy(shortArray));
    }
}
