package com.memfixer.mixin.chunk;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

/**
 * Mixin accessor allowing safe injection of shared empty structure map singletons on client chunks.
 */
@Mixin(ChunkAccess.class)
public interface ChunkAccessAccessor {

    @Mutable
    @Accessor("structureStarts")
    void setStructureStarts(Map<Structure, StructureStart> starts);

    @Mutable
    @Accessor("structuresRefences")
    void setStructuresRefences(Map<Structure, LongSet> references);
}
