import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe, in-memory implementation of {@link AbstractTelemetryStore}.
 *
 * Each subject (keyed by a string ID such as {@code "user-1"}) maps to a
 * single {@link CortisolReading} that accumulates all readings for that
 * subject via {@link CortisolReading#append(CortisolReading)}.
 */
public class InMemoryTelemetryStore extends AbstractTelemetryStore {

    private final Map<String, CortisolReading> store = new ConcurrentHashMap<>();

    @Override
    public void store(String subjectId, CortisolReading incoming) {
        if (subjectId == null || incoming == null) return;

        store.compute(subjectId, (id, existing) -> {
            if (existing == null) {
                // First reading for this subject
                return incoming;
            }
            // Merge subsequent readings into the existing accumulator.
            existing.append(incoming);
            return existing;
        });
    }

    @Override
    public CortisolReading get(String subjectId) {
        if (subjectId == null) return null;
        return store.get(subjectId);
    }

    @Override
    public Map<String, CortisolReading> getAll() {
        return Collections.unmodifiableMap(store);
    }
}