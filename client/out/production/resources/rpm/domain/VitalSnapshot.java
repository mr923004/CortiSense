package rpm.domain;

import java.time.Instant;
import java.util.Map;

/**
 * Immutable snapshot of all vitals at a single point in time.
 * Holds a timestamp and a map of vital types to their numeric values.
 */

public class VitalSnapshot {

    private final Instant timestamp;
    private final Map<VitalType, Double> values;

    public VitalSnapshot(Instant timestamp, Map<VitalType, Double> values) {
        this.timestamp = timestamp;
        this.values = values;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Map<VitalType, Double> getValues() {
        return values;
    }
}
