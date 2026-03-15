package rpm.domain.alarm;

import rpm.domain.PatientId;
import rpm.domain.VitalType;

import java.time.Instant;

public final class AlarmTransition {

    // Which patient the alarm belongs to
    private final PatientId patientId;

    // Which vital triggered the change
    private final VitalType vitalType;

    // Previous alarm level
    private final AlarmLevel from;

    // New alarm level
    private final AlarmLevel to;

    // Time when the change occurred
    private final Instant time;

    // Short explanation for the change (for UI and logs)
    private final String reason;

    public AlarmTransition(PatientId patientId, VitalType vitalType,
                           AlarmLevel from, AlarmLevel to,
                           Instant time, String reason) {
        this.patientId = patientId;
        this.vitalType = vitalType;
        this.from = from;
        this.to = to;
        this.time = time;
        this.reason = reason;
    }

    public PatientId getPatientId() {
        return patientId;
    }

    public VitalType getVitalType() {
        return vitalType;
    }

    public AlarmLevel getFrom() {
        return from;
    }

    public AlarmLevel getTo() {
        return to;
    }

    public Instant getTime() {
        return time;
    }

    public String getReason() {
        return reason;
    }
}
