package rpm.domain.alarm;

import rpm.domain.PatientId;
import java.time.Instant;

public interface AlarmListener {

    // Called when a single alarm changes level/colour
    void onAlarmTransition(AlarmTransition transition);

    // Called when the full alarm state for a patient is updated
    void onAlarmState(PatientId id, Instant time, AlarmState state);
}
