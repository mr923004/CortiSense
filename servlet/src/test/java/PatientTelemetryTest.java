import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PatientTelemetryTest {

    // Tests that patient telemetry data is safely merged
    @Test
    void append_shouldMergeTelemetryValues() {
        // Existing telemetry
        PatientTelemetry base = new PatientTelemetry();

        // New incoming telemetry
        PatientTelemetry incoming = new PatientTelemetry();
        incoming.getTs().add(1000L);
        incoming.getHr().add(72.0);
        incoming.getTemp().add(36.7);

        // Merge incoming data into base
        base.append(incoming);

        // Verify merged results
        assertEquals(1, base.getTs().size());
        assertEquals(1000L, base.getTs().get(0));
        assertEquals(72.0, base.getHr().get(0));
        assertEquals(36.7, base.getTemp().get(0));
    }
    // Test that null inputs are handled without errors
    @Test
    void append_shouldIgnoreNullInput() {
        PatientTelemetry base = new PatientTelemetry();

        // Append null telemetry
        base.append(null);

        // Ensure no data was added
        assertTrue(base.getTs().isEmpty());
        assertTrue(base.getHr().isEmpty());
        assertTrue(base.getTemp().isEmpty());
    }
}
