package com.memfixer.gametest;

import com.memfixer.MemFixer;
import com.memfixer.modules.chunk.ChunkStorageOptimizer;
import com.memfixer.modules.collision.CollisionDeduplicator;
import com.memfixer.modules.shape.ShapeDeduplicator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.gametest.GameTestHolder;

/**
 * In-game GameTests verifying MemFixer optimization subsystems inside an active Minecraft world.
 */
@GameTestHolder(MemFixer.MOD_ID)
public class MemFixerGameTests {

    @GameTest(template = "empty")
    public static void testCollisionDeduplicatorFullCube(GameTestHelper helper) {
        VoxelShape blockShape = Shapes.block();
        List<AABB> aabbs = CollisionDeduplicator.computeAabbs(blockShape);
        helper.assertTrue(
                aabbs == CollisionDeduplicator.FULL_CUBE_LIST,
                "CollisionDeduplicator must return pre-cached FULL_CUBE_LIST singleton");
        helper.assertTrue(
                CollisionDeduplicator.computeBounds(blockShape) == CollisionDeduplicator.FULL_CUBE,
                "CollisionDeduplicator must return FULL_CUBE bounds singleton");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void testChunkStorageZeroBitSingletons(GameTestHelper helper) {
        helper.assertTrue(
                ChunkStorageOptimizer.getZeroBitStorage(4096) == ChunkStorageOptimizer.STATIC_4096,
                "ZeroBitStorage for 4096 must return STATIC_4096 singleton");
        helper.assertTrue(
                ChunkStorageOptimizer.getZeroBitStorage(64) == ChunkStorageOptimizer.STATIC_64,
                "ZeroBitStorage for 64 must return STATIC_64 singleton");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void testShapeDeduplicatorOcclusion(GameTestHelper helper) {
        VoxelShape stoneShape = Blocks.STONE.defaultBlockState().getShape(helper.getLevel(), BlockPos.ZERO);
        VoxelShape faceUp = ShapeDeduplicator.getFaceShape(stoneShape, Direction.UP);
        helper.assertTrue(faceUp == Shapes.block(), "Full cube face shape must match Shapes.block()");
        helper.succeed();
    }
}
