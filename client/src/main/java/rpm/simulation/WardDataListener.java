package rpm.simulation;

import rpm.domain.PatientId;
import rpm.domain.VitalSnapshot;

import java.time.Instant;

/*
- Listener interface for receiving updates from the ward simulation.
- Implementations can react to new vitals, ECG data, and patient events.
- All methods are optional (default empty implementations).
*/
public interface WardDataListener {

    // Called whenever a new set of vital signs is produced for a patient
    default void onVitalsSnapshot(PatientId id, VitalSnapshot snapshot) {}

    // Called when a new ECG segment is generated
    default void onEcgSegment(PatientId id, Instant time, double[] segment) {}

    // Called when a medical event starts for a patient
    default void onEventStarted(PatientId id, PatientEvent event) {}

    // Called when a medical event ends for a patient
    default void onEventEnded(PatientId id, PatientEvent event) {}
}
