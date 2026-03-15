package rpm.ui.alerts;

import rpm.domain.PatientId;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

// Manage temporary suppressions of alerts. Silences "acknowledged" alerts for a duration
public final class AlertAcknowledger {

    private final Map<PatientId, Long> ackUntilMs = new HashMap<>();

    // Silences alerts for a specific patient for a specific duration
    public void acknowledge(PatientId id, Duration duration, long nowMs) {
        if (id == null) return;
        if (duration == null || duration.isZero() || duration.isNegative()) duration = Duration.ofSeconds(30);
        ackUntilMs.put(id, nowMs + duration.toMillis());
    }

    // Checks if alerts for a patient should be suppressed
    public boolean isAcknowledged(PatientId id, long nowMs) {
        Long until = ackUntilMs.get(id);
        if (until == null) return false;
        if (nowMs >= until) {
            ackUntilMs.remove(id);
            return false;
        }
        return true;
    }

    public void clear(PatientId id) { ackUntilMs.remove(id); }
    public void clearAll() { ackUntilMs.clear(); }
}
