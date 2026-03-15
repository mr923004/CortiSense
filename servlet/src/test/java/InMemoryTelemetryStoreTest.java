import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTelemetryStoreTest {

    //Verifies that storing telemetry for a new bed ID
    @Test
    void store_shouldCreateNewEntryForNewBed() {
        InMemoryTelemetryStore store = new InMemoryTelemetryStore();

        PatientTelemetry telemetry = new PatientTelemetry();
        telemetry.getHr().add(80.0);

        // Store telemetry under a new bed ID
        store.store("bed-1", telemetry);

        // Retrieve and verify stored data
        PatientTelemetry result = store.get("bed-1");
        assertNotNull(result);
        assertEquals(1, result.getHr().size());
    }
    //Verifies that storing telemetry for an existing bed ID appends new data rather than overwriting existing data.
    @Test
    void store_shouldAppendTelemetryForExistingBed() {
        InMemoryTelemetryStore store = new InMemoryTelemetryStore();

        PatientTelemetry first = new PatientTelemetry();
        first.getHr().add(70.0);

        PatientTelemetry second = new PatientTelemetry();
        second.getHr().add(75.0);

        // Store two telemetry packets for the same bed
        store.store("bed-2", first);
        store.store("bed-2", second);

        // Verify data was appended, not overwritten
        PatientTelemetry result = store.get("bed-2");
        assertEquals(2, result.getHr().size());
        assertEquals(75.0, result.getHr().get(1));
    }
    //Verifies that null inputs are safely ignore and do not modify the store.

    @Test
    void store_shouldIgnoreNullInputs() {
        InMemoryTelemetryStore store = new InMemoryTelemetryStore();

        // Attempt to store invalid input
        store.store(null, null);

        // Store should remain empty
        assertTrue(store.getAll().isEmpty());
    }
}
