package rpm.domain;

import java.time.Instant;
import java.util.Map;

// Snapshot of all vital readings taken at a single point in time
public class VitalSnapshot {

    // When the snapshot was captured
    private final Instant timestamp;

    // Map of vital type to its measured value
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
