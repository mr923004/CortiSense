package rpm.simulation;

import rpm.domain.PatientId;
import rpm.domain.VitalSnapshot;

import java.time.Instant;

public interface WardDataListener {
    default void onVitalsSnapshot(PatientId id, VitalSnapshot snapshot) {}
    default void onEcgSegment(PatientId id, Instant time, double[] segment) {}
    default void onEventStarted(PatientId id, PatientEvent event) {}
    default void onEventEnded(PatientId id, PatientEvent event) {}
}

