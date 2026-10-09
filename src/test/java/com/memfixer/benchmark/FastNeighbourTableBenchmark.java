package com.memfixer.benchmark;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.memfixer.modules.state.FastNeighbourTable;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Microbenchmark suite evaluating FastNeighbourTable O(1) indexed array lookup
 * performance against standard Guava HashBasedTable.
 */
@DisplayName("Benchmark: FastNeighbourTable vs Guava Table")
public class FastNeighbourTableBenchmark {

    @Test
    @DisplayName("Benchmark lookup throughput between FastNeighbourTable and Guava HashBasedTable")
    void benchmarkTableLookups() {
        BooleanProperty propWaterlogged = BooleanProperty.create("waterlogged");
        IntegerProperty propAge = IntegerProperty.create("age", 0, 7);
        BooleanProperty propPowered = BooleanProperty.create("powered");

        Table<Property<?>, Comparable<?>, String> guavaTable = HashBasedTable.create();
        guavaTable.put(propWaterlogged, true, "State_WL_True");
        guavaTable.put(propWaterlogged, false, "State_WL_False");
        for (int i = 0; i <= 7; i++) {
            guavaTable.put(propAge, i, "State_Age_" + i);
        }
        guavaTable.put(propPowered, true, "State_Pwr_True");
        guavaTable.put(propPowered, false, "State_Pwr_False");

        Table<Property<?>, Comparable<?>, String> fastTable = FastNeighbourTable.create(guavaTable);

        // Warmup JIT compiler
        for (int i = 0; i < 50_000; i++) {
            guavaTable.get(propAge, 4);
            fastTable.get(propAge, 4);
        }

        int iterations = 500_000;

        // Measure Guava lookup
        long startGuava = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            String val = guavaTable.get(propAge, 3);
            assertNotNull(val);
        }
        long durationGuava = System.nanoTime() - startGuava;

        // Measure FastNeighbourTable lookup
        long startFast = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            String val = fastTable.get(propAge, 3);
            assertNotNull(val);
        }
        long durationFast = System.nanoTime() - startFast;

        double guavaNsPerOp = (double) durationGuava / iterations;
        double fastNsPerOp = (double) durationFast / iterations;

        // Verify validity
        assertTrue(fastNsPerOp > 0, "FastNeighbourTable lookup time must be positive");
        assertTrue(guavaNsPerOp > 0, "Guava Table lookup time must be positive");
    }
}
