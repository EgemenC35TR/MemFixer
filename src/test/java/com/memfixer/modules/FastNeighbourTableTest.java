package com.memfixer.modules;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.memfixer.modules.state.FastNeighbourTable;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Subsystem 2: FastNeighbourTable Tests")
class FastNeighbourTableTest {

    private final BooleanProperty testProp = BooleanProperty.create("test_prop");

    @Test
    @DisplayName("Should return empty table singleton when source is empty")
    void testEmptySource() {
        Table<Property<?>, Comparable<?>, String> source = HashBasedTable.create();
        Table<Property<?>, Comparable<?>, String> compacted = FastNeighbourTable.create(source);

        assertSame(FastNeighbourTable.empty(), compacted);
        assertTrue(compacted.isEmpty());
        assertEquals(0, compacted.size());
    }

    @Test
    @DisplayName("Should correctly lookup values from compacted table")
    void testCompactAndLookup() {
        Table<Property<?>, Comparable<?>, String> source = HashBasedTable.create();
        source.put(testProp, true, "State_Waterlogged_True");
        source.put(testProp, false, "State_Waterlogged_False");

        Table<Property<?>, Comparable<?>, String> table = FastNeighbourTable.create(source);

        assertEquals(2, table.size());
        assertFalse(table.isEmpty());
        assertEquals("State_Waterlogged_True", table.get(testProp, true));
        assertEquals("State_Waterlogged_False", table.get(testProp, false));
        assertNull(table.get(testProp, 42));

        assertTrue(table.contains(testProp, true));
        assertTrue(table.containsRow(testProp));
        assertTrue(table.containsColumn(true));
        assertTrue(table.containsValue("State_Waterlogged_True"));
    }

    @Test
    @DisplayName("Should share canonical keys between two tables with same property/value layout")
    void testSharedKeys() {
        Table<Property<?>, Comparable<?>, String> source1 = HashBasedTable.create();
        source1.put(testProp, true, "StateA_True");
        source1.put(testProp, false, "StateA_False");

        Table<Property<?>, Comparable<?>, String> source2 = HashBasedTable.create();
        source2.put(testProp, true, "StateB_True");
        source2.put(testProp, false, "StateB_False");

        Table<Property<?>, Comparable<?>, String> table1 = FastNeighbourTable.create(source1);
        Table<Property<?>, Comparable<?>, String> table2 = FastNeighbourTable.create(source2);

        assertNotSame(table1, table2);
        assertEquals("StateA_True", table1.get(testProp, true));
        assertEquals("StateB_True", table2.get(testProp, true));
    }

    @Test
    @DisplayName("Should share canonical keys even when cells are inserted in reverse or different order")
    void testCanonicalSharedKeysOrderIndependent() {
        BooleanProperty propFacing = BooleanProperty.create("facing");

        Table<Property<?>, Comparable<?>, String> source1 = HashBasedTable.create();
        source1.put(testProp, true, "State1_W_True");
        source1.put(testProp, false, "State1_W_False");
        source1.put(propFacing, true, "State1_F_True");
        source1.put(propFacing, false, "State1_F_False");

        Table<Property<?>, Comparable<?>, String> source2 = HashBasedTable.create();
        source2.put(propFacing, false, "State2_F_False");
        source2.put(propFacing, true, "State2_F_True");
        source2.put(testProp, false, "State2_W_False");
        source2.put(testProp, true, "State2_W_True");

        int keysBefore = FastNeighbourTable.getSharedKeysCount();
        Table<Property<?>, Comparable<?>, String> table1 = FastNeighbourTable.create(source1);
        Table<Property<?>, Comparable<?>, String> table2 = FastNeighbourTable.create(source2);
        int keysAfter = FastNeighbourTable.getSharedKeysCount();

        assertEquals(keysBefore + 1, keysAfter);
        assertEquals("State1_W_True", table1.get(testProp, true));
        assertEquals("State2_W_True", table2.get(testProp, true));
        assertEquals("State1_F_False", table1.get(propFacing, false));
        assertEquals("State2_F_False", table2.get(propFacing, false));
    }
}
