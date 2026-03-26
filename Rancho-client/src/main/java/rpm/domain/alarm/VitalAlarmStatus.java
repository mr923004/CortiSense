package rpm.domain.alarm;

import rpm.domain.VitalType;
import java.time.Instant;

public final class VitalAlarmStatus {

    // Which vital this alarm refers to
    private final VitalType type;

    // Current alarm level for this vital
    private final AlarmLevel level;

    // Short explanation of why the alarm is active
    private final String reason;

    // Time when this alarm level started
    private final Instant since;

    public VitalAlarmStatus(VitalType type, AlarmLevel level,
                            String reason, Instant since) {
        this.type = type;
        this.level = level;
        this.reason = reason;
        this.since = since;
    }

    public VitalType getType() {
        return type;
    }

    public AlarmLevel getLevel() {
        return level;
    }

    public String getReason() {
        return reason;
    }

    public Instant getSince() {
        return since;
    }
}
