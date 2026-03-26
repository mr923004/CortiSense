import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stores an in-memory time-series of cortisol readings for one subject.
 *
 * Units     : nmol/L
 * Timestamps: epoch milliseconds (UTC)
 *
 * A rolling cap of MAX_READINGS is kept so the in-memory footprint
 */
public class CortisolReading {

    /** 7 days × 288 readings/day. */
    private static final int MAX_READINGS = 2016;

    private final List<Long>   ts       = new ArrayList<>();
    private final List<Double> cortisol = new ArrayList<>();
    private String phase = "baseline";

    public synchronized void addReading(long timestampMs, double nmolPerL, String phaseLabel) {
        ts.add(timestampMs);
        cortisol.add(nmolPerL);
        if (phaseLabel != null && !phaseLabel.isBlank()) {
            phase = phaseLabel;
        }
        prune();
    }

    /**
     * Used by InMemoryTelemetryStore.store() to accumulate readings
     * across successive POST calls.
     */
    public synchronized void append(CortisolReading incoming) {
        if (incoming == null) return;
        ts.addAll(incoming.ts);
        cortisol.addAll(incoming.cortisol);
        if (incoming.phase != null && !incoming.phase.isBlank()) {
            phase = incoming.phase;
        }
        prune();
    }

    public synchronized List<Long>   getTs()       { return Collections.unmodifiableList(ts); }
    public synchronized List<Double> getCortisol() { return Collections.unmodifiableList(cortisol); }
    public synchronized String       getPhase()    { return phase; }
    public synchronized int          size()        { return ts.size(); }

    /** Latest cortisol value, or NaN if no readings exist yet. */
    public synchronized double latest() {
        return cortisol.isEmpty() ? Double.NaN : cortisol.get(cortisol.size() - 1);
    }

    private void prune() {
        while (ts.size() > MAX_READINGS) {
            ts.remove(0);
            cortisol.remove(0);
        }
    }
}