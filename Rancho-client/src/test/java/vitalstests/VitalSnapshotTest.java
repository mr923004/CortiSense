package vitalstests;

import org.junit.jupiter.api.Test;
import rpm.domain.VitalSnapshot;
import rpm.domain.VitalType;


import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class VitalSnapshotTest {

    //verify that a VitalSnapshot object can be safely constructed with valid inputs
    @Test
    void constructorShouldAcceptValidInputs() {
        assertDoesNotThrow(() ->
                new VitalSnapshot(Instant.now(), Map.of())
        );
    }
    //null cases
    @Test
    void constructorShouldNotReturnNullObject() {
        VitalSnapshot snapshot = new VitalSnapshot(Instant.now(), Map.of());
        assertNotNull(snapshot);
    }

    //can create many snapshots
    @Test
    void shouldCreateManySnapshotsWithoutFailure() {
        for (int i = 0; i < 100; i++) {
            Map<VitalType, Double> vitals = new HashMap<>();
            vitals.put(VitalType.HEART_RATE, 98.0);
            vitals.put(VitalType.RESP_RATE, 98.0);
            vitals.put(VitalType.TEMPERATURE, 37.0);


            VitalSnapshot snapshot = new VitalSnapshot(Instant.now(), vitals);
            assertNotNull(snapshot);
        }
    }
}
