package rpm.domain.report;

import rpm.domain.PatientId;
import rpm.domain.VitalSnapshot;
import rpm.domain.alarm.AlarmTransition;

import java.time.Instant;
import java.util.List;

// Provides access to patient vital data and alarm history for reporting
public interface PatientDataSource {

    // Returns vital snapshots for the given patient within the time range
    List<VitalSnapshot> getVitals(PatientId id, Instant from, Instant to);

    // Returns alarm transitions for the given patient within the time range
    List<AlarmTransition> getAlarmTransitions(PatientId id, Instant from, Instant to);
}
